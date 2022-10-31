package tide.trader.bot.strategy;

import org.ta4j.core.*;
import org.ta4j.core.num.DoubleNum;
import reactor.core.publisher.BaseSubscriber;
import tide.trader.bot.dto.market.TickerDTO;
import tide.trader.bot.dto.trade.SideDTO;
import tide.trader.bot.dto.util.ColumnsDTO;
import tide.trader.bot.dto.util.CurrencyPairDTO;
import tide.trader.bot.strategy.internal.CassandreStrategy;
import tide.trader.bot.util.java.ZonedDateTimeBetween;
import tide.trader.bot.util.ta4j.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
 * - CassandreStrategy (class) is the class that every strategy used by user ({@link BasicCassandreStrategy} or {@link BasicTa4jCassandreStrategy}) must extend. It contains methods to access data and manage orders, trades, positions.
 * - CassandreStrategy (interface) is the annotation allowing you Cassandre to recognize a user strategy.
 * - BasicCassandreStrategy - User inherits this class this one to make a basic strategy.
 * - BasicCassandreStrategy - User inherits this class this one to make a strategy with ta4j.
 */
@SuppressWarnings("unused")
public abstract class BasicTa4jCassandreStrategy extends CassandreStrategy {

    /** Ta4j Indicator Rule. */
    private final Map<DurationMaximumBar, IndicatorRule> rules = new ConcurrentHashMap<>();

    /** Series. */
    private final Map<DurationMaximumBar, BarSeriesDuration> barSeries = new ConcurrentHashMap<>();

    /** The bar aggregator. */
    private final Map<DurationMaximumBar, BarAggregator> barAggregators = new ConcurrentHashMap<>();

    /** Last Duration tickers received. */
    private final Map<DurationMaximumBar, TickerDTO> lastDurationTickers = new LinkedHashMap<>();

    /** Duration tickers received. */
    private final Map<DurationMaximumBar, TickerDTO> durationTickers = new ConcurrentHashMap<>();

    /**
     * Implements this method to set the time that should separate two bars.
     *
     * @return temporal amount
     */
    public abstract Set<DurationMaximumBar> getRequestedDurationMaximumBars();

    /**
     * Implements this method to tell the bot which strategy to apply.
     *
     * @return strategy
     */
    public abstract IndicatorRule getIndicatorRule(DurationMaximumBar bar);

    @Override
    public final Set<CurrencyPairDTO> getRequestedCurrencyPairs() {
        // We only support one currency pair with BasicTa4jCassandreStrategy.
        return getRequestedDurationMaximumBars().stream().map(DurationMaximumBar::getCurrencyPair).distinct().collect(Collectors.toSet());
    }

    @Override
    public final void initialize() {
        // init parameters.
        this.initializeParameters(getConfiguration().getParameters());
        this.getRequestedDurationMaximumBars().forEach(this::initializationBarSeries);
        this.getRequestedDurationMaximumBars().forEach(this::initializationIndicatorRule);
        // Build the series.
        this.getRequestedDurationMaximumBars().forEach(bar -> {
            if(isShort()) dependencies.getTradeService().setLeverage(bar.getCurrencyPair(), Integer.parseInt(getConfiguration().getLeverage()));
        });
    }

    /**
     * Implements duration bar count
     * @param bar
     * @return
     */
    private void initializationBarSeries(DurationMaximumBar bar) {
        String withName = bar.getCurrencyPair().toString() + "_" + bar.getDuration().toString();

        // Build the series.
        BarSeries series = new BaseBarSeriesBuilder().withNumTypeOf(DoubleNum.class).withName(withName).build();
        series.setMaximumBarCount(bar.getMaximumBarCount());
        BarSeriesDuration barSeriesDuration = new BarSeriesDuration(series, bar.getDuration(), bar.getCurrencyPair());
        barSeries.put(bar, barSeriesDuration);

        // Build the aggregator
        BarAggregator barAggregator = new DurationBarAggregator(bar);
        final AggregatedBarSubscriber barSubscriber = new AggregatedBarSubscriber(this::addBars);
        barSubscriber.request(1);
        barAggregator.getBarFlux().subscribe(barSubscriber);
        barAggregators.put(bar, barAggregator);

    }

