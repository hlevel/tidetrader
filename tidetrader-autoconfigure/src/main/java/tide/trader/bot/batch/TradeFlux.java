package tide.trader.bot.batch;

import lombok.RequiredArgsConstructor;
import tide.trader.bot.domain.Trade;
import tide.trader.bot.dto.trade.TradeDTO;
import tide.trader.bot.repository.OrderRepository;
import tide.trader.bot.repository.TradeRepository;
import tide.trader.bot.service.TradeService;
import tide.trader.bot.util.base.batch.BaseFlux;
import tide.trader.bot.util.base.Base;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Trade flux - push {@link TradeDTO}.
 * Two methods override from super class:
 * - getNewValues(): calling trade service to retrieve trades from exchange (only if orders exists already in database).
 * - saveValues(): saving/updating trades in database.
 * To get a deep understanding of how it works, read the documentation of {@link BaseFlux}.
 */
@RequiredArgsConstructor
public class TradeFlux extends BaseFlux<TradeDTO> {

    /** Order repository. */
    private final OrderRepository orderRepository;

    /** Trade repository. */
    private final TradeRepository tradeRepository;

    /** Trade service. */
    private final TradeService tradeService;

    @Override
    protected final Set<TradeDTO> getNewValues() {
        logger.debug("Retrieving trades from exchange");
        Set<TradeDTO> newValues = new LinkedHashSet<>();

        // Finding which trades have been updated.
        tradeService.getTrades()
                .stream()
                // Note: we only save trades when the order present in database.
                .filter(t -> orderRepository.findByOrderId(t.getOrderId()).isPresent())
                .forEach(trade -> {
                    logger.debug("Checking trade: {}", trade.getTradeId());
                    final Optional<Trade> tradeInDatabase = tradeRepository.findByTradeId(trade.getTradeId());

                    // The trade is not in database.
                    if (tradeInDatabase.isEmpty()) {
                        logger.debug("New trade from exchange: {}", trade);
                        newValues.add(trade);
                    }

                    // The trade is in database but the trade values from the server changed.
                    if (tradeInDatabase.isPresent() && !Base.TRADE_MAPPER.mapToTradeDTO(tradeInDatabase.get()).equals(trade)) {
                        logger.debug("Updated trade from exchange: {}", trade);
                        newValues.add(trade);
                    }
                });

        return newValues;
    }

    @Override
    protected final Set<TradeDTO> saveValues(final Set<TradeDTO> newValues) {
        Set<Trade> trades = new LinkedHashSet<>();

        // We create or update every trade retrieved by the exchange.
        newValues.forEach(newValue -> tradeRepository.findByTradeId(newValue.getTradeId())
                .ifPresentOrElse(trade -> {
                    // Update trade.
                    Base.TRADE_MAPPER.updateTrade(newValue, trade);
                    trades.add(tradeRepository.save(trade));
                    logger.debug("Updating trade in database: {}", trade);
                }, () -> {
                    // Create trade.
                    final Trade newTrade = Base.TRADE_MAPPER.mapToTrade(newValue);
                    // Order is always present as we check it in getNewValues().
                    orderRepository.findByOrderId(newValue.getOrderId()).ifPresent(newTrade::setOrder);
                    trades.add(tradeRepository.save(newTrade));
                    logger.debug("Creating trade in database: {}", newTrade);
                }));

        return trades.stream()
                .map(Base.TRADE_MAPPER::mapToTradeDTO)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
