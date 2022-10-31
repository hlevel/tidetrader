package tide.trader.bot.util.ta4j;

import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import tide.trader.bot.dto.market.TickerDTO;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implementation of the {@link BarAggregator} based on {@link Duration}.
 */
public class DurationBarAggregator implements BarAggregator {

    /** Duration. */
    private final DurationMaximumBar duration;

    /** The bar context. */
    private BarContext ctx;

    /** The processor. */
    private final DirectProcessor<TickerBarDuration> processor;

    /** The sink. */
    private final FluxSink<TickerBarDuration> sink;

    /** Highest price. */
    private Number highest = 0;

    /** Lowest price. */
    private Number lowest = 0;

    /**
     * Creates the Aggregator with the given {@link Duration}.
     *
     * @param duration the DurationMaximumBar
     */
    public DurationBarAggregator(final DurationMaximumBar duration) {
        this.duration = duration;
        this.processor = DirectProcessor.create();
        this.sink = processor.sink();
    }


    /**
     * Updates the bar data.
     *
     * @param timestamp   time of the tick
     * @param ticker latest price
     */

    @Override
    public void update(final ZonedDateTime timestamp, final TickerDTO ticker) {
        calculateHighestLowest(ticker.getLast());
        if (ctx == null) {
            ctx = new BarContext(duration.getDuration(), duration.getCurrencyPair(), timestamp, ticker.getLow(), ticker.getHigh(), ticker.getOpen(), ticker.getLast(), ticker.getVolume());
            //Initialization requires calling
            //System.out.println("2." + ctx.getDurationTicker().getTimestamp().toEpochSecond()  + "-" + ctx.getDurationTicker().getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "," + ctx.getDurationTicker().getOpen() + "," + ctx.getDurationTicker().getLast() + "," + ctx.getDurationTicker().getHigh() + "," + ctx.getDurationTicker().getLow() + "," + ctx.getDurationTicker().getVolume());
            sink.next(new TickerBarDuration(duration, ctx.getDurationTicker(), ticker));
        } else if (ctx.isAfter(timestamp)) {
            //BaseBar baseBar = new BaseBar(duration.getBarDuration(), ctx.getEndTime(), ctx.getOpen(), ctx.getHigh(), ctx.getLow(), ctx.getClose(), ctx.getVolume());
            // take the close and start counting new context
            ctx = new BarContext(duration.getDuration(), duration.getCurrencyPair(), ctx.getEndTime(), ticker.getLow(), ticker.getHigh(), ticker.getOpen(), ticker.getLast(), ticker.getVolume());
            //System.out.println("3." + ctx.getDurationTicker().getTimestamp().toEpochSecond()  + "-" + ctx.getDurationTicker().getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "," + ctx.getDurationTicker().getOpen() + "," + ctx.getDurationTicker().getLast() + "," + ctx.getDurationTicker().getHigh() + "," + ctx.getDurationTicker().getLow() + "," + ctx.getDurationTicker().getVolume());

            // we have new bar starting - emit current ctx
            sink.next(new TickerBarDuration(duration, ctx.getDurationTicker(), ticker));
            highest = ticker.getLast();
            lowest = ticker.getLast();
        } else {
            //System.out.println("else updateTicker=" + ticker);
            ctx.update(lowest, highest, ticker.getLast(), ticker.getVolume());
        }
    }

    /**
     * Gets the {@link Flux}.
     *
     * @return flux of Bars
     */
    @Override
    public Flux<TickerBarDuration> getBarFlux() {
        return processor;
    }

    @Override
    public DurationMaximumBar getDurationMaximumBar() {
        return duration;
    }

    @Override
    public BarContext getBarContext() {
        return ctx;
    }

    /**
     * Calculate highest and lowest.
     *
     * @param latestPrice latest price
     */
    private void calculateHighestLowest(final Number latestPrice) {
        if (lowest.doubleValue() == 0D) {
            lowest = latestPrice;
            highest = latestPrice;
        } else {
            lowest = Math.min(lowest.doubleValue(), latestPrice.doubleValue());
            highest = Math.max(highest.doubleValue(), latestPrice.doubleValue());
        }
    }
}
