package tide.trader.bot.indicator;

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.Num;

public class NegativeStandardDeviationIndicator  extends CachedIndicator<Num> {
    private final StandardDeviationIndicator stdev;

    public NegativeStandardDeviationIndicator(StandardDeviationIndicator stdev) {
        super(stdev);
        this.stdev = stdev;
    }

    @Override
    protected Num calculate(int index) {
        return stdev.getValue(index).multipliedBy(this.numOf(-1));
    }

}
