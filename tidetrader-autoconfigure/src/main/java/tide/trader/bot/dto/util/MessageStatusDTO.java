package tide.trader.bot.dto.util;

import tide.trader.bot.dto.trade.OrderDTO;

/**
 * Order status for {@link OrderDTO}.
 */
public enum MessageStatusDTO {

    /** Initial order when placed on the order book at exchange. */
    NEW,

    /** Message has read. */
    READ,

    /** Message has expired it's time to live. */
    EXPIRED;

}
