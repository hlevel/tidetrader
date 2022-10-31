package tide.trader.bot.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import tide.trader.bot.batch.PositionFlux;
import tide.trader.bot.domain.Position;
import tide.trader.bot.dto.market.TickerDTO;
import tide.trader.bot.dto.position.PositionCreationResultDTO;
import tide.trader.bot.dto.position.PositionDTO;
import tide.trader.bot.dto.position.PositionRulesDTO;
import tide.trader.bot.dto.position.PositionTypeDTO;
import tide.trader.bot.dto.strategy.StrategyDomainDTO;
import tide.trader.bot.dto.trade.OrderCreationResultDTO;
import tide.trader.bot.dto.trade.TradeDTO;
import tide.trader.bot.dto.util.CurrencyAmountDTO;
import tide.trader.bot.dto.util.CurrencyDTO;
import tide.trader.bot.dto.util.CurrencyPairDTO;
import tide.trader.bot.dto.util.GainDTO;
import tide.trader.bot.repository.PositionRepository;
import tide.trader.bot.strategy.internal.CassandreStrategy;
import tide.trader.bot.strategy.internal.CassandreStrategyInterface;
import tide.trader.bot.util.base.Base;
import tide.trader.bot.util.base.service.BaseService;
import tide.trader.bot.util.math.MathConstants;
import tide.trader.bot.dto.position.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;

/**
 * Position service - Implementation of {@link PositionService}.
 */
@RequiredArgsConstructor
public class PositionServiceCassandreImplementation extends BaseService implements PositionService {

    /** Minimum amount for creating a position. */
    private static final BigDecimal MINIMUM_AMOUNT_FOR_POSITION = new BigDecimal("0.000000001");

    /** Position repository. */
    private final PositionRepository positionRepository;

    /** Trade service. */
    private final TradeService tradeService;

    /** Position flux. */
    private final PositionFlux positionFlux;

    @Override
    public final PositionCreationResultDTO createLongPosition(@NonNull final CassandreStrategy strategy,
                                                              @NonNull final CurrencyPairDTO currencyPair,
                                                              @NonNull final BigDecimal amount,
                                                              @NonNull final PositionRulesDTO rules) {
        return createPosition(strategy, PositionTypeDTO.LONG, currencyPair, amount, rules);
    }

    @Override
    public final PositionCreationResultDTO createShortPosition(@NonNull final CassandreStrategy strategy,
                                                               @NonNull final CurrencyPairDTO currencyPair,
                                                               @NonNull final BigDecimal amount,
                                                               @NonNull final PositionRulesDTO rules) {
        return createPosition(strategy, PositionTypeDTO.SHORT, currencyPair, amount, rules);
    }

