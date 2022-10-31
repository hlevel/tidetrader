package tide.trader.bot.indicator.bollinger;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.TRIndicator;
import org.ta4j.core.num.Num;

public class KeltnerUpperIndicator extends CachedIndicator<Num> {

    private final EMAIndicator ema;
    private final EMAIndicator trEma;
    private final double kmult;

    public KeltnerUpperIndicator(Indicator<Num> indicator, double kmult, int barCount) {
        super(indicator);
        this.ema = new EMAIndicator(indicator, barCount);
        this.trEma = new EMAIndicator(new TRIndicator(indicator.getBarSeries()), barCount);
        this.kmult = kmult;
    }

    @Override
    protected Num calculate(int i) {
        return ema.getValue(i).plus(trEma.getValue(i).multipliedBy(this.numOf(kmult)));
    }
}
