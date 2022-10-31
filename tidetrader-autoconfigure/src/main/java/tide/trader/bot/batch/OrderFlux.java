package tide.trader.bot.batch;

import lombok.RequiredArgsConstructor;
import tide.trader.bot.domain.Order;
import tide.trader.bot.dto.trade.OrderDTO;
import tide.trader.bot.repository.OrderRepository;
import tide.trader.bot.service.TradeService;
import tide.trader.bot.util.base.Base;
import tide.trader.bot.util.base.batch.BaseFlux;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Order flux - push {@link OrderDTO}.
 * Two methods override from super class:
 * - getNewValues(): calling trade service to retrieve orders from exchange.
 * - saveValues(): saving/updating orders in database.
 * To get a deep understanding of how it works, read the documentation of {@link BaseFlux}.
 */
@RequiredArgsConstructor
public class OrderFlux extends BaseFlux<OrderDTO> {

    /** Order repository. */
    private final OrderRepository orderRepository;

    /** Trade service. */
    private final TradeService tradeService;

    @Override
    protected final Set<OrderDTO> getNewValues() {
        logger.debug("Retrieving orders from exchange");
        Set<OrderDTO> newValues = new LinkedHashSet<>();

        // Getting all the orders from the exchange.
        tradeService.getOrders()
                .forEach(order -> {
                    logger.debug("Checking order: {}", order.getOrderId());
                    final Optional<Order> orderInDatabase = orderRepository.findByOrderId(order.getOrderId());

                    // If the order is not in database, we insert it only if strategy is set on that order.
                    // If strategy is not set, it means that Cassandre did not yet save its locally created order.
                    if (orderInDatabase.isEmpty() && order.getStrategy() != null) {
                        logger.debug("New order from exchange: {}", order);
                        newValues.add(order);
                    }

                    // If the local order is already saved in database and the order retrieved from the exchange
                    // is different, then, we update the order in database.
                    if (orderInDatabase.isPresent() && !Base.ORDER_MAPPER.mapToOrderDTO(orderInDatabase.get()).equals(order)) {
                        logger.debug("Updated order from exchange: {}", order);
                        newValues.add(order);
                    }
                });

        return newValues;
    }

    @Override
    protected final Set<OrderDTO> saveValues(final Set<OrderDTO> newValues) {
        Set<Order> orders = new LinkedHashSet<>();

        // We create or update every order retrieved by the exchange.
        newValues.forEach(newValue -> orderRepository.findByOrderId(newValue.getOrderId())
                .ifPresentOrElse(order -> {
                    // Update order.
                    Base.ORDER_MAPPER.updateOrder(newValue, order);
                    orders.add(orderRepository.save(order));
                    logger.debug("Updating order in database: {}", order);
                }, () -> {
                    // Create order.
                    final Order newOrder = Base.ORDER_MAPPER.mapToOrder(newValue);
                    orders.add(orderRepository.save(newOrder));
                    logger.debug("Creating order in database: {}", newOrder);
                }));

        return orders.stream()
                .map(Base.ORDER_MAPPER::mapToOrderDTO)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
