package tide.trader.bot.indicator;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.BooleanTransformIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.num.Num;

public class BooleanTransform2Indicator extends CachedIndicator<Boolean> {

    private final Indicator<Num> first;
    private final Indicator<Num> second;
    private final BooleanTransformIndicator.BooleanTransformType type;

    public BooleanTransform2Indicator(Indicator<Num> first, Indicator<Num> second, BooleanTransformIndicator.BooleanTransformType type) {
        super(first);
        this.first = first;
        this.second = second;
        this.type = type;
    }

    public BooleanTransform2Indicator(Indicator<Num> first, Num threshold, BooleanTransformIndicator.BooleanTransformType type) {
        this(first, (new ConstantIndicator(first.getBarSeries(), threshold)), type);
    }

    @Override
    protected Boolean calculate(int index) {
        Num firstVal = this.first.getValue(index);
        Num secondVal = this.second.getValue(index);
        //System.out.println("BooleanFormIndicator[" + firstVal + this.type + secondVal + "]");
        switch(this.type) {
            case equals:
                return firstVal.equals(secondVal);
            case isGreaterThan:
                return firstVal.isGreaterThan(secondVal);
            case isGreaterThanOrEqual:
                return firstVal.isGreaterThanOrEqual(secondVal);
            case isLessThan:
                return firstVal.isLessThan(secondVal);
            case isLessThanOrEqual:
                return firstVal.isLessThanOrEqual(secondVal);
        }
        return false;
    }

}
