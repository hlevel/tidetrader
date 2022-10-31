package tide.trader.bot.indicator.bollinger;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.num.Num;
import tide.trader.bot.indicator.NumLinkedHashMap;
import tide.trader.bot.util.BigDecimalUtil;

import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

/**
 * Calculate the value of kdj line
 */
public class BcwsmaIndicator extends CachedIndicator<Num> {

    private final CachedIndicator<Num> indicator;
    private final int l;
    private final int m;
    private final NumLinkedHashMap<Integer, Num> previousBcwsma;

    public BcwsmaIndicator(CachedIndicator<Num> indicator, int l, int m) {
        super(indicator);
        this.indicator = indicator;
        this.l = l;
        this.m = m;
        this.previousBcwsma = new NumLinkedHashMap<>(l);
    }

    @Override
    protected Num calculate(int index) {
        //Bar bar = this.getBarSeries().getBar(index);

        Num bcwsma = previousBcwsma.computeIfAbsent((index-1), key -> this.numOf(0));

        Num _s = this.indicator.getValue(index);
        Num _l = this.numOf(l);
        Num _m = this.numOf(m);
        Num _bcwsma = _m.multipliedBy(_s).plus((_l.minus(_m)).multipliedBy(bcwsma)).dividedBy(_l);
        //System.out.println(bar.getBeginTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "," + bar.getBeginTime().toEpochSecond() +"," + bar.getOpenPrice() + "," + indicator + "[" + index + "]k="  + _s + ",previousBcwsma=" + bcwsma + ",bcwsma=" + _bcwsma  +  ",l=" + l + ",m="+ m +  ",previousList=" +previousBcwsma);
        previousBcwsma.put(index, _bcwsma);
        return _bcwsma;
    }

}
