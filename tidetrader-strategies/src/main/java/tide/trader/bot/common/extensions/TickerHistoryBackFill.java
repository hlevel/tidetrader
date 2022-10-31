package tide.trader.bot.common.extensions;

import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Kline;
import org.knowm.xchange.service.marketdata.params.DefaultCancelOrderByClientOrderIdParams;
import org.knowm.xchange.service.marketdata.params.PeriodParams;
import tide.trader.bot.dto.market.TickerDTO;
import tide.trader.bot.dto.util.CurrencyPairDTO;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class TickerHistoryBackFill {

    private final static String OUT_FILE = "tickers-%S-%S.tsv";

    private final Exchange exchange;
    private final String middleFolder;
    private final List<String[]> retryGapList = new ArrayList<>();
    /** Cached History Tickers from Exchange. */
    private final Map<CurrencyPairDTO, List<TickerDTO>> cacheHistoryTickers = new ConcurrentHashMap<>();

    private Duration duration = Duration.ofDays(1);
    private int backfillLength = 500;
    private ChronoUnit backfillUnit = ChronoUnit.HOURS;

    private LocalDateTime backfillFrom = null;
    private LocalDateTime backfillTo = null;

    public TickerHistoryBackFill(Exchange exchange, String middleFolder) {
        this.exchange = exchange;
        if(!middleFolder.startsWith(File.separator)) {
            this.middleFolder = File.separator + middleFolder;
        } else {
            this.middleFolder = middleFolder;
        }
    }

    public LocalDateTime backfillTo() {
        if (backfillTo != null) {
            return backfillTo;
        }
        return LocalDateTime.now();
    }

    public LocalDateTime backfillFrom() {
        if (backfillFrom != null) {
            return backfillFrom;
        }
        return backfillTo().minus(backfillLength, backfillUnit);
    }

    public void backfillFrom(LocalDateTime backfillFrom) {
        this.backfillFrom = backfillFrom;
    }

    public void backfillFrom(LocalDateTime backfillFrom, Duration duration) {
        this.backfillFrom = backfillFrom;
        this.duration = duration;
    }

    public Exchange exchange(){
        return this.exchange;
    }

    public final List<TickerDTO> backfillHistoryList(CurrencyPairDTO currencyPair) {
        return backfillHistory(currencyPair);
    }

    public final void backfillHistoryListFile(CurrencyPairDTO... currencyPairToUpdate) {
        for(CurrencyPairDTO currencyPair : currencyPairToUpdate) {
            File file = this.generateFile(currencyPair);
            if(file.exists()) {
                log.info("Current file {} already exists", file.getName());
                continue;
            }
            List<TickerDTO> currencyPairList = this.backfillHistory(currencyPair);
            this.generateDataFile(currencyPairList, file);
        }
    }

    private final List<TickerDTO> backfillHistory(CurrencyPairDTO currencyPair) {
        final LocalDateTime start = this.backfillFrom();
        final LocalDateTime end = this.backfillTo();
        log.info("Created {} Candle file between {} and {}", currencyPair, start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return this.tickerHistoryList(currencyPair, start, end);
    }

    private final List<TickerDTO> tickerHistoryList(CurrencyPairDTO currencyPair, LocalDateTime start, LocalDateTime end) {
        long startTime = start.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
        long endTime = end.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
        long diffTime = (endTime - startTime);
        long num = diffTime / duration.toSeconds();
        int splitLength = (int) (num / backfillLength);

        String symbol = new CurrencyPair(currencyPair.getBaseCurrency().getSymbol(), currencyPair.getQuoteCurrency().getSymbol()).toString();
        List<String[]> gapList = new ArrayList<>();
        LocalDateTime next = start;
        for(int i = 0; i<splitLength; i++) {
            long startGap = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            next = next.plusSeconds(backfillLength*duration.toSeconds());
            long endGap = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            gapList.add(new String[]{symbol, String.valueOf(startGap), String.valueOf(endGap)});
        }
        LocalDateTime newEnd = end.minusSeconds(end.atZone(ZoneId.systemDefault()).toEpochSecond() % duration.toSeconds());
        if(next.isBefore(newEnd)) {
            gapList.add(new String[]{symbol, String.valueOf(next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()), String.valueOf(newEnd.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())});
        }
/*
        for(String[] g : gapList) {
            System.out.println(g[0] + "," + g[1] + "-" + g[2] + ", s=" + tide.trader.bot.util.DateTimeUtil.formatDateFromUnix(Long.parseLong(g[1]), null)+ ", e=" + tide.trader.bot.util.DateTimeUtil.formatDateFromUnix(Long.parseLong(g[2]), null));
        }
*/

        List<TickerDTO> currencyPairTickerList = new ArrayList<>();
        gapList.stream().map(gap -> getTickers(gap, 3)).forEach(tickers -> currencyPairTickerList.addAll(tickers));
        return currencyPairTickerList;
    }

    private List<TickerDTO> getTickers(String[] gap, Integer retry) {
        try{
            List<TickerDTO> tickerList = new ArrayList<>();

            if(retry <= 0) {
                return tickerList;
            }
            PeriodParams period = new DefaultCancelOrderByClientOrderIdParams(new CurrencyPair(gap[0]), duration.toMillis(), Long.parseLong(gap[1]), Long.parseLong(gap[2]));

            List<Kline> klines = exchange.getMarketDataService().getKlines(period);
            for(Kline kline : klines) {
                ZonedDateTime now = ZonedDateTime.ofInstant((new Date(kline.getOpenTime())).toInstant(), ZoneId.systemDefault());
                TickerDTO ticker = TickerDTO.builder().timestamp(now).open(kline.getOpen()).high(kline.getHigh()).low(kline.getLow()).last(kline.getLast()).volume(kline.getVolume()).quoteVolume(kline.getQuoteVolume()).build();
                tickerList.add(ticker);
            }

            log.info("Loaded {} {} tickers between {} and {}", tickerList.size(), gap[0], gap[1], gap[2]);
            Thread.sleep(3000l);
            return tickerList;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return this.getTickers(gap, retry - 1);
        }
    }

    private List<String[]> lookAbsenceCap(String symbol, List<TickerDTO> tickerList) {
        long previous = 0l;
        List<String[]> gapList = new ArrayList<>();
        for (TickerDTO ticker : tickerList) {
            long should = previous + duration.toMillis();
            long openTime = ticker.getTimestamp().toInstant().toEpochMilli();
            if(previous > 0 && openTime > should) {
                gapList.add(new String[]{symbol, String.valueOf(should), String.valueOf(openTime)});
            }
            previous = openTime;
            //System.out.println("candle=" + DateTimeUtil.formatDateFromUnix(candle.getOpenTime()*1000, null));
        }
        return gapList;
    }

    private File generateFile(CurrencyPairDTO currencyPair) {
        String root = System.getProperty("user.dir");
        if(root.endsWith(File.separator)) {
            root += File.separator;
        }
        File folder = new File(root + middleFolder);
        String fileName = String.format(OUT_FILE, currencyPair.getBaseCurrency(), currencyPair.getQuoteCurrency());
        File file = new File(folder + File.separator + fileName.toLowerCase());
        return file;
    }

    private void generateDataFile(List<TickerDTO> tickerList, File datafile) {
        FileOutputStream out = null;
        OutputStreamWriter outWriter = null;
        BufferedWriter bufWrite = null;
        try {
            if(tickerList.isEmpty()) {
                log.info("No file output");
                return;
            }
            if (!datafile.exists()) {
                datafile.createNewFile();
            }
            out = new FileOutputStream(datafile);
            outWriter = new OutputStreamWriter(out, "UTF-8");
            bufWrite = new BufferedWriter(outWriter);

            /*
            开始时间,开盘,收盘,最高,最低,成交量,成交额
             */
            for(TickerDTO ticker : tickerList) {
                String format =
                        //DateTimeUtil.formatDateFromUnix(ticker.getTimestamp().toInstant().toEpochMilli(), null) + "\t" +
                        ticker.getTimestamp().toEpochSecond() + "\t"
                                + ticker.getOpen() + "\t"
                                + ticker.getLast() + "\t"
                                + ticker.getHigh() + "\t"
                                + ticker.getLow() + "\t"
                                + ticker.getVolume() + "\t"
                                + ticker.getQuoteVolume() + "\n";

                //System.out.println(format);
                bufWrite.write(format);
            }
            log.info("Total {} tickers, File {} generated successfully", tickerList.size(), datafile.getName());
        } catch (Exception e) {
            log.error("Mount failed:"+ datafile.getAbsolutePath(), e.getMessage());
        } finally {
            try {
                if(bufWrite != null )
                    bufWrite.close();
                if(outWriter != null)
                    outWriter.close();
                if(out != null)
                    out.close();
            }catch (IOException e1) {
                log.error(e1.getMessage(), e1);
            }
        }
    }

    /**
     * Get recent history
     * @param currencyPair
     * @param durationMaximum
     * @return
     */
    public List<TickerDTO> getHistoryTickers(CurrencyPairDTO currencyPair, int durationMaximum) {
        try {
            CurrencyPair pair = new CurrencyPair(currencyPair.getBaseCurrency().getSymbol(), currencyPair.getQuoteCurrency().getSymbol());
            // We create the currency pairs parameter required by some exchanges.
            PeriodParams params = new DefaultCancelOrderByClientOrderIdParams(pair, duration.toMillis(), durationMaximum);

            List<TickerDTO> historyTickers = exchange.getMarketDataService().getKlines(params)
                    .stream()
                    .map(kline -> TickerDTO.builder()
                            .timestamp(ZonedDateTime.ofInstant((new Date(kline.getOpenTime())).toInstant(), ZoneId.systemDefault()))
                            .open(kline.getOpen())
                            .last(kline.getLast())
                            .high(kline.getHigh())
                            .low(kline.getLow())
                            .volume(kline.getVolume())
                            .quoteVolume(kline.getQuoteVolume()).build())
                    .peek(t -> log.debug(" - HisNew ticker: {}", t))
                    .collect(Collectors.toList());

            //Cache history tickers
            if(cacheHistoryTickers.containsKey(currencyPair)) {
                List<TickerDTO> newTickers = historyTickers.stream().filter(ticker -> !cacheHistoryTickers.get(currencyPair).contains(ticker)).collect(Collectors.toCollection(ArrayList::new));
                cacheHistoryTickers.get(currencyPair).addAll(newTickers);
            } else {
                cacheHistoryTickers.put(currencyPair, historyTickers);
            }
            return historyTickers;
        } catch (IOException e) {
            log.error("Error retrieving tickers: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

}
