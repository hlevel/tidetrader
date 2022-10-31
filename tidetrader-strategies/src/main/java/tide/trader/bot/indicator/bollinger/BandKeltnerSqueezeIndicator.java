package tide.trader.bot.indicator.bollinger;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

import java.time.format.DateTimeFormatter;

/**
 * Squeeze
 */
public class BandKeltnerSqueezeIndicator extends CachedIndicator<Num> {

    private final Band2SDUpperIndicator band2SDUpper;
    private final Band2SDLowerIndicator band2SDLower;
    private final KeltnerUpperIndicator keltnerUpper;
    private final KeltnerLowerIndicator keltnerLower;
    private final ClosePriceIndicator closePrice;

    public BandKeltnerSqueezeIndicator(BarSeries series, int barCount) {
        super(series);
        this.closePrice = new ClosePriceIndicator(series);
        //2 Standard Deviation Bollinger Bands
        this.band2SDUpper = new Band2SDUpperIndicator(closePrice, 2.0, barCount);
        this.band2SDLower = new Band2SDLowerIndicator(closePrice, 2.0, barCount);

        //1.5 Keltner channels
        this.keltnerUpper = new KeltnerUpperIndicator(closePrice, 1.5, barCount);
        this.keltnerLower = new KeltnerLowerIndicator(closePrice, 1.5, barCount);
    }

    @Override
    protected Num calculate(int index) {
        boolean band2sd = band2SDUpper.getValue(index).isLessThanOrEqual(keltnerUpper.getValue(index));
        boolean keltner = band2SDLower.getValue(index).isGreaterThanOrEqual(keltnerLower.getValue(index));
        return band2sd && keltner ? this.numOf(1) : this.numOf(0);
    }

    /**
     * 是否超过上轨线
     * @param index
     * @return
     */
    public boolean isOverBand2SDUpper(int index) {
        return band2SDUpper.getValue(index).isLessThan(closePrice.getValue(index));
    }

    /**
     * 是否未到下轨线
     * @param index
     * @return
     */
    public boolean isUnderBand2SDLower(int index) {
        return band2SDLower.getValue(index).isGreaterThan(closePrice.getValue(index));
    }

}
