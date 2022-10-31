package tide.trader.bot.util.dry;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.knowm.xchange.dto.account.OpenPosition;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import tide.trader.bot.dto.market.TickerDTO;
import tide.trader.bot.dto.position.OpenPositionDTO;
import tide.trader.bot.dto.position.OpenPositionResultDTO;
import tide.trader.bot.dto.position.PositionDTO;
import tide.trader.bot.dto.strategy.StrategyDomainDTO;
import tide.trader.bot.dto.trade.OrderCreationResultDTO;
import tide.trader.bot.dto.trade.OrderStatusDTO;
import tide.trader.bot.dto.trade.TradeDTO;
import tide.trader.bot.dto.account.AccountDTO;
import tide.trader.bot.dto.account.BalanceDTO;
import tide.trader.bot.dto.util.CurrencyPairDTO;
import tide.trader.bot.dto.util.GainDTO;
import tide.trader.bot.repository.OrderRepository;
import tide.trader.bot.repository.PositionRepository;
import tide.trader.bot.strategy.internal.CassandreStrategy;
import tide.trader.bot.util.base.service.BaseService;
import tide.trader.bot.util.exception.DryModeException;
import tide.trader.bot.util.math.MathConstants;
import tide.trader.bot.dto.position.PositionTypeDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.*;
import static org.knowm.xchange.dto.Order.OrderStatus.FILLED;
import static org.knowm.xchange.dto.marketdata.Trades.TradeSortType.SortByTimestamp;

/**
 * AOP for trade service in dry mode.
 */
@Aspect
@Component
@ConditionalOnExpression("${trading.bot.exchange.modes.dry:true}")
@RequiredArgsConstructor
public class TradeServiceDryModeAOP extends BaseService {

    /** Dry order prefix. */
    private static final String DRY_ORDER_PREFIX = "DRY_ORDER_";

    /** Order counter. */
    private final AtomicInteger orderCounter = new AtomicInteger(1);

    /** Dry trade prefix. */
    private static final String DRY_TRADE_PREFIX = "DRY_TRADE_";

    /** Order repository. */
    private final OrderRepository orderRepository;

    /** Position repository. */
    private final PositionRepository positionRepository;

    /** User service - dry mode. */
    private final UserServiceDryModeAOP userService;