    /**
     * Implements duration bar count
     * @param bar
     * @return
     */
    private void initializationIndicatorRule(DurationMaximumBar bar) {
        // Build the Indicator Rule
        IndicatorRule rule = this.getIndicatorRule(bar);
        rules.put(bar, rule);

        //Initialize the online environment
        dependencies.getMarketService()
                .getHistoryTickers(bar.getCurrencyPair(), bar.getDuration(), ZonedDateTimeBetween.ofDuration(bar.getDuration(), bar.getMaximumBarCount()))
                .forEach(ticker -> {
                    BarSeries series = getSeries(bar);
                    series.addBar(new BaseBar(bar.getDuration(), ticker.getTimestamp().plus(bar.getDuration()), ticker.getOpen().doubleValue(),
                            ticker.getHigh().doubleValue(), ticker.getLow().doubleValue(), ticker.getLast().doubleValue(), ticker.getVolume().doubleValue()));
                    int endIndex = series.getEndIndex();
                    //Rule Execution
                    rule.shouldLongEnter(endIndex);
                    rule.shouldLongExit(endIndex);
                    rule.shouldShortEnter(endIndex);
                    rule.shouldShortEnter(endIndex);
                });

    }

    /**
     * Implements this method to tell the bot which BarSeries to apply.
     * @param bar
     * @return
     */
    public final BarSeries getSeries(DurationMaximumBar bar) {
        return barSeries.get(bar).getSeries();
    }

    @Override
    public final void tickersUpdates(final Set<TickerDTO> tickers) {
        try{
            // We only retrieve the ticker requested by the strategy (only one because it's a ta4j strategy).
            final Map<CurrencyPairDTO, TickerDTO> tickersUpdates = tickers.stream()
                    .filter(ticker -> getRequestedCurrencyPairs().contains(ticker.getCurrencyPair()))
                    .collect(Collectors.toMap(TickerDTO::getCurrencyPair, Function.identity()));

            tickersUpdates.values().forEach(ticker -> {
                getLastTickers().put(ticker.getCurrencyPair(), ticker);
                //Check for missing calls
                this.getRequestedDurationMaximumBars()
                        .stream()
                        .filter(dmb -> dmb.getCurrencyPair().equals(ticker.getCurrencyPair()))
                        .forEach(dmb -> {
                            //Update data addition process
                            BarSeries series = getSeries(dmb);
                            BarAggregator barAggregator = barAggregators.get(dmb);

                            if (barAggregator.getBarContext() == null && series.getEndIndex() > -1) {
                                barAggregator.update(series.getLastBar().getEndTime(), ticker);
                            } else {
                                //System.out.println("1." + ticker.getTimestamp().toEpochSecond()  + "-" + ticker.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "," + ticker.getOpen() + "," + ticker.getLast() + "," + ticker.getHigh() + "," + ticker.getLow() + "," + ticker.getVolume());

                                barAggregator.update(ticker.getTimestamp(), ticker);
                                //Check for missing calls
                                if(getLastDurationTickers().containsKey(dmb)) {
                                    ZonedDateTime last = getLastDurationTickers().get(dmb).getTimestamp().plus(dmb.getDuration());
                                    if(last.isBefore(barAggregator.getBarContext().getEndTime())) {
                                        logger.debug("Compensation trigger addBarAndCallRule {} and {} durationTicker={}", last.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), barAggregator.getBarContext().getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), getLastDurationTickers().get(dmb));
                                        this.addBars(new TickerBarDuration(dmb, null, ticker));
                                    }
                                }
                            }

                        });
            });
            // We update the positions with tickers.
            updatePositionsWithTickersUpdates(tickersUpdates);

