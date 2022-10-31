package tide.trader.bot.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import tide.trader.bot.dto.account.BalanceDTO;
import tide.trader.bot.dto.position.PositionDTO;
import tide.trader.bot.dto.position.PositionStatusDTO;
import tide.trader.bot.dto.strategy.StrategyDTO;
import tide.trader.bot.dto.trade.OrderDTO;
import tide.trader.bot.dto.trade.OrderStatusDTO;
import tide.trader.bot.dto.trade.OrderTypeDTO;
import tide.trader.bot.dto.util.*;
import tide.trader.bot.strategy.internal.CassandreStrategy;
import tide.trader.bot.util.base.service.BaseService;
import tide.trader.bot.util.notification.MessageNotify;
import tide.trader.bot.util.parameters.ExchangeParameters;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Message service - implementation of {@link MessageService}.
 */
@RequiredArgsConstructor
public class MessageServiceImplementation extends BaseService implements MessageService {

    //private final MessageRepository messageRepository;
    /** Message id. */
    private final AtomicLong messageId = new AtomicLong();

    /** Available message. */
    private final List<MessageDTO> availableMessage = new ArrayList<>();

    /** is simulated exchange. */
    private final Boolean simulated;

    /** is simulated exchange. */
    private final Set<MessageNotify> notifies;


    @Override
    public void orderUpdateMessage(CassandreStrategy strategy, OrderDTO order) {
        if(order.getStatus() == OrderStatusDTO.FILLED) {
            logger.debug("orderUpdateMessage with order:{}", order);
            Optional<PositionDTO> positionDTO = strategy.getPositions(order.getCurrencyPair()).values().stream().filter(position -> (position.getOpeningOrder() != null && position.getOpeningOrder().getUid().equals(order.getUid())) || (position.getClosingOrder() != null && position.getClosingOrder().getUid().equals(order.getUid()))).findFirst();
            this.newMessage(strategy.getConfiguration().getStrategyDTO(), (positionDTO.isPresent() ? positionDTO.get().getPositionId() : null), this.getOrderTitle(strategy, positionDTO.orElse(null), order), this.getOrderBody(strategy, positionDTO.orElse(null), order));
        }
    }

    @Override
    public void newMessage(StrategyDTO strategy, String title, String body) {
        newMessage(strategy, null, title, body);
    }

    @Override
    public void newMessage(StrategyDTO strategy, Long positonId, String title, String body) {
        //Do not really add messages to the database
        /*
        Message unreadMessage = new Message();
        unreadMessage.setStrategy(Base.STRATEGY_MAPPER.mapToStrategy(strategy));
        unreadMessage.setStatus(MessageStatusDTO.NEW);
        unreadMessage.setTitle(title);
        unreadMessage.setBody(body);
        messageRepository.save(unreadMessage);
        */
        long id = messageId.incrementAndGet();
        MessageDTO notsentMessage = MessageDTO.builder()
                .uid(id)
                .strategy(strategy)
                .positionId(positonId == null ? id : positonId)
                .title(title)
                .body(body)
                .status(MessageStatusDTO.NEW)
                .build();

        if(!availableMessage.contains(notsentMessage)) {
            availableMessage.add(notsentMessage);
        }
    }

    @Override
    public void updateFlux() {
        Set<MessageDTO> messageUpdates;
        if(simulated) {
            messageUpdates = availableMessage
                    .stream()
                    .filter(message -> message.getStatus() == MessageStatusDTO.NEW)
                    .peek(message -> System.out.println("[" + message.getPositionId() + "]" + message.getTitle() + " " + message.getBody().replaceAll("\n", " ")))
                    .map(this::updateReadMessage)
                    .collect(Collectors.toSet());
        } else {
            messageUpdates = notifies
                    .stream()
                    .flatMap(n -> availableMessage.stream().filter(message -> message.getStatus() == MessageStatusDTO.NEW).filter(n::notify))
                    .map(this::updateReadMessage)
                    .collect(Collectors.toSet());
        }
        //messageUpdates.forEach(availableMessage::remove);

        availableMessage.clear();
        availableMessage.addAll(messageUpdates.size() > 10 ? messageUpdates.stream().skip(messageUpdates.size() - 10).collect(Collectors.toSet()) : messageUpdates);
    }