    /**
     * Creates a position.
     *
     * @param strategy     strategy
     * @param type         long or short
     * @param currencyPair currency pair
     * @param amount       amount
     * @param rules        rules
     * @return position creation result
     */
    private PositionCreationResultDTO createPosition(final CassandreStrategy strategy,
                                                     final PositionTypeDTO type,
                                                     final CurrencyPairDTO currencyPair,
                                                     final BigDecimal amount,
                                                     final PositionRulesDTO rules) {
        logger.debug("Creating a {} position for {} on {} with the rules: {}",
                type.toString().toLowerCase(Locale.ROOT),
                amount,
                currencyPair,
                rules);

        // It's forbidden to create a position with a too small amount.
        if (MINIMUM_AMOUNT_FOR_POSITION.compareTo(amount) > 0) {
            logger.error("Impossible to create a position for such a small amount ({})", amount);
            return new PositionCreationResultDTO("Impossible to create a position for such a small amount: " + amount, null);
        }

        // =============================================================================================================
        // Creates the order on the exchange.
        final OrderCreationResultDTO orderCreationResult;
        if (type == PositionTypeDTO.LONG) {
            // Long position - we buy.
            orderCreationResult = tradeService.createBuyMarketOrder(strategy, currencyPair, amount);
        } else {
            // Short position - we sell.
            orderCreationResult = tradeService.createSellMarketOrder(strategy, currencyPair, amount);
        }

        // If it works, we create the position.
        if (orderCreationResult.isSuccessful()) {
            // =========================================================================================================
            // Creates the position in database.
            Position position = new Position();
            position.setStrategy(Base.STRATEGY_MAPPER.mapToStrategy(strategy.getConfiguration().getStrategyDTO()));
            position = positionRepository.save(position);

            // =========================================================================================================
            // Creates the position dto.
            PositionDTO p = new PositionDTO(position.getUid(), type, strategy.getConfiguration().getStrategyDTO(), currencyPair, orderCreationResult.getOrder(), rules);
            positionRepository.save(Base.POSITION_MAPPER.mapToPosition(p));
            logger.debug("Position {} opened with order {}",
                    p.getPositionId(),
                    orderCreationResult.getOrder().getOrderId());

            // =========================================================================================================
            // Emit the position, creates and return the position creation result.
            positionFlux.emitValue(p);
            return new PositionCreationResultDTO(p);
        } else {
            logger.error("Position creation failure: {}", orderCreationResult.getErrorMessage());
            return new PositionCreationResultDTO(orderCreationResult.getErrorMessage(), orderCreationResult.getException());
        }
    }

    @Override
    public final void updatePositionRules(final long positionUid, @NonNull final PositionRulesDTO newRules) {
        logger.debug("Updating position {} with the rules: {}", positionUid, newRules);
        final Optional<Position> p = positionRepository.findById(positionUid);
        // If position exists and position is not closed.
        if (p.isPresent() && p.get().getStatus() != PositionStatusDTO.CLOSED) {
            // Stop gain.
            if (newRules.isStopGainPercentageSet()) {
                positionRepository.updateStopGainRule(positionUid, newRules.getStopGainPercentage());
            } else {
                positionRepository.updateStopGainRule(positionUid, null);
            }
            // Stop loss.
            if (newRules.isStopLossPercentageSet()) {
                positionRepository.updateStopLossRule(positionUid, newRules.getStopLossPercentage());
            } else {
                positionRepository.updateStopLossRule(positionUid, null);
            }
        }
    }

    @Override
    public final OrderCreationResultDTO closePosition(@NonNull final CassandreStrategyInterface strategy,
                                                      final long positionUid,
                                                      @NonNull final TickerDTO ticker, final String exitReason) {
        logger.debug("Trying to close position {}.", positionUid);
        final Optional<Position> position = positionRepository.findById(positionUid);
        if (position.isPresent()) {
            final PositionDTO positionDTO = Base.POSITION_MAPPER.mapToPositionDTO(position.get());
            final OrderCreationResultDTO orderCreationResult;

            // =========================================================================================================
            // Here, we create the order creation depending on the position type (Short or long).
            if (positionDTO.getType() == PositionTypeDTO.LONG) {
                // Long - We just sell.
                orderCreationResult = tradeService.createSellMarketOrder(strategy, positionDTO.getCurrencyPair(), positionDTO.getAmount().getValue());
            } else {
                // Short - We buy back with the money we get from the original selling.
                // On opening, we had:
                // CP2: ETH/USDT - 1 ETH costs 10 USDT - We sold 1 ETH, and it will give us 10 USDT.
                // We will use those 10 USDT to buy back ETH when the rule is triggered.
                // CP2: ETH/USDT - 1 ETH costs 2 USDT - We buy 5 ETH, and it will cost us 10 USDT.
                // We can now use those 10 USDT to buy 5 ETH (amount sold / price).
                //final BigDecimal amountToBuy = positionDTO.getAmountToLock().getValue().divide(ticker.getLast(), HALF_UP).setScale(MathConstants.BIGINTEGER_SCALE, FLOOR);
                //orderCreationResult = tradeService.createBuyMarketOrder(strategy, positionDTO.getCurrencyPair(), amountToBuy);
                orderCreationResult = tradeService.createBuyMarketOrder(strategy, positionDTO.getCurrencyPair(), positionDTO.getAmount().getValue());
            }
            // =========================================================================================================
            // If the order is successful, we set the position as closed using closePositionWithOrder().
            if (orderCreationResult.isSuccessful()) {
                //positionDTO.closePositionWithOrder(orderCreationResult.getOrder());
                positionDTO.closePositionWithOrder(orderCreationResult.getOrder(), exitReason);
                logger.debug("Position {} closed with order {}", positionDTO.getPositionId(), orderCreationResult.getOrder().getOrderId());
            } else {
                logger.error("Position {} not closed, failed to create order: {}", positionDTO.getPositionId(), orderCreationResult.getErrorMessage());
            }

            // =========================================================================================================
            // We emit the position to save it and send events to strategies.
            positionFlux.emitValue(positionDTO);
            return orderCreationResult;
        } else {
            logger.error("Impossible to close position {} because we couldn't find it in database", positionUid);
            return new OrderCreationResultDTO("Impossible to close position " + positionUid + " because we couldn't find it in database", null);
        }
    }

