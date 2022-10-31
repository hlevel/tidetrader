package tide.trader.bot.dto.trade;

import tide.trader.bot.dto.strategy.StrategyDTO;

/**
 * Strategy types for {@link StrategyDTO}.
 */
public enum SideDTO {

    /** long. */
    LONG,

    /** short. */
    SHORT,

    /** close long. */
    EXITLONG,

    /** close short. */
    EXITSHORT,

    /** close. */
    FLAT

}
