package tide.trader.bot.service;

import lombok.NonNull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.marketdata.params.CurrencyPairsParam;
import org.knowm.xchange.service.marketdata.params.DefaultCancelOrderByClientOrderIdParams;
import org.knowm.xchange.service.marketdata.params.PeriodParams;
import tide.trader.bot.dto.market.TickerDTO;
import tide.trader.bot.dto.util.CurrencyPairDTO;
import tide.trader.bot.util.base.Base;
import tide.trader.bot.util.base.service.BaseService;
import tide.trader.bot.util.java.ZonedDateTimeBetween;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Market service - XChange implementation of {@link MarketService}.
 */
public class MarketServiceXChangeImplementation extends BaseService implements MarketService {

    /** XChange service. */
    private final MarketDataService marketDataService;

    /** Cached reply from Exchange. */
    private final List<Ticker> cachedReply = new ArrayList<>();

    /** Cached max ticker. */
    private final static int MAX_HISTORY_TICKERS = 5000;

    /** Cached ticker from Exchange. */
    private final Map<String, CircularFifoQueue<TickerDTO>> cachedHistoryTickers = new ConcurrentHashMap<>();

    /** time record **/
    private final String FILE_TIME = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"));

    /**
     * Constructor.
     *
     * @param rate                 rate in ms
     * @param newMarketDataService market data service
     */
    public MarketServiceXChangeImplementation(final long rate, final MarketDataService newMarketDataService) {
        super(rate);
        this.marketDataService = newMarketDataService;
    }

    @Override
    @SuppressWarnings("checkstyle:DesignForExtension")
    public Optional<TickerDTO> getTicker(@NonNull final CurrencyPairDTO currencyPair) {
        try {
            // Consume a token from the token bucket.
            // If a token is not available this method will block until the refill adds one to the bucket.
            bucket.asBlocking().consume(1);

            logger.debug("Retrieving ticker for {} currency pair", currencyPair);
            TickerDTO t = Base.TICKER_MAPPER.mapToTickerDTO(marketDataService.getTicker(Base.CURRENCY_MAPPER.mapToInstrument(currencyPair)));
            logger.debug(" - New ticker {}", t);
            return Optional.ofNullable(t);
        } catch (IOException e) {
            logger.error("Error retrieving ticker: {}", e.getMessage());
            return Optional.empty();
        } catch (InterruptedException e) {
            return Optional.empty();
        }
    }

    @Override
    @SuppressWarnings("checkstyle:DesignForExtension")
    public Set<TickerDTO> getTickers(@NonNull final Set<CurrencyPairDTO> currencyPairs) {
        try {
            // We create the currency pairs parameter required by some exchanges.
            CurrencyPairsParam params = () -> currencyPairs
                    .stream()
                    .map(Base.CURRENCY_MAPPER::mapToCurrencyPair)
                    .collect(Collectors.toList());

            // Consume a token from the token bucket.
            // If a token is not available this method will block until the refill adds one to the bucket.
            bucket.asBlocking().consume(1);

            logger.debug("Retrieving ticker for {} currency pair", currencyPairs.size());
            final List<Ticker> tickers = marketDataService.getTicker(params);
            cachedReply.clear();
            cachedReply.addAll(tickers.stream().filter(ticker -> ticker.getLast().compareTo(BigDecimal.ZERO) > 0).collect(Collectors.toList()));
            return tickers.stream()
                    .filter(ticker -> CollectionUtils.containsAny(params.getCurrencyPairs(), ticker.getInstrument()))
                    .map(Base.TICKER_MAPPER::mapToTickerDTO)
                    .peek(t -> logger.debug(" - New ticker: {}", t))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } catch (IOException e) {
            logger.error("Error retrieving tickers: {}", e.getMessage());
            return Collections.emptySet();
        } catch (InterruptedException e) {
            return Collections.emptySet();
        }
    }

    @Override
    public List<TickerDTO> getHistoryTickers(CurrencyPairDTO currencyPair, Duration duration, ZonedDateTimeBetween between) {
        try {
            // We create the currency pairs parameter required by some exchanges.
            PeriodParams params = new DefaultCancelOrderByClientOrderIdParams(Base.CURRENCY_MAPPER.mapToCurrencyPair(currencyPair), duration.toMillis(), between.getStartToMilli(), between.getEndToMilli()-1);

            // Consume a token from the token bucket.
            // If a token is not available this method will block until the refill adds one to the bucket.
            bucket.asBlocking().consume(1);
            logger.debug("Retrieving history ticker for {} currency pair", currencyPair);

            List<TickerDTO> tickers = marketDataService.getKlines(params)
                    .stream()
                    .map(Base.TICKER_MAPPER::mapToTickerDTO)
                    .peek(t -> logger.debug(" - HisNew ticker: {}", t))
                    .collect(Collectors.toList());

            CircularFifoQueue<TickerDTO> cachedQueue = cachedHistoryTickers.computeIfAbsent(currencyPair + "_" + duration, value -> new CircularFifoQueue<>(MAX_HISTORY_TICKERS));
            tickers.stream().filter(t -> !cachedQueue.contains(t)).forEach(cachedQueue::add);
            //write in local file
            historyTickersToLocalFile(currencyPair, duration);
            return tickers;

        } catch (IOException e) {
            logger.error("Error retrieving tickers: {}", e.getMessage());
            return Collections.emptyList();
        } catch (InterruptedException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public Set<TickerDTO> getTickersFromCache() {
        return cachedReply.stream()
                .map(Base.TICKER_MAPPER::mapToTickerDTO)
                .peek(t -> logger.debug(" - New ticker: {}", t))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public List<TickerDTO> getHistoryTickersFromCache(CurrencyPairDTO currencyPair, Duration duration) {
        return cachedHistoryTickers.putIfAbsent(currencyPair + "_" + duration, new CircularFifoQueue<>(MAX_HISTORY_TICKERS)).stream().collect(Collectors.toList());
    }

    /**
     * Save data locally
     */
    private void historyTickersToLocalFile(CurrencyPairDTO currencyPair, Duration duration) {
        if(logger.isDebugEnabled()) {
            FileOutputStream outputStream = null;
            try {
                File directory = new File(System.getProperty("user.dir") + "/data/");
                directory.mkdir();

                File file = new File(directory.getAbsoluteFile() + "/tickers-" + duration + "-" + currencyPair.getBaseCurrency() + "-" + currencyPair.getQuoteCurrency() + "_" + FILE_TIME + ".tsv");
                if(!directory.exists()) {
                    file.createNewFile();
                }
                outputStream = new FileOutputStream(file);//形参里面可追加true参数，表示在原有文件末尾追加信息
                String data = getHistoryTickersFromCache(currencyPair, duration).stream().map(ticker -> ticker.getTimestamp().toEpochSecond() + "\t" + ticker.getOpen().stripTrailingZeros().toPlainString() + "\t" + ticker.getLast().stripTrailingZeros().toPlainString() + "\t" + ticker.getHigh().stripTrailingZeros().toPlainString() + "\t" + ticker.getLow().stripTrailingZeros().toPlainString() + "\t" + ticker.getVolume().stripTrailingZeros().toPlainString() + "\t" + ticker.getQuoteVolume().stripTrailingZeros().toPlainString()).collect(Collectors.joining("\n"));
                outputStream.write(data.getBytes("utf-8"));
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            } finally {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