            // We update the rule with duration tickers.
            getRequestedDurationMaximumBars().stream().filter(bar -> durationTickers.containsKey(bar)).peek(bar -> callRule(getSeries(bar), bar, durationTickers.get(bar))).peek(bar -> lastDurationTickers.put(bar, durationTickers.get(bar))).forEach(bar -> durationTickers.remove(bar));

            onTickersUpdates(tickersUpdates);
        }catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Add new candles and calculate indicators at the same time
     * @param barDuration
     * @return
     */
    private TickerBarDuration addBars(final TickerBarDuration barDuration) {
        try{
            BarSeries series = this.getSeries(barDuration.getDurationBar());
            TickerDTO durationTicker = barDuration.getDurationTicker();

            //The simulated environment can get local data normally
            if(this.isSimulated()) {
                //combination Bar
                if(durationTicker != null) {
                    series.addBar(new BaseBar(barDuration.getDurationBar().getDuration(),
                            durationTicker.getTimestamp(),
                            durationTicker.getOpen().doubleValue(),
                            durationTicker.getHigh().doubleValue(),
                            durationTicker.getLow().doubleValue(),
                            durationTicker.getLast().doubleValue(),
                            durationTicker.getVolume().doubleValue()));

                    //System.out.println("4." + series.getLastBar().getBeginTime().toEpochSecond()  + "-" + series.getLastBar().getEndTime().toEpochSecond() + "," + series.getLastBar().getBeginTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))  + "-" + series.getLastBar().getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "," + series.getLastBar().getOpenPrice() + "," + series.getLastBar().getClosePrice() + "," + series.getLastBar().getHighPrice() + "," + series.getLastBar().getLowPrice() + "," + series.getLastBar().getVolume());
                }
            } else {
                //Every candle in the online environment comes from the historical data of the exchange to prevent
                //Get the recent candle lines of the exchange
                durationTicker = dependencies.getMarketService().getHistoryTickers(barDuration.getDurationBar().getCurrencyPair(), barDuration.getDurationBar().getDuration(),
                        ZonedDateTimeBetween.ofStartDuration(barDuration.getDurationBar().getDuration(), series.getLastBar().getEndTime()))
                        .stream()
                        .filter(ticker -> series.getLastBar().getBeginTime().isBefore(ticker.getTimestamp()))
                        .peek(ticker -> logger.debug("New Bar:[{},{},{},{},{},{}], Lastbar:[{},{},{},{},{},{}]", ticker.getTimestamp().toEpochSecond(), ticker.getOpen(), ticker.getLast(), ticker.getHigh(), ticker.getLow(), ticker.getVolume(), series.getLastBar().getBeginTime().toEpochSecond(), series.getLastBar().getOpenPrice(), series.getLastBar().getClosePrice(), series.getLastBar().getHighPrice(), series.getLastBar().getLowPrice(), series.getLastBar().getVolume()))
                        .peek(ticker -> series.addBar(new BaseBar(barDuration.getDurationBar().getDuration(),
                                ticker.getTimestamp().plus(barDuration.getDurationBar().getDuration()),
                                ticker.getOpen().doubleValue(),
                                ticker.getHigh().doubleValue(),
                                ticker.getLow().doubleValue(),
                                ticker.getLast().doubleValue(),
                                ticker.getVolume().doubleValue()))
                        )
                        .reduce((first, second) -> second)
                        .map(ticker -> new BarContext(barDuration.getDurationBar().getDuration(), ticker).getDurationTicker()).orElse(null);
            }
            Optional.ofNullable(durationTicker).ifPresent(ticker -> durationTickers.put(barDuration.getDurationBar(), ticker));
        }catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return barDuration;
    }

    /**
     * Automatically call according to the time period, and support manual call calculation
     * @param series
     * @param durationMaximumBar
     * @param durationTicker
     */
    protected final void callRule(final BarSeries series, DurationMaximumBar durationMaximumBar, TickerDTO durationTicker) {

        //Do not implement the policy if the bar is not full
        if(series.getBarCount() == series.getMaximumBarCount()) {
            logger.debug("callRule {} now={}", durationTicker.getTimestamp().toEpochSecond(), durationTicker);
            int endIndex = series.getEndIndex();
            IndicatorRule rule = rules.get(durationMaximumBar);
            if (rule.shouldLongEnter(endIndex)) {
                // Our strategy should enter long.
                shouldPosition(SideDTO.LONG, durationTicker);
            }

            if (rule.shouldLongExit(endIndex)) {
                // Our strategy should long exit.
                shouldPosition(SideDTO.EXITLONG, durationTicker);
            }

            //Need to configure bearish
            if(isShort()) {
                if (rule.shouldShortEnter(endIndex)) {
                    // Our strategy should enter short.
                    shouldPosition(SideDTO.SHORT, durationTicker);
                }

                if (rule.shouldShortExit(endIndex)) {
                    // Our strategy should short exit.
                    shouldPosition(SideDTO.EXITSHORT, durationTicker);
                }
            }
            logger.debug("callRule {} end={}", durationTicker.getTimestamp().toEpochSecond(), durationTicker);
        }
    }

    /**
     * Return Duration last received tickers.
     * @return ticker
     */
    public final Map<DurationMaximumBar, TickerDTO> getLastDurationTickers() {
        return lastDurationTickers;
    }

    /**
     * Called when your strategy think you should Position.
     * @param side
     * @param durationTicker
     */
    public abstract void shouldPosition(SideDTO side, TickerDTO durationTicker);

    /**
     * Get real-time Ticker
     * @return
     */
    @Override
    public ColumnsDTO getColumnTickers() {
        //Market data
        ColumnsDTO tickerColumns = new ColumnsDTO("Latest Tickers");
        tickerColumns.setColName("CurrencyPair", "Duration", "Open", "Last", "High", "Low", "Time", "Action");
        this.getLastDurationTickers().forEach((k, v) -> {
            Optional<DurationMaximumBar> bar = this.getRequestedDurationMaximumBars().stream().filter(dmb -> dmb.equals(k)).findFirst();
            String chart = "<a href='/strategy/chart/" + configuration.getStrategyDTO().getStrategyId() + "/" + k.getDuration() + "/"+ v.getCurrencyPair().getBaseCurrency() + "_" + v.getCurrencyPair().getQuoteCurrency() + "' target='_blank' ><i class='fas fa-chart-line'></i></a>";
            tickerColumns.addRow(
                    v.getCurrencyPair().toString(),
                    bar.isPresent() ? bar.get().getDuration().toString() : "",
                    v.getOpen().stripTrailingZeros(),
                    v.getLast().stripTrailingZeros(),
                    v.getHigh().stripTrailingZeros(),
                    v.getLow().stripTrailingZeros(),
                    v.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    chart);
        });
        return tickerColumns;
    }

    /**
     * Subscriber to the Bar series.
     */
    private static class AggregatedBarSubscriber extends BaseSubscriber<TickerBarDuration> {

        /**
         * The function to be called when the next bar arrives.
         */
        private final Function<TickerBarDuration, TickerBarDuration> theNextFunction;

        /*
        AggregatedBarSubscriber(final Function<Bar, Bar> onNextFunction) {
            this.theNextFunction = onNextFunction;
        }*/

        AggregatedBarSubscriber(final Function<TickerBarDuration, TickerBarDuration> onNextFunction) {
            this.theNextFunction = onNextFunction;
        }

        /**
         * Invoke the given function and ask for next bar.
         *
         * @param value the bar value
         */
        @Override
        protected void hookOnNext(final TickerBarDuration value) {
            super.hookOnNext(value);
            theNextFunction.apply(value);
            request(1);
        }
    }

}
