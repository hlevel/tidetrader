package tide.trader.bot.dto.account;

/**
 * {@link AccountDTO} features.
 */
public enum AccountFeatureDTO {

    /** The wallet has the ability to deposit external funds and withdraw funds allocated on it. */
    FUNDING,

    /** You can trade funds allocated to this wallet. */
    TRADING,

    /** You can do margin trading with funds allocated to this wallet. */
    MARGIN_TRADING,

    /** You can fund other margin traders with funds allocated to this wallet to earn an interest. */
    MARGIN_FUNDING

}