    @Around(value = "execution(* tide.trader.bot.service.TradeService.createBuyMarketOrder(..)) && args(strategy, currencyPair, amount)", argNames = "pjp, strategy, currencyPair, amount")
    public final OrderCreationResultDTO createBuyMarketOrder(final ProceedingJoinPoint pjp,
                                                             final CassandreStrategy strategy,
                                                             final CurrencyPairDTO currencyPair,
                                                             final BigDecimal amount) {
        // We check that we have the trade account.
        final Optional<AccountDTO> tradeAccount = strategy.getTradeAccount();
        if (tradeAccount.isEmpty()) {
            throw new DryModeException("Trade account was not found");
        }

        // We check if we have enough assets to buy.
        // Buying order - we buy ETH with BTC.
        // We are buying the following amount: ticker last price * amount
        AccountDTO account = tradeAccount.get();
        Optional<BalanceDTO> balanceQuote = account.getBalance(currencyPair.getQuoteCurrency());
        final Optional<TickerDTO> ticker = strategy.getLastTickerByCurrencyPair(currencyPair);

        //Final participation in calculation
        BigDecimal useAmount = ZERO;
        BigDecimal useQuote = ZERO;
        OpenPositionDTO useOpenPosition = null;
        if (balanceQuote.isPresent()  && ticker.isPresent()) {
            BigDecimal ownedQuote = balanceQuote.get().getAvailable();
            BigDecimal last = ticker.get().getLast();
            BigDecimal cost = last.multiply(amount);

            //If shorting mode exists
            if(strategy.getConfiguration().getStrategyDTO().getDomain() == StrategyDomainDTO.PERPETUAL) {
                //Create warehouse
                useOpenPosition = OpenPositionDTO.builder()
                        .currencyPair(currencyPair)
                        .type(OpenPosition.Type.LONG)
                        .amount(amount)
                        .price(last)
                        .margin(amount.multiply(last).divide(new BigDecimal(strategy.getConfiguration().getLeverage()), MathConstants.USDT_SCALE_DECIMAL, RoundingMode.DOWN))
                        .build();

                //Get the existing position
                Optional<OpenPositionDTO> openPositionDTO = account.getOpenPosition(currencyPair);
                if(openPositionDTO.isPresent()) {
                    OpenPositionResultDTO openPositionResult = openPositionDTO.get().positionLiquidation(useOpenPosition);
                    if(openPositionResult.isSuccessful()) {
                        useOpenPosition = openPositionResult.getOpenPosition();
                    }
                    //Calculate profit and expense amount
                    useQuote = openPositionResult.getQuoteBalance();
                    //System.out.println("swap: flat useQuote=" + useQuote + "\n------------------------------");
                } else {
                    useQuote = useOpenPosition.getMargin().multiply(new BigDecimal("-1"));
                    //System.out.println("——————————————————————————————\nswap: long useQuote=" + useQuote);
                }
                //Using margin and total settlement
                if(useQuote.compareTo(ZERO) < 0 && useQuote.abs().compareTo(ownedQuote) > 0) {
                    final String errorMessage = "Not enough quote (costs: " + cost + " " + currencyPair.getQuoteCurrency() + " - owned quote(x" + strategy.getConfiguration().getLeverage() + "): " + ownedQuote + " " + currencyPair.getQuoteCurrency() + ")";
                    return new OrderCreationResultDTO(errorMessage, new RuntimeException());
                }
                //Calculation of occupation deposit
                //useQuote = useQuote.add(useOpenPosition.getAmountValue().multiply(useOpenPosition.getPriceValue()).divide(BigDecimal.valueOf(strategy.getConfiguration().getLeverage()), MathConstants.USDT_SCALE_DECIMAL, DOWN));
                //useQuote = useQuote.multiply(new BigDecimal("-1"));
            } else {
                //Spot processing logic
                if (cost.compareTo(ownedQuote) > 0) {
                    final String errorMessage = "Not enough assets (costs: " + cost + " " + currencyPair.getQuoteCurrency() + " - owned quote: " + ownedQuote + " " + currencyPair.getQuoteCurrency() + ")";
                    return new OrderCreationResultDTO(errorMessage, new RuntimeException());
                }
                useAmount = amount;
                useQuote = amount.multiply(ticker.get().getLast()).multiply(new BigDecimal("-1"));
            }
        } else {
            return new OrderCreationResultDTO("No assets (" + currencyPair.getQuoteCurrency() + ")", new RuntimeException());
        }

        // We execute the buy.
        Object result = null;
        try {
            result = pjp.proceed();
        } catch (Throwable throwable) {
            logger.error("Error in Dry mode AOP: {}", throwable.getMessage());
        }

        // We update the account.
        if(useOpenPosition != null) {
            userService.addToOpenPosition(strategy, useOpenPosition);
        }
        if(useAmount.compareTo(ZERO) != 0) {
            userService.addToBalance(strategy, CURRENCY_MAPPER.mapToCurrency(currencyPair.getBaseCurrency()), useAmount);
        }
        if(useQuote.compareTo(ZERO) != 0) {
            userService.addToBalance(strategy, CURRENCY_MAPPER.mapToCurrency(currencyPair.getQuoteCurrency()), useQuote);
        }
        return (OrderCreationResultDTO) result;
    }

