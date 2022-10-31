package tide.trader.bot.indicator;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Down and up Percentage
 */
public class DownAndUpPercentageIndicator extends CachedIndicator<Num> {

    public DownAndUpPercentageIndicator(BarSeries series) {
        super(series);
    }

    protected Num calculate(int index) {
        if (index == 0) {
            return this.numOf(0);
        } else {
            Num firstBar = this.getBarSeries().getFirstBar().getClosePrice();
            Num currentBar = this.getBarSeries().getLastBar().getClosePrice();
            Num difference = currentBar.minus(firstBar);
            int percentage = difference.dividedBy(firstBar).multipliedBy(this.numOf(10000)).intValue();
            //System.out.println( currentBar+ "-" + firstBar + "=" + difference + "(" + this.numOf(percentage).dividedBy(this.numOf(100)) + ")");
            return this.numOf(percentage).dividedBy(this.numOf(100));
        }
    }
}
