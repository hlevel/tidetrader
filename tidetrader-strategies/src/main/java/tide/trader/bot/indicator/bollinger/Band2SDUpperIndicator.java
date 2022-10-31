package tide.trader.bot.indicator.bollinger;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.Num;

public class Band2SDUpperIndicator extends CachedIndicator<Num> {

    private final SMAIndicator sma;
    private final StandardDeviationIndicator stdev;
    private final double b2mult;

    public Band2SDUpperIndicator(Indicator<Num> indicator, double b2mult, int barCount) {
        super(indicator);
        this.sma = new SMAIndicator(indicator, barCount);
        this.stdev = new StandardDeviationIndicator(indicator, barCount);
        this.b2mult = b2mult;
    }

    @Override
    protected Num calculate(int i) {
        return sma.getValue(i).plus(this.numOf(b2mult).multipliedBy(stdev.getValue(i)));
    }
}
