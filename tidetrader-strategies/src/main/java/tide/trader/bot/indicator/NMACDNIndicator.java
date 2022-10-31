package tide.trader.bot.indicator;

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.num.Num;

public class NMACDNIndicator extends CachedIndicator<Num> {

    private final NMACDIndicator nmacd;
    private final LowestValueIndicator lowest;
    private final HighestValueIndicator highest;

    public NMACDNIndicator(NMACDIndicator nmacd, int lowestHigestBarCount) {
        super(nmacd);
        this.nmacd = nmacd;
        this.lowest = new LowestValueIndicator(this.nmacd, lowestHigestBarCount);
        this.highest = new HighestValueIndicator(this.nmacd, lowestHigestBarCount);
    }

    @Override
    protected Num calculate(int index) {
        Num lo = nmacd.getValue(index).minus(lowest.getValue(index));
        Num hg = highest.getValue(index).minus(lowest.getValue(index)).plus(this.numOf(0.000001));
        Num macNorm = lo.dividedBy(hg).multipliedBy(this.numOf(2)).minus(this.numOf(1));
        return macNorm;
    }
}