    @Around(value = "execution(* tide.trader.bot.service.TradeService.createSellMarketOrder(..)) && args(strategy, currencyPair, amount)", argNames = "pjp, strategy, currencyPair, amount")
    public final OrderCreationResultDTO createSellMarketOrder(final ProceedingJoinPoint pjp,
                                                              final CassandreStrategy strategy,
                                                              final CurrencyPairDTO currencyPair,
                                                              final BigDecimal amount) {
        // We check that we have the trade account.
        final Optional<AccountDTO> tradeAccount = strategy.getTradeAccount();
        if (tradeAccount.isEmpty()) {
            throw new DryModeException("Trade account was not found");
        }

        // Selling order - we sell ETH to buy BTC.
        // We are selling the amount
        AccountDTO account = tradeAccount.get();
        Optional<BalanceDTO> balance = account.getBalance(currencyPair.getBaseCurrency());
        Optional<BalanceDTO> balanceQuote = account.getBalance(currencyPair.getQuoteCurrency());
        final Optional<TickerDTO> ticker = strategy.getLastTickerByCurrencyPair(currencyPair);

        //Final participation in calculation
        BigDecimal useAmount = ZERO;
        BigDecimal useQuote = ZERO;
        OpenPositionDTO useOpenPosition = null;
        if ((balanceQuote.isPresent() || balance.isPresent()) && ticker.isPresent()) {
            BigDecimal ownedQuote = balanceQuote.get().getAvailable();
            BigDecimal last = ticker.get().getLast();
            BigDecimal cost = last.multiply(amount);
            //If shorting mode exists
            if(strategy.getConfiguration().getStrategyDTO().getDomain() == StrategyDomainDTO.PERPETUAL) {
                //Create warehouse
                useOpenPosition = OpenPositionDTO.builder()
                        .currencyPair(currencyPair)
                        .type(OpenPosition.Type.SHORT)
                        .amount(amount)
                        .price(last)
                        .margin(amount.multiply(last).divide(new BigDecimal(strategy.getConfiguration().getLeverage()), MathConstants.USDT_SCALE_DECIMAL, RoundingMode.DOWN))
                        .build();

                Optional<OpenPositionDTO> openPositionDTO = account.getOpenPosition(currencyPair);
                if(openPositionDTO.isPresent()) {
                    OpenPositionResultDTO openPositionResult = openPositionDTO.get().positionLiquidation(useOpenPosition);
                    if(openPositionResult.isSuccessful()) {
                        useOpenPosition = openPositionResult.getOpenPosition();
                    }
                    //Calculate the percentage of deposit occupied
                    useQuote = openPositionResult.getQuoteBalance();
                    //System.out.println("swap: flat useQuote=" + useQuote + "\n------------------------------");
                } else {
                    useQuote = useOpenPosition.getMargin().multiply(new BigDecimal("-1"));
                    //System.out.println("------------------------------\nswap: short useQuote=" + useQuote);
                }

                //Using margin and total settlement
                if(useQuote.compareTo(ZERO) < 0 && useQuote.abs().compareTo(ownedQuote) > 0) {
                    final String errorMessage = "Not enough quote1 (costs: " + cost + " " + currencyPair.getQuoteCurrency() + " - owned quote(x" + strategy.getConfiguration().getLeverage() + "): " + ownedQuote + " " + currencyPair.getQuoteCurrency() + ")";
                    return new OrderCreationResultDTO(errorMessage, new RuntimeException());
                }
                //Calculation of occupation deposit
                //useQuote = useQuote.add(useOpenPosition.getAmountValue().multiply(useOpenPosition.getPriceValue()).divide(BigDecimal.valueOf(strategy.getConfiguration().getLeverage()), MathConstants.USDT_SCALE_DECIMAL, DOWN));
                //useQuote = useQuote.multiply(new BigDecimal("-1"));
            } else {
                BigDecimal ownedAssets = balance.get().getAvailable();
                if (amount.compareTo(ownedAssets) > 0) {
                    final String errorMessage = "Not enough assets (amount: " + amount + " " + currencyPair.getQuoteCurrency() + " - owned assets: " + ownedAssets + " " + currencyPair.getBaseCurrency();
                    return new OrderCreationResultDTO(errorMessage, new RuntimeException());
                }
                useAmount = amount.multiply(new BigDecimal("-1"));
                useQuote = amount.multiply(last);
            }
        } else {
            return new OrderCreationResultDTO("No assets (" + currencyPair.getBaseCurrency() + ")", new RuntimeException());
        }

        // We execute the sell.
        Object result = null;
        try {
            result = pjp.proceed();
        } catch (Throwable throwable) {
            logger.error("Error in Dry mode AOP: {}", throwable.getMessage());
        }

        // We update the account.
        if(useOpenPosition != null) {
            userService.addToOpenPosition(strategy, useOpenPosition);
        }
        if(useAmount.compareTo(ZERO) != 0) {
            userService.addToBalance(strategy, CURRENCY_MAPPER.mapToCurrency(currencyPair.getBaseCurrency()), useAmount);
        }
        if(useQuote.compareTo(ZERO) != 0) {
            userService.addToBalance(strategy, CURRENCY_MAPPER.mapToCurrency(currencyPair.getQuoteCurrency()), useQuote);
        }
        // We update the account.
        //userService.addToBalance(strategy, CURRENCY_MAPPER.mapToCurrency(currencyPair.getBaseCurrency()), amount.multiply(new BigDecimal("-1")));
        //userService.addToBalance(strategy, CURRENCY_MAPPER.mapToCurrency(currencyPair.getQuoteCurrency()), amount.multiply(ticker.get().getLast()));
        return (OrderCreationResultDTO) result;
    }

