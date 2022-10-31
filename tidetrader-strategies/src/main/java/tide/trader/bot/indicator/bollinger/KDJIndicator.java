package tide.trader.bot.indicator.bollinger;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.num.Num;
import tide.trader.bot.indicator.NumLinkedHashMap;

import java.time.format.DateTimeFormatter;

public class KDJIndicator extends CachedIndicator<Num> {

    private final BcwsmaIndicator bcwsmaK;
    private final BcwsmaIndicator bcwsmaD;
    private final StochasticOscillatorKIndicator stochasticOscillatorK;
    private final StochasticOscillatorDIndicator stochasticOscillatorD;
    private final NumLinkedHashMap<Integer, Num> previousJ;

    public KDJIndicator(BarSeries series, int shortBarCount, int longBarCount) {
        super(series);
        this.stochasticOscillatorK = new StochasticOscillatorKIndicator(series, longBarCount);
        this.stochasticOscillatorD = new StochasticOscillatorDIndicator(this.stochasticOscillatorK);

        this.bcwsmaK = new BcwsmaIndicator(new StochasticOscillatorKIndicator(series, longBarCount), shortBarCount, 1);
        this.bcwsmaD = new BcwsmaIndicator(bcwsmaK, shortBarCount, 1);
        this.previousJ = new NumLinkedHashMap<>(shortBarCount);
    }

    @Override
    protected Num calculate(int index) {
        Bar bar = this.getBarSeries().getBar(index);

        Num _pk = this.bcwsmaK.getValue(index);
        Num _pd = this.bcwsmaD.getValue(index);
        Num _pj = this.numOf(3).multipliedBy(_pk).minus(this.numOf(2).multipliedBy(_pd));

        //Num k = this.stochasticOscillatorK.getValue(index);
        //Num d = this.stochasticOscillatorD.getValue(index);
        //Num j = (this.numOf(3).multipliedBy(k)).minus(this.numOf(2).multipliedBy(d));
        //System.out.println(bar.getBeginTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "," + bar.getBeginTime().toEpochSecond() +"," + bar.getOpenPrice() + ",[" + index + "]k="  + k + ",d=" + d + ",j=" + j);

        previousJ.putIfAbsent(index, _pj);
        return _pj;
    }

    public Num getJ(int index) {
        return previousJ.containsKey(index) ? previousJ.get(index) : this.getValue(index);
    }

}
