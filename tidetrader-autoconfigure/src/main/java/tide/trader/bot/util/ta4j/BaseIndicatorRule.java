package tide.trader.bot.util.ta4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;

/**
 * Base implementation of a {@link IndicatorRule}.
 */
public class BaseIndicatorRule implements IndicatorRule {
    /** The logger */
    protected final transient Logger log = LoggerFactory.getLogger(getClass());;

    /** The class name */
    private final String className = getClass().getSimpleName();

    /** Name of the strategy */
    private String name;

    /** The entry long rule */
    private Rule entryLongRule;

    /** The entry short rule */
    private Rule entryShortRule;

    /** The exit rule */
    private Rule exitLongRule;

    /** The exit rule */
    private Rule exitShortRule;

    /**
     * The unstable period (number of bars).<br>
     * During the unstable period of the strategy any trade placement will be
     * cancelled.<br>
     * I.e. no entry/exit signal will be fired before index == unstablePeriod.
     */
    private int unstablePeriod;

    public BaseIndicatorRule(Rule entryLongRule, Rule exitLongRule) {
        this(null, entryLongRule, null, exitLongRule, null, 0);
    }

    public BaseIndicatorRule(Rule entryLongRule, Rule entryShortRule, Rule exitLongRule, Rule exitShortRule) {
        this(null, entryLongRule, entryShortRule, exitLongRule, exitShortRule, 0);
    }

    public BaseIndicatorRule(Rule entryLongRule, Rule entryShortRule, Rule exitLongRule, Rule exitShortRule, int unstablePeriod) {
        this(null, entryLongRule, entryShortRule, exitLongRule, exitShortRule, unstablePeriod);
    }

    public BaseIndicatorRule(String name, Rule entryLongRule, Rule entryShortRule, Rule exitLongRule, Rule exitShortRule) {
        this(name, entryLongRule, entryShortRule, exitLongRule, exitShortRule, 0);
    }

    /**
     * Constructor.
     *
     * @param name           the name of the strategy
     * @param entryLongRule  the entry long rule
     * @param entryShortRule the entry short rule
     * @param exitLongRule   the exit long rule
     * @param exitShortRule  the exit short rule
     * @param unstablePeriod strategy will ignore possible signals at
     *                       <code>index</code> < <code>unstablePeriod</code>
     */
    public BaseIndicatorRule(String name, Rule entryLongRule, Rule entryShortRule, Rule exitLongRule, Rule exitShortRule, int unstablePeriod) {
        if (entryLongRule == null || exitLongRule == null) {
            throw new IllegalArgumentException("Rules cannot be null");
        }
        if (unstablePeriod < 0) {
            throw new IllegalArgumentException("Unstable period bar count must be >= 0");
        }
        this.name = name;
        this.entryLongRule = entryLongRule;
        this.entryShortRule = entryShortRule;
        this.exitLongRule = exitLongRule;
        this.exitShortRule = exitShortRule;
        this.unstablePeriod = unstablePeriod;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public Rule getEntryLongRule() {
        return this.entryLongRule;
    }

    @Override
    public Rule getEntryShortRule() {
        return this.entryShortRule;
    }

    public Rule getExitLongRule() {
        return this.exitLongRule;
    }

    public Rule getExitShortRule() {
        return this.exitShortRule;
    }

    public int getUnstablePeriod() {
        return this.unstablePeriod;
    }

    public void setUnstablePeriod(int unstablePeriod) {
        this.unstablePeriod = unstablePeriod;
    }

    public boolean isUnstableAt(int index) {
        return index < this.unstablePeriod;
    }

    public boolean shouldLongEnter(int index, TradingRecord tradingRecord) {
        boolean enter = IndicatorRule.super.shouldLongEnter(index, tradingRecord);
        this.traceShouldLongEnter(index, enter);
        return enter;
    }

    public boolean shouldShortEnter(int index, TradingRecord tradingRecord) {
        boolean enter = IndicatorRule.super.shouldShortEnter(index, tradingRecord);
        this.traceShouldShortEnter(index, enter);
        return enter;
    }

    public boolean shouldLongExit(int index, TradingRecord tradingRecord) {
        boolean exit = IndicatorRule.super.shouldLongExit(index, tradingRecord);
        this.traceShouldExit(index, exit);
        return exit;
    }

    public boolean shouldShortExit(int index, TradingRecord tradingRecord) {
        boolean exit = IndicatorRule.super.shouldShortExit(index, tradingRecord);
        this.traceShouldExit(index, exit);
        return exit;
    }

    public IndicatorRule and(IndicatorRule rule) {
        String andName = "and(" + this.name + "," + rule.getName() + ")";
        int unstable = Math.max(this.unstablePeriod, rule.getUnstablePeriod());
        return this.and(andName, rule, unstable);
    }

    public IndicatorRule or(IndicatorRule rule) {
        String orName = "or(" + this.name + "," + rule.getName() + ")";
        int unstable = Math.max(this.unstablePeriod, rule.getUnstablePeriod());
        return this.or(orName, rule, unstable);
    }

    public IndicatorRule opposite() {
        return new BaseIndicatorRule("opposite(" + this.name + ")", this.entryLongRule,  this.entryShortRule, this.exitLongRule, this.exitShortRule, this.unstablePeriod);
    }

    public IndicatorRule and(String name, IndicatorRule rule, int unstablePeriod) {
        return new BaseIndicatorRule(name, this.entryLongRule.and(rule.getEntryLongRule()), this.entryShortRule.and(rule.getEntryShortRule()), this.exitLongRule.and(rule.getExitLongRule()), this.exitShortRule.and(rule.getExitShortRule()), unstablePeriod);
    }

    public IndicatorRule or(String name, IndicatorRule rule, int unstablePeriod) {
        return new BaseIndicatorRule(name, this.entryLongRule.or(rule.getEntryLongRule()), this.entryShortRule.or(rule.getEntryShortRule()), this.exitLongRule.or(rule.getExitLongRule()), this.exitShortRule.or(rule.getExitShortRule()), unstablePeriod);
    }

    /**
     * Traces the shouldLongEnter() method calls.
     *
     * @param index the bar index
     * @param enter true if the strategy should enter, false otherwise
     */
    protected void traceShouldLongEnter(int index, boolean enter) {
        this.log.trace(">>> {}#shouldEnter({}): {}", new Object[]{this.className, index, enter});
    }

    /**
     * Traces the shouldShortEnter() method calls.
     *
     * @param index the bar index
     * @param enter true if the strategy should enter, false otherwise
     */
    protected void traceShouldShortEnter(int index, boolean enter) {
        this.log.trace(">>> {}#shouldEnter({}): {}", new Object[]{this.className, index, enter});
    }

    /**
     * Traces the shouldExit() method calls.
     *
     * @param index the bar index
     * @param exit  true if the strategy should exit, false otherwise
     */
    protected void traceShouldExit(int index, boolean exit) {
        this.log.trace(">>> {}#shouldExit({}): {}", new Object[]{this.className, index, exit});
    }
}
