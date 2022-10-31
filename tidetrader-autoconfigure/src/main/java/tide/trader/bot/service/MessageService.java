package tide.trader.bot.service;

import tide.trader.bot.dto.position.PositionDTO;
import tide.trader.bot.dto.strategy.StrategyDTO;
import tide.trader.bot.dto.trade.OrderDTO;
import tide.trader.bot.dto.util.CurrencyPairDTO;
import tide.trader.bot.dto.util.CurrencyPairMetaDataDTO;
import tide.trader.bot.dto.util.MessageDTO;
import tide.trader.bot.strategy.internal.CassandreStrategy;
import tide.trader.bot.util.base.strategy.BaseStrategy;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Message sending function
 */
public interface MessageService {

    /**
     * Order update message
     * @param order
     */
    void orderUpdateMessage(CassandreStrategy strategy, OrderDTO order);

    /**
     * New message
     * @param strategy
     * @param title
     * @param body
     */
    void newMessage(StrategyDTO strategy, String title, String body);

    /**
     * New message
     * @param strategy
     * @param positonId
     * @param title
     * @param body
     */
    void newMessage(StrategyDTO strategy, Long positonId, String title, String body);


    /**
     * Update and process unread messages
     */
    void updateFlux();

}
