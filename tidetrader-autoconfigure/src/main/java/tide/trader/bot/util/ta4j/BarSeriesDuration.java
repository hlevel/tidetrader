package tide.trader.bot.util.ta4j;

import lombok.Value;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.ta4j.core.BarSeries;
import tide.trader.bot.dto.util.CurrencyPairDTO;
import tide.trader.bot.util.java.EqualsBuilder;
import tide.trader.bot.util.test.ExcludeFromCoverageGeneratedReport;

import java.time.Duration;

@Value
public class BarSeriesDuration {

    private final BarSeries series;
    private final Duration duration;
    private final CurrencyPairDTO currencyPair;

    public BarSeriesDuration(BarSeries series, Duration duration, CurrencyPairDTO currencyPair) {
        this.series = series;
        this.duration = duration;
        this.currencyPair = currencyPair;
    }

    @Override
    @ExcludeFromCoverageGeneratedReport
    @SuppressWarnings("checkstyle:DesignForExtension")
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final BarSeriesDuration that = (BarSeriesDuration) o;
        return new EqualsBuilder()
                .append(this.duration, that.duration)
                .append(this.currencyPair, that.currencyPair)
                .isEquals();
    }

    @Override
    @ExcludeFromCoverageGeneratedReport
    @SuppressWarnings("checkstyle:DesignForExtension")
    public int hashCode() {
        return new HashCodeBuilder()
                .append(duration)
                .append(currencyPair)
                .toHashCode();
    }
}
