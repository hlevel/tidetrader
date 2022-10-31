package tide.trader.bot.util.ta4j;

import lombok.Value;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import tide.trader.bot.dto.util.CurrencyPairDTO;
import tide.trader.bot.util.java.EqualsBuilder;
import tide.trader.bot.util.test.ExcludeFromCoverageGeneratedReport;

import java.time.Duration;

@Value
public class DurationMaximumBar {

    private final CurrencyPairDTO currencyPair;
    private final Duration duration;
    private final int maximumBarCount;

    public DurationMaximumBar(CurrencyPairDTO currencyPair, Duration duration, int maximumBarCount) {
        this.currencyPair = currencyPair;
        this.duration = duration;
        this.maximumBarCount = maximumBarCount;
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
        final DurationMaximumBar that = (DurationMaximumBar) o;
        return new EqualsBuilder()
                .append(this.currencyPair, that.currencyPair)
                .append(this.duration, that.duration)
                .append(this.maximumBarCount, that.maximumBarCount)
                .isEquals();
    }

    @Override
    @ExcludeFromCoverageGeneratedReport
    @SuppressWarnings("checkstyle:DesignForExtension")
    public int hashCode() {
        return new HashCodeBuilder()
                .append(currencyPair)
                .append(duration)
                .append(maximumBarCount)
                .toHashCode();
    }
}
