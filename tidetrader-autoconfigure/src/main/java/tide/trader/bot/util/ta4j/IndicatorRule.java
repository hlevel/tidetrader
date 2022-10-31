package tide.trader.bot.util.ta4j;

import org.ta4j.core.Position;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;

/**
 * If the entry and exit do not meet the trading relationship,
 * it is necessary to distinguish three specific entry rules,
 * and clearly define the position direction of long, short and close positions
 */
public interface IndicatorRule {

    String getName();

    Rule getEntryLongRule();

    Rule getEntryShortRule();

    Rule getExitLongRule();

    Rule getExitShortRule();

    IndicatorRule and(IndicatorRule rule);

    IndicatorRule or(IndicatorRule rule);

    IndicatorRule and(String name, IndicatorRule rule, int unstablePeriod);

    IndicatorRule or(String name, IndicatorRule rule, int unstablePeriod);

    IndicatorRule opposite();

    void setUnstablePeriod(int unstablePeriod);

    int getUnstablePeriod();

    boolean isUnstableAt(int unstablePeriod);

    /*
    default boolean shouldOperate(int index, TradingRecord tradingRecord) {
        Position position = tradingRecord.getCurrentPosition();
        if (position.isNew()) {
            return this.shouldLongEnter(index, tradingRecord);
        } else {
            return position.isOpened() ? this.shouldExit(index, tradingRecord) : false;
        }
    }
    */

    default boolean shouldLongEnter(int index) {
        return this.shouldLongEnter(index, null);
    }

    default boolean shouldShortEnter(int index) {
        return this.shouldShortEnter(index, null);
    }

    default boolean shouldLongEnter(int index, TradingRecord tradingRecord) {
        return !this.isUnstableAt(index) && this.getEntryLongRule().isSatisfied(index, tradingRecord);
    }

    default boolean shouldShortEnter(int index, TradingRecord tradingRecord) {
        return !this.isUnstableAt(index) && this.getEntryShortRule().isSatisfied(index, tradingRecord);
    }

    default boolean shouldLongExit(int index) {
        return this.shouldLongExit(index, null);
    }

    default boolean shouldLongExit(int index, TradingRecord tradingRecord) {
        return !this.isUnstableAt(index) && this.getExitLongRule().isSatisfied(index, tradingRecord);
    }

    default boolean shouldShortExit(int index) {
        return this.shouldShortExit(index, null);
    }

    default boolean shouldShortExit(int index, TradingRecord tradingRecord) {
        return !this.isUnstableAt(index) && this.getExitShortRule().isSatisfied(index, tradingRecord);
    }

}