    /**
     * update message
     * @param message
     * @return
     */
    private MessageDTO updateReadMessage(MessageDTO message) {
        logger.debug("update unread message to " + message);
        if(availableMessage.contains(message)) {
            MessageDTO updateMessage = MessageDTO.builder()
                    .uid(message.getUid())
                    .strategy(message.getStrategy())
                    .status(MessageStatusDTO.READ)
                    .title(message.getTitle())
                    .body(message.getBody())
                    .build();
            logger.debug("update message to " + message);
            return updateMessage;
        }
        return message;
    }

    /**
     * order title
     * @param strategy
     * @param order
     * @return
     */
    private String getOrderTitle(CassandreStrategy strategy, PositionDTO position, OrderDTO order) {
        /*Optional<PositionDTO> positionDTO = strategy.getPositions(order.getCurrencyPair())
                .values()
                .stream()
                .filter(position -> (position.getOpeningOrder() != null && position.getOpeningOrder().getUid().equals(order.getUid())) || (position.getClosingOrder() != null && position.getClosingOrder().getUid().equals(order.getUid())))
                .findFirst();*/

        String title = (strategy.isShort() ? (order.getType() == OrderTypeDTO.BID ? "Long" : "Short") : (order.getType() == OrderTypeDTO.BID ? "Buy" : "Sell"));
        if(position != null) {
            if(position.getClosingOrder() != null && position.getClosingOrder().getUid().equals(order.getUid())) {
                title = (strategy.isShort() ? "Cover" :  "Sell");
            }
        }

        String titleFormat = order.getAmount() + "(" + order.getStatus() + ")";
        titleFormat += " @" + order.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
        return title + " " + titleFormat;
    }

    /**
     * order information
     * @param strategy
     * @param order
     * @return
     */
    private String getOrderBody(CassandreStrategy strategy, PositionDTO position, OrderDTO order) {

        BigDecimal cost = order.getAveragePriceValue().multiply(order.getAmountValue());
        BalanceDTO balanceQuote = strategy.getTradeAccountBalances().get(order.getCurrencyPair().getQuoteCurrency());
        //Optional<PositionDTO> positionDTO = strategy.getPositions(order.getCurrencyPair()).values().stream().filter(position -> (position.getOpeningOrder() != null && position.getOpeningOrder().getUid().equals(order.getUid())) || (position.getClosingOrder() != null && position.getClosingOrder().getUid().equals(order.getUid()))).findFirst();

        StringBuffer bodyBuffer = new StringBuffer();
        if(position != null) {
            bodyBuffer.append("Rules: " + position.getRules() + "\n");
        }
        //bodyBuffer.append("Market price: " + order.getMarketPrice() + "\n");
        bodyBuffer.append("Cost: " + cost.stripTrailingZeros().toPlainString() + " " + order.getCurrencyPair().getQuoteCurrency() + "\n");
        /*if(strategy.isShort()) {
            bodyFormat += "Leverage " + strategy.getConfiguration().getLeverage() + "\n";
        }*/
        if(position != null && (position.getClosingOrder() != null && position.getClosingOrder().getUid().equals(order.getUid()))) {
            bodyBuffer.append("Open price: " + position.getOpeningOrder().getAveragePrice() + "\n");
            bodyBuffer.append("Close price: " + position.getClosingOrder().getAveragePrice() + "\n");
            bodyBuffer.append("Reason: " + position.getExitReason() + "\n");
            bodyBuffer.append(position.getGain() + "\n");
        } else {
            bodyBuffer.append("Average price: " + order.getAveragePrice() + "\n");
        }
        bodyBuffer.append("Balance " + balanceQuote.getAvailable().stripTrailingZeros().toPlainString()  + " " + balanceQuote.getCurrency());
        //bodyFormat += "Balance Pct " + cost.divide(balanceQuote.getAvailable().add(cost), 2, RoundingMode.DOWN).multiply(new BigDecimal("100")) + "(%)";
        return bodyBuffer.toString();
    }

}