    @Override
    public final void setAutoClose(final long positionUid, final boolean value) {
        logger.debug("Set auto close to {} on position {}", value, positionUid);
        positionRepository.updateAutoClose(positionUid, value);
    }

    @Override
    public final void forcePositionClosing(final long positionUid, String exitReason) {
        logger.debug("Force position {} to close, exit reason:{}", positionUid, exitReason);
        positionRepository.updateForceClosing(positionUid, true, exitReason);
    }

    @Override
    public final Set<PositionDTO> getPositions() {
        logger.debug("Retrieving all positions");
        return positionRepository.findByOrderByUid().stream()
                .map(Base.POSITION_MAPPER::mapToPositionDTO)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public final Optional<PositionDTO> getPositionByUid(final long positionUid) {
        logger.debug("Retrieving position by its uid {}", positionUid);
        return positionRepository.findById(positionUid).map(Base.POSITION_MAPPER::mapToPositionDTO);
    }

    @Override
    public final Map<Long, CurrencyAmountDTO> getAmountsLockedByPosition() {
        logger.debug("Retrieving amounts locked by position");
        return positionRepository.findByStatusIn(Stream.of(PositionStatusDTO.OPENING, PositionStatusDTO.OPENED).collect(Collectors.toList()))
                .stream()
                .map(Base.POSITION_MAPPER::mapToPositionDTO)
                .collect(Collectors.toMap(PositionDTO::getUid, PositionDTO::getAmountToLock, (key, value) -> key, HashMap::new));
    }

    @Override
    public final Map<CurrencyDTO, GainDTO> getGains(final long strategyUid) {
        logger.debug("Retrieving gains for all positions");
        HashMap<CurrencyDTO, BigDecimal> totalBefore = new LinkedHashMap<>();
        HashMap<CurrencyDTO, BigDecimal> totalAfter = new LinkedHashMap<>();
        List<CurrencyAmountDTO> openingOrdersFees = new LinkedList<>();
        List<CurrencyAmountDTO> closingOrdersFees = new LinkedList<>();
        HashMap<CurrencyDTO, GainDTO> gains = new LinkedHashMap<>();

        // We calculate, by currency, the amount bought & sold.
        positionRepository.findByStatus(PositionStatusDTO.CLOSED)
                .stream()
                // If we have a strategyUid equals to 0, it means we calculate all gains on all closed positions.
                // If we have a strategyUid different from 0, we only retrieved the position of a particular strategy.
                .filter(position -> position.getStrategy().getUid() == strategyUid || strategyUid == 0)
                .map(Base.POSITION_MAPPER::mapToPositionDTO)
                .forEach(p -> {
                    // We retrieve the currency and initiate the maps if they are empty
                    CurrencyDTO currency;
                    if (p.getType() == PositionTypeDTO.LONG) {
                        // LONG.
                        currency = p.getCurrencyPair().getQuoteCurrency();
                    } else {
                        // SHORT.
                        switch (p.getStrategy().getDomain()) {
                            case SPOT:
                                currency = p.getCurrencyPair().getBaseCurrency();
                                break;
                            case PERPETUAL:
                                currency = p.getCurrencyPair().getQuoteCurrency();
                                break;
                            default:
                                currency = p.getCurrencyPair().getBaseCurrency();
                                break;
                        }

                    }
                    gains.putIfAbsent(currency, null);
                    totalBefore.putIfAbsent(currency, ZERO);
                    totalAfter.putIfAbsent(currency, ZERO);

                    // We calculate the amounts bought and amount sold.
                    if (p.getType() == PositionTypeDTO.LONG) {
                        totalBefore.put(currency, p.getOpeningOrder().getTrades()
                                .stream()
                                .map(t -> t.getAmountValue().multiply(t.getPriceValue()))
                                .reduce(totalBefore.get(currency), BigDecimal::add));
                        totalAfter.put(currency, p.getClosingOrder().getTrades()
                                .stream()
                                .map(t -> t.getAmountValue().multiply(t.getPriceValue()))
                                .reduce(totalAfter.get(currency), BigDecimal::add));
                    } else {
                        if(p.getStrategy().getDomain() == StrategyDomainDTO.PERPETUAL) {
                            totalAfter.put(currency, p.getOpeningOrder().getTrades()
                                    .stream()
                                    .map(t -> t.getAmountValue().multiply(t.getPriceValue()))
                                    .reduce(totalAfter.get(currency), BigDecimal::add));
                            totalBefore.put(currency, p.getClosingOrder().getTrades()
                                    .stream()
                                    .map(t -> t.getAmountValue().multiply(t.getPriceValue()))
                                    .reduce(totalBefore.get(currency), BigDecimal::add));
                        } else {
                            totalBefore.put(currency, p.getOpeningOrder().getTrades()
                                    .stream()
                                    .map(TradeDTO::getAmountValue)
                                    .reduce(totalBefore.get(currency), BigDecimal::add));
                            totalAfter.put(currency, p.getClosingOrder().getTrades()
                                    .stream()
                                    .map(TradeDTO::getAmountValue)
                                    .reduce(totalAfter.get(currency), BigDecimal::add));
                        }
                    }

                    // And now the fees.
                    p.getOpeningOrder().getTrades()
                            .stream()
                            .filter(tradeDTO -> tradeDTO.getFee() != null)
                            .forEach(tradeDTO -> openingOrdersFees.add(tradeDTO.getFee()));
                    p.getClosingOrder().getTrades()
                            .stream()
                            .filter(tradeDTO -> tradeDTO.getFee() != null)
                            .forEach(tradeDTO -> closingOrdersFees.add(tradeDTO.getFee()));
                });

        gains.keySet()
                .forEach(currency -> {
                    // We make the calculation.
                    BigDecimal before = totalBefore.get(currency);
                    BigDecimal after = totalAfter.get(currency);
                    BigDecimal gainAmount = after.subtract(before);
                    BigDecimal gainPercentage = ((after.subtract(before)).divide(before, HALF_UP)).multiply(MathConstants.ONE_HUNDRED_BIG_DECIMAL);

                    GainDTO g = GainDTO.builder()
                            .percentage(gainPercentage.setScale(2, HALF_UP).doubleValue())
                            .amount(CurrencyAmountDTO.builder()
                                    .value(gainAmount)
                                    .currency(currency)
                                    .build())
                            .openingOrderFees(openingOrdersFees)
                            .closingOrderFees(closingOrdersFees)
                            .build();
                    gains.put(currency, g);
                });
        return gains;
    }

    @Override
    public final Map<CurrencyDTO, GainDTO> getGains() {
        return getGains(0);
    }

}
