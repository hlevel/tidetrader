package tide.trader.bot.indicator;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.num.Num;

/**
 *
 */
public class NMACDIndicator extends CachedIndicator<Num> {

    private final EMAIndicator shortTermEma;
    private final EMAIndicator longTermEma;

    public NMACDIndicator(Indicator<Num> indicator, int shortBarCount, int longBarCount) {
        super(indicator);
        if (shortBarCount > longBarCount) {
            throw new IllegalArgumentException("Long term period count must be greater than short term period count");
        } else {
            this.shortTermEma = new EMAIndicator(indicator, shortBarCount);
            this.longTermEma = new EMAIndicator(indicator, longBarCount);
        }
    }

    @Override
    protected Num calculate(int index) {
        Num minEMA = shortTermEma.getValue(index).min(longTermEma.getValue(index));
        Num maxEMA =  shortTermEma.getValue(index).max(longTermEma.getValue(index));
        Num ratio = minEMA.dividedBy(maxEMA);
        Num mac = (shortTermEma.getValue(index).isGreaterThan(longTermEma.getValue(index)) ? this.numOf(2).minus(ratio) : ratio);
        return mac.minus(this.numOf(1));
    }

}
