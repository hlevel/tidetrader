package tide.trader.bot.util.ta4j;

import org.ta4j.core.Bar;
import reactor.core.publisher.Flux;
import tide.trader.bot.dto.market.TickerDTO;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * Component to aggregate bars and provide a Flux of {@link Bar}.
 */
public interface BarAggregator {

    /**
     * Updates the dar data.
     * @param timestamp time of the tick
     * @param ticker latest ticker
     */
    void update(ZonedDateTime timestamp, TickerDTO ticker);

    /**
     * Gets the {@link Flux}.
     * @return flux of Bars
     */
    Flux<TickerBarDuration> getBarFlux();

    /**
     * Gets the {@link Duration}.
     * @return Duration of Bars
     */
    DurationMaximumBar getDurationMaximumBar();

    /**
     * Gets Ticker aggregator {@link BarContext}.
     * @return
     */
    BarContext getBarContext();
}
