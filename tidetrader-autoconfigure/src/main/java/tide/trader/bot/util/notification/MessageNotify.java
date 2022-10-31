package tide.trader.bot.util.notification;

import tide.trader.bot.dto.util.MessageDTO;
import tide.trader.bot.util.parameters.ExchangeParameters;

/**
 * Message notification
 */
public interface MessageNotify {

    /**
     * isEnable notify
     * @return
     */
    boolean isEnable();

    /**
     * message sending
     * @param message
     * @return
     */
    boolean notify(MessageDTO message);

}