    @Around(value = "execution(* org.knowm.xchange.service.trade.TradeService.placeMarketOrder(..)) && args(marketOrder)", argNames = "pjp, marketOrder")
    public final String placeMarketOrder(final ProceedingJoinPoint pjp, final MarketOrder marketOrder) {
        return DRY_ORDER_PREFIX.concat(String.format("%09d", orderCounter.getAndIncrement()));
    }

    @Around(value = "execution(* tide.trader.bot.service.TradeService.cancelOrder(..)) && args(orderUid))", argNames = "pjp, orderUid")
    public final boolean cancelOrder(final ProceedingJoinPoint pjp, final long orderUid) {
        throw new DryModeException("Not supported");
    }

    @Around("execution(* org.knowm.xchange.service.trade.TradeService.getOpenOrders())")
    public final OpenOrders getOpenOrders(final ProceedingJoinPoint pjp) {
        // For every new order created in Cassandre (in database), we reply with an updated order saying this same order is filled.
        return new OpenOrders(orderRepository.findByStatusNot(OrderStatusDTO.CLOSED)
                .stream()
                .map(ORDER_MAPPER::mapToOrderDTO)
                .map(orderDTO -> new LimitOrder.Builder(UTIL_MAPPER.mapToOrderType(orderDTO.getType()), CURRENCY_MAPPER.mapToCurrencyPair(orderDTO.getCurrencyPair()))
                        .id(orderDTO.getOrderId())
                        .originalAmount(orderDTO.getAmountValue())
                        .averagePrice(orderDTO.getAveragePriceValue())
                        .limitPrice(orderDTO.getLimitPriceValue())
                        .orderStatus(FILLED)
                        .cumulativeAmount(orderDTO.getCumulativeAmountValue())
                        .userReference(orderDTO.getUserReference())
                        .timestamp(Timestamp.valueOf(orderDTO.getTimestamp().toLocalDateTime()))
                        .build())
                .collect(Collectors.toList()));
    }

