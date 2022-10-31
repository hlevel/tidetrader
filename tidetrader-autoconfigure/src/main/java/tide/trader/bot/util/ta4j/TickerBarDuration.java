package tide.trader.bot.util.ta4j;

import lombok.Value;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;
import tide.trader.bot.dto.account.BalanceDTO;
import tide.trader.bot.dto.market.TickerDTO;
import tide.trader.bot.util.java.EqualsBuilder;
import tide.trader.bot.util.test.ExcludeFromCoverageGeneratedReport;

import java.time.Duration;

@Value
public class TickerBarDuration {

    //private final BaseBar bar;
    private final DurationMaximumBar durationBar;
    private final TickerDTO durationTicker;
    private final TickerDTO lastTicker;

    public TickerBarDuration(DurationMaximumBar durationBar, TickerDTO durationTicker, TickerDTO lastTicker) {
        //this.bar = bar;
        this.durationBar = durationBar;
        this.durationTicker = durationTicker;
        this.lastTicker = lastTicker;
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
        final TickerBarDuration that = (TickerBarDuration) o;
        return new EqualsBuilder()
                .append(this.durationBar, that.durationBar)
                .append(this.durationTicker, that.durationTicker)
                .append(this.lastTicker, that.lastTicker)
                .isEquals();
    }

    @Override
    @ExcludeFromCoverageGeneratedReport
    @SuppressWarnings("checkstyle:DesignForExtension")
    public int hashCode() {
        return new HashCodeBuilder()
                .append(durationBar)
                .append(durationTicker)
                .append(lastTicker)
                .toHashCode();
    }
}
