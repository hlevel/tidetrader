package tide.trader.bot.strategy;

import tide.trader.bot.domain.Signal;
import tide.trader.bot.dto.market.TickerDTO;
import tide.trader.bot.dto.trade.SignalDTO;
import tide.trader.bot.dto.trade.SignalStatusDTO;
import tide.trader.bot.dto.trade.SideDTO;
import tide.trader.bot.dto.util.CurrencyPairDTO;
import tide.trader.bot.strategy.internal.CassandreStrategy;
import tide.trader.bot.util.base.Base;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * BasicCassandreStrategy - User inherits this class this one to make a strategy with ta4j.
 * <p>
 * These are the classes used to manage a position.
 * - CassandreStrategyInterface list the methods a strategy type must implement to be able to interact with the Cassandre framework.
 * - CassandreStrategyConfiguration contains the configuration of the strategy.
 * - CassandreStrategyDependencies contains all the dependencies required by a strategy and provided by the Cassandre framework.
 * - CassandreStrategyImplementation is the default implementation of CassandreStrategyInterface, this code manages the interaction between Cassandre framework and a strategy.
 * - CassandreStrategy (class) is the class that every strategy used by user ({@link BasicCassandreStrategy} or {@link BasicSingalCassandreStrategy}) must extend. It contains methods to access data and manage orders, trades, positions.
 * - CassandreStrategy (interface) is the annotation allowing you Cassandre to recognize a user strategy.
 * - BasicSingalCassandreStrategy - User inherits this class this one to make a basic strategy.
 * - BasicSingalCassandreStrategy - User inherits this class this one to make a strategy with ta4j.
 */
@SuppressWarnings("unused")
public abstract class BasicSingalCassandreStrategy extends CassandreStrategy {

    @Override
    public final void initialize() {
        this.initializeParameters(getConfiguration().getParameters());
    }

    @Override
    public final void tickersUpdates(final Set<TickerDTO> tickers) {
        synchronized (this) {
            // We only retrieve the tickers requested by the strategy (in real time).
            final Set<CurrencyPairDTO> requestedCurrencyPairs = getRequestedCurrencyPairs();

            // We build the results.
            final Map<CurrencyPairDTO, TickerDTO> tickersUpdates = tickers.stream()
                    .filter(tickerDTO -> requestedCurrencyPairs.contains(tickerDTO.getCurrencyPair()))
                    // We also update the values of the last tickers received by the strategy.
                    .peek(tickerDTO -> lastTickers.put(tickerDTO.getCurrencyPair(), tickerDTO))
                    .collect(Collectors.toMap(TickerDTO::getCurrencyPair, Function.identity(), (id, value) -> id, LinkedHashMap::new));

            tickersUpdates.values().forEach(ticker -> {
                //Get the first signal in chronological order
                Optional<SignalDTO> singalGet = this.getSequenceSingal(ticker.getCurrencyPair());
                if (singalGet.isPresent()) {
                    SignalDTO signal = singalGet.get();
                    boolean should = false;
                    switch (signal.getSide()) {
                        case LONG:
                        case SHORT:
                            should = this.shouldEnter(signal.getCurrencyPair(), signal.getSide(), signal.getPriceValue());
                            break;
                        case FLAT:
                            should = this.shouldExit(signal.getCurrencyPair(), signal.getPriceValue());
                            break;
                    }
                    if (should) {
                        this.dependencies.getSignalRepository().updateStatus(signal.getUid(), SignalStatusDTO.EXPIRED);
                    }
                }
            });

            // We update the positions with tickers.
            updatePositionsWithTickersUpdates(tickersUpdates);

            onTickersUpdates(tickersUpdates);
        }
    }

    protected Optional<SignalDTO> getSequenceSingal(CurrencyPairDTO currencyPair){
        try {
            this.dependencies.getSignalRepository().findAll().forEach(signal -> {
                System.out.println(signal);
            });
            Optional<Signal> signalGet = this.dependencies.getSignalRepository().findFirstByStrategyStrategyIdAndCurrencyPairAndStatusOrderByCreatedOnAsc(this.getConfiguration().getStrategyDTO().getStrategyId(), currencyPair.toString(), SignalStatusDTO.ACTIVE);
            if (signalGet.isPresent()) {
                Signal signal = signalGet.get();
                //Judge whether the expiration time is less than the current time
                if (signal.getCreatedOn().plusSeconds(this.configuration.getExpireSec()).isBefore(ZonedDateTime.now())) {
                    this.dependencies.getSignalRepository().updateStatus(signal.getUid(), SignalStatusDTO.EXPIRED);
                }
                return Optional.of(Base.TRADE_MAPPER.mapToSignalDTO(signal));
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }


    /**
     * Called when your strategy think you should enter.
     */
    public abstract boolean shouldEnter(CurrencyPairDTO currencyPair, SideDTO type, BigDecimal price);

    /**
     * Called when your strategy think you should exit.
     */
    public abstract boolean shouldExit(CurrencyPairDTO currencyPair, BigDecimal price);

    // =================================================================================================================

}