    @Around(value = "execution(* org.knowm.xchange.service.trade.TradeService.getTradeHistory(..)) && args(params))", argNames = "pjp, params")
    public final UserTrades getTradeHistory(final ProceedingJoinPoint pjp, final TradeHistoryParams params) {
        // We will check for every order not fulfilled, if this order is used to close a position.
        // If so, we change the price of the trade to the price of the trade according to the position price.
        Map<String, BigDecimal> tradePrices = new HashMap<>();
        orderRepository.findByOrderByTimestampAsc()
                .stream()
                .filter(order -> order.getStatus() == OrderStatusDTO.FILLED)
                .map(ORDER_MAPPER::mapToOrderDTO)
                .filter(orderDTO -> !orderDTO.isFulfilled())    // Only orders with trades not arrived
                .forEach(orderDTO -> {
                            tradePrices.put(orderDTO.getOrderId(), orderDTO.getMarketPriceValue());

                            // We search to see if the order is used to close a position.
                            final Optional<PositionDTO> positionDTO = positionRepository.findAll()
                                    .stream()
                                    .filter(position -> position.getClosingOrder() != null)
                                    .filter(position -> position.getClosingOrder().getOrderId().equals(orderDTO.getOrderId()))
                                    .map(POSITION_MAPPER::mapToPositionDTO)
                                    .findFirst();

                            // If this order is used to close position, we calculate a new price to match rules percentages.
                            // A gain was made, we recalculate it from the order.
                            if (positionDTO.isPresent()) {
                                final Optional<GainDTO> gainDTO = positionDTO.get().calculateGainFromPrice(orderDTO.getMarketPriceValue());

                                if (gainDTO.isPresent() && gainDTO.isEmpty()) {
                                    /*存在计算有问题。假设止损点5%，这根线已经计算出损失已经是6%,该操作会强行修正到5%.实际账户余额已经按照6%进行减扣了*/
                                    // We need the trade of the opening order to know the price the asset was bought.
                                    final TradeDTO openingTrade = positionDTO.get().getOpeningOrder().getTrades().iterator().next();

                                    if (positionDTO.get().getType().equals(PositionTypeDTO.LONG)) {
                                        // =====================================================================================
                                        // Treating long positions.

                                        if (positionDTO.get().getRules().isStopGainPercentageSet()
                                                && gainDTO.get().getPercentage() >= 0) {
                                            // If the position has a stop gain percentage and the real gain is superior to this percentage.
                                            // This means the stop gain won, and we should transform the price.

                                            // Long position n°1 (rules: 200.0 % gain).
                                            //  Opening order: 20 000 USDT.
                                            //  Closed with trade DRY_TRADE_000000007: 70 000 USDT.
                                            //  250 % evolution => ((70000 - 20000) / 20000) * 100 = 250 %
                                            //  How to calculate the new price.
                                            //  openingTrade market price * (( openingTrade market price * rules gain)/100)
                                            final BigDecimal augmentation = positionDTO.get().getOpeningOrder().getMarketPriceValue()
                                                    .multiply(BigDecimal.valueOf(positionDTO.get().getRules().getStopGainPercentage()))
                                                    .divide(MathConstants.ONE_HUNDRED_BIG_DECIMAL, MathConstants.BIGINTEGER_SCALE, FLOOR);
                                            tradePrices.put(orderDTO.getOrderId(), openingTrade.getPriceValue().add(augmentation));
                                        } else if (positionDTO.get().getRules().isStopLossPercentageSet()
                                                && gainDTO.get().getPercentage() < 0) {
                                            // If the position has a stop gain percentage and the real gain is superior to this percentage.
                                            // This means the stop gain won, and we should transform the price.

                                            // Long position n°2 (rules: 20.0 % loss).
                                            //  Opening order: 50 000 USDT.
                                            //  Closed with trade DRY_TRADE_000000004: 30 000 USDT.
                                            //  -40 % evolution => ((30000 - 50000) / 50000) * 100 = -40 %
                                            //  How to calculate the new price.
                                            //  openingTrade market price * (( openingTrade market price * rules gain)/100)
                                            final BigDecimal reduction = positionDTO.get().getOpeningOrder().getMarketPriceValue()
                                                    .multiply(BigDecimal.valueOf(positionDTO.get().getRules().getStopLossPercentage()))
                                                    .divide(MathConstants.ONE_HUNDRED_BIG_DECIMAL, MathConstants.BIGINTEGER_SCALE, FLOOR);
                                            tradePrices.put(orderDTO.getOrderId(), openingTrade.getPriceValue().subtract(reduction));
                                        }
                                        // =====================================================================================
                                    } else {
                                        // =====================================================================================
                                        // Treating short positions.

                                        if (positionDTO.get().getRules().isStopGainPercentageSet()
                                                && gainDTO.get().getPercentage() >= 0) {
                                            // If the position has a stop gain percentage and the real gain is superior to this percentage.
                                            // This means the stop gain won, and we should transform the price.

                                            // Short position n°4 (rules: 100.0 % gain)
                                            //  Opening order: 70 000 USDT.
                                            //  Closed with DRY_TRADE_000000009: 25 000 USDT.
                                            //  It's a shot position so:
                                            //  We sold one bitcoin for 70 000 USDT.
                                            //  When the price reached 25 000 USDT, with the 70 000 USDT, we could buy 2.8 BTC.
                                            //  180 % evolution => ((2.8 - 1) / 1) * 100 = 180 %
                                            //  How to calculate the new price.
                                            //  Amount I gained = opening trade amount * 70 000 USDT.
                                            //  To gain 100%, I should be able to by 2 bitcoins: opening trade amount * (opening trade amount * stop gain/100)
                                            //  so the question is how much a bitcoin should cost, so I can buy 2 with 70 000 USDT
                                            //  2 * price = 70 000 USDT => price = 70 000/2 = 35 000
                                            final BigDecimal augmentation = openingTrade.getAmountValue()
                                                    .multiply(BigDecimal.valueOf(positionDTO.get().getRules().getStopGainPercentage()))
                                                    .divide(MathConstants.ONE_HUNDRED_BIG_DECIMAL, MathConstants.BIGINTEGER_SCALE, FLOOR);
                                            orderRepository.updateAmount(orderDTO.getUid(), openingTrade.getAmountValue().add(augmentation));
                                            tradePrices.put(orderDTO.getOrderId(), positionDTO.get().getOpeningOrder().getMarketPriceValue().divide(openingTrade.getAmountValue().add(augmentation), MathConstants.BIGINTEGER_SCALE, FLOOR));

                                        } else if (positionDTO.get().getRules().isStopLossPercentageSet()
                                                && gainDTO.get().getPercentage() < 0) {
                                            // If the position has a stop gain percentage and the real gain is superior to this percentage.
                                            // This means the stop gain won, and we should transform the price.

                                            // Short position n°3 (rules: 10.0 % loss)
                                            //  Opening order: 40 000 USDT.
                                            //  Closed with trade DRY_TRADE_000000008: 70 000 USDT.
                                            //  It's a shot position so:
                                            //  We sold 1 bitcoin for 40 000 USDT.
                                            //  When the price reached 70 000 USDT, with the 40 000 USDT, we could buy 0.57 BTC.
                                            //  We had 1 BTC, we now only have 0.57 BTC
                                            //  -43 % evolution => ((0.57 - 1) / 1) * 100 = -43 %
                                            //  How to calculate the new price.
                                            //  Amount I gained = opening trade amount * 40 000 USDT.
                                            //  To lose 10%, I should finish by only being able to buy 0,90 BTC: opening trade amount * (opening trade amount * stop gain/100)
                                            //  so the question is how much a bitcoin should cost, so I can buy 0,90 with 40 000 USDT
                                            //  0.9 * price = 40 000 USDT => price = 40 000/0.9

                                            final BigDecimal reduction = positionDTO.get().getOpeningOrder().getMarketPriceValue()
                                                    .multiply(BigDecimal.valueOf(positionDTO.get().getRules().getStopLossPercentage()))
                                                    .divide(MathConstants.ONE_HUNDRED_BIG_DECIMAL, MathConstants.BIGINTEGER_SCALE, FLOOR);
                                            tradePrices.put(orderDTO.getOrderId(), openingTrade.getPriceValue().add(reduction));

                                        }
                                        // =====================================================================================
                                    }
                                }
                            }
                        }
                );

        // For every orders not fulfilled in database, we will simulate an equivalent trade to close it.
        List<UserTrade> trades = orderRepository.findByOrderByTimestampAsc()
                .stream()
                .map(ORDER_MAPPER::mapToOrderDTO)
                .filter(orderDTO -> !orderDTO.isFulfilled())                        // Only orders without trade.
                .filter(orderDTO -> tradePrices.get(orderDTO.getOrderId()) != null) // Only orders with price calculated.
                .map(orderDTO -> UserTrade.builder()
                        .id(orderDTO.getOrderId().replace(DRY_ORDER_PREFIX, DRY_TRADE_PREFIX))
                        .type(UTIL_MAPPER.mapToOrderType(orderDTO.getType()))
                        .orderId(orderDTO.getOrderId())
                        .currencyPair(CURRENCY_MAPPER.mapToCurrencyPair(orderDTO.getCurrencyPair()))
                        .originalAmount(orderDTO.getAmountValue())
                        .price(tradePrices.get(orderDTO.getOrderId()))
                        .feeAmount(ZERO)
                        .timestamp(Timestamp.valueOf(orderDTO.getTimestamp().toLocalDateTime()))
                        .build())
                .collect(Collectors.toList());
        return new UserTrades(trades, SortByTimestamp);
    }

    @Around(value = "execution(* tide.trader.bot.service.TradeService.setLeverage(..)) && args(currencyPair, leverage))", argNames = "pjp, currencyPair, leverage")
    public final void setLeverage(final ProceedingJoinPoint pjp, final CurrencyPairDTO currencyPair, final Integer leverage) {
        logger.debug("{} set leverage is {}", currencyPair, leverage);
    }

}
