package org.knowm.xchange.binance.perpetualswap.service;

import org.knowm.xchange.binance.BinanceAdapters;
import org.knowm.xchange.binance.BinanceErrorAdapter;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.dto.BinanceException;
import org.knowm.xchange.binance.dto.trade.*;
import org.knowm.xchange.binance.perpetualswap.BinancePerpetualAuthenticated;
import org.knowm.xchange.binance.dto.trade.BinancePerpetualLeverage;
import org.knowm.xchange.binance.dto.trade.BinancePerpetualOrder;
import org.knowm.xchange.binance.service.BinanceTradeHistoryParams;
import org.knowm.xchange.binance.service.BinanceTradeService;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.trade.*;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.*;
import org.knowm.xchange.service.trade.params.orders.*;
import org.knowm.xchange.utils.Assert;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class BinancePerpetualTradeService extends BinancePerpetualTradeServiceRaw implements TradeService {

    public BinancePerpetualTradeService(BinanceExchange exchange, BinancePerpetualAuthenticated binance, ResilienceRegistries resilienceRegistries) {
        super(exchange, binance, resilienceRegistries);
    }

    @Override
    public OpenOrders getOpenOrders() throws IOException {
        return getOpenOrders(new DefaultOpenOrdersParam());
    }

    public OpenOrders getOpenOrders(CurrencyPair pair) throws IOException {
        return getOpenOrders(new DefaultOpenOrdersParamCurrencyPair(pair));
    }

    @Override
    public OpenOrders getOpenOrders(OpenOrdersParams params) throws IOException {
        try {
            List<BinanceOrder> binanceOpenOrders;
            if (params instanceof OpenOrdersParamCurrencyPair) {
                OpenOrdersParamCurrencyPair pairParams = (OpenOrdersParamCurrencyPair) params;
                CurrencyPair pair = pairParams.getCurrencyPair();
                binanceOpenOrders = super.openOrders(pair);
            } else {
                binanceOpenOrders = super.openOrders();
            }

            List<LimitOrder> limitOrders = new ArrayList<>();
            List<Order> otherOrders = new ArrayList<>();
            binanceOpenOrders.forEach(
                    binanceOrder -> {
                        Order order = BinanceAdapters.adaptOrder(binanceOrder);
                        if (order instanceof LimitOrder) {
                            limitOrders.add((LimitOrder) order);
                        } else {
                            otherOrders.add(order);
                        }
                    });
            return new OpenOrders(limitOrders, otherOrders);
        } catch (BinanceException e) {
            throw BinanceErrorAdapter.adapt(e);
        }
    }

    @Override
    public String placeMarketOrder(MarketOrder mo) throws IOException {
        return placeOrder(OrderType.MARKET, mo, null, null, null);
    }

    @Override
    public String placeLimitOrder(LimitOrder limitOrder) throws IOException {
        TimeInForce tif = timeInForceFromOrder(limitOrder).orElse(TimeInForce.GTC);
        OrderType type;
        if (limitOrder.hasFlag(org.knowm.xchange.binance.dto.trade.BinanceOrderFlags.LIMIT_MAKER)) {
            type = OrderType.LIMIT_MAKER;
            tif = null;
        } else {
            type = OrderType.LIMIT;
        }
        return placeOrder(type, limitOrder, limitOrder.getLimitPrice(), null, tif);
    }

    @Override
    public String placeStopOrder(StopOrder order) throws IOException {
        // Time-in-force should not be provided for market orders but is required for
        // limit orders, order we only default it for limit orders. If the caller
        // specifies one for a market order, we don't remove it, since Binance might
        // allow
        // it at some point.
        TimeInForce tif = timeInForceFromOrder(order).orElse(order.getLimitPrice() != null ? TimeInForce.GTC : null);

        OrderType orderType = BinanceAdapters.adaptOrderType(order);

        return placeOrder(orderType, order, order.getLimitPrice(), order.getStopPrice(), tif);
    }

    /*
    @Override
    public List<String> placeLimitOrders(List<LimitOrder> limitOrders) throws IOException {
        placeOrdersLimit(limitOrders);
        return null;
    }

    @Override
    public String placeMarketOrders(List<MarketOrder> marketOrder) throws IOException {
        return placeOrdersMarket(marketOrder);
    }
    */

    @Override
    public boolean getPositionSideDual(Long recvWindow) throws IOException {
        return super.getPositionSideDual(recvWindow);
    }

    @Override
    public HashMap setPositionSideDual(String dualSidePosition, Long recvWindow) throws IOException {
        return super.setPositionSideDual(dualSidePosition, recvWindow);
    }

    @Override
    public void setLeverage(CurrencyPair currencyPair, Integer leverage) throws IOException {
        BinancePerpetualLeverage result = setLeverageRaw(currencyPair, leverage);
    }

    private String placeOrder(OrderType type, Order order, BigDecimal limitPrice, BigDecimal stopPrice, TimeInForce tif) throws IOException {
        try {
            Long recvWindow = (Long) exchange.getExchangeSpecification().getExchangeSpecificParametersItem("recvWindow");
            BinancePerpetualOrder newOrder =
                    newOrder(
                            order.getCurrencyPair(),
                            BinanceAdapters.convert(order.getType()),
                            type,
                            tif,
                            order.getOriginalAmount(),
                            limitPrice,
                            getClientOrderId(order),
                            stopPrice);
            return Long.toString(newOrder.getOrderId());
        } catch (BinanceException e) {
            throw BinanceErrorAdapter.adapt(e);
        }
    }

    private String getClientOrderId(Order order) {

        String clientOrderId = null;
        for (Order.IOrderFlags flags : order.getOrderFlags()) {
            if (flags instanceof BinanceTradeService.BinanceOrderFlags) {
                BinanceTradeService.BinanceOrderFlags bof = (BinanceTradeService.BinanceOrderFlags) flags;
                if (clientOrderId == null) {
                    clientOrderId = bof.getClientId();
                }
            }
        }
        return clientOrderId;
    }

    @Override
    public boolean cancelOrder(String clientOrderId) throws IOException {
        throw new ExchangeException("You need to provide the currency pair to cancel an order.");
    }

    @Override
    public boolean cancelOrder(CancelOrderParams params) throws IOException {
        try {
            /*if (!(params instanceof CancelOrderByCurrencyPair) && !(params instanceof CancelOrderByIdParams)) {
                throw new ExchangeException("You need to provide the currency pair and the order id to cancel an order.");
            }*/
            CurrencyPair currencyPair = null;
            Long orderId = null;
            String clientOrderId = null;

            if(params instanceof CancelOrderByCurrencyPair) {
                CancelOrderByCurrencyPair paramCurrencyPair = (CancelOrderByCurrencyPair) params;
                currencyPair = paramCurrencyPair.getCurrencyPair();
            } else if(params instanceof CancelOrderByIdParams) {
                CancelOrderByIdParams paramId = (CancelOrderByIdParams) params;
                orderId = BinanceAdapters.id(paramId.getOrderId());
            } else if(params instanceof CancelOrderByClientOrderIdParams) {
                CancelOrderByClientOrderIdParams clientOrderIdParams = (CancelOrderByClientOrderIdParams) params;
                clientOrderId = clientOrderIdParams.getClientOrderId();
                currencyPair = clientOrderIdParams.getCurrencyPair();
            }
            super.cancelOrder(currencyPair, orderId, clientOrderId);
            return true;
        } catch (BinanceException e) {
            throw BinanceErrorAdapter.adapt(e);
        }
    }

    private Optional<TimeInForce> timeInForceFromOrder(Order order) {
        return order.getOrderFlags().stream()
                .filter(flag -> flag instanceof TimeInForce)
                .map(flag -> (TimeInForce) flag)
                .findFirst();
    }

    @Override
    public Class[] getRequiredCancelOrderParamClasses() {
        return new Class[] {CancelOrderByIdParams.class, CancelOrderByCurrencyPair.class};
    }

    @Override
    public UserTrades getTradeHistory(TradeHistoryParams params) throws IOException {
        try {
            Assert.isTrue( params instanceof TradeHistoryParamCurrencyPair, "You need to provide the currency pair to get the user trades.");
            TradeHistoryParamCurrencyPair pairParams = (TradeHistoryParamCurrencyPair) params;
            CurrencyPair pair = pairParams.getCurrencyPair();
            if (pair == null) {
                throw new ExchangeException("You need to provide the currency pair to get the user trades.");
            }
            Long orderId = null;
            Long startTime = null;
            Long endTime = null;
            if (params instanceof TradeHistoryParamsTimeSpan) {
                if (((TradeHistoryParamsTimeSpan) params).getStartTime() != null) {
                    startTime = ((TradeHistoryParamsTimeSpan) params).getStartTime().getTime();
                }
                if (((TradeHistoryParamsTimeSpan) params).getEndTime() != null) {
                    endTime = ((TradeHistoryParamsTimeSpan) params).getEndTime().getTime();
                }
            }
            Long fromId = null;
            if (params instanceof TradeHistoryParamsIdSpan) {
                TradeHistoryParamsIdSpan idParams = (TradeHistoryParamsIdSpan) params;
                try {
                    fromId = BinanceAdapters.id(idParams.getStartId());
                } catch (Throwable ignored) {
                }
            }
            if ((fromId != null) && (startTime != null || endTime != null)) {
                throw new ExchangeException("You should either specify the id from which you get the user trades from or start and end times. If you specify both, Binance will only honour the fromId parameter.");
            }

            Integer limit = null;
            if (params instanceof TradeHistoryParamLimit) {
                TradeHistoryParamLimit limitParams = (TradeHistoryParamLimit) params;
                limit = limitParams.getLimit();
            }

            List<BinanceTrade> binanceTrades = super.myTrades(pair, orderId, startTime, endTime, fromId, limit);
            List<UserTrade> trades = binanceTrades.stream().map(t -> new UserTrade.Builder()
                                                    .type(BinanceAdapters.convertType(t.isBuyer))
                                                    .originalAmount(t.qty)
                                                    .currencyPair(pair)
                                                    .price(t.price)
                                                    .timestamp(t.getTime())
                                                    .id(Long.toString(t.id))
                                                    .orderId(Long.toString(t.orderId))
                                                    .feeAmount(t.commission)
                                                    .feeCurrency(Currency.getInstance(t.commissionAsset))
                                                    .build())
                            .collect(Collectors.toList());
            long lastId = binanceTrades.stream().map(t -> t.id).max(Long::compareTo).orElse(0L);
            return new UserTrades(trades, lastId, Trades.TradeSortType.SortByTimestamp);
        } catch (BinanceException e) {
            throw BinanceErrorAdapter.adapt(e);
        }
    }

    @Override
    public TradeHistoryParams createTradeHistoryParams() {
        return new BinanceTradeHistoryParams();
    }

    @Override
    public OpenOrdersParams createOpenOrdersParams() {
        return new DefaultOpenOrdersParamCurrencyPair();
    }

    @Override
    public Collection<Order> getOrder(OrderQueryParams... params) throws IOException {
        try {
            Collection<Order> orders = new ArrayList<>();
            for (OrderQueryParams param : params) {
                if (!(param instanceof OrderQueryParamCurrencyPair)) {
                    throw new ExchangeException(
                            "Parameters must be an instance of OrderQueryParamCurrencyPair");
                }
                OrderQueryParamCurrencyPair orderQueryParamCurrencyPair = (OrderQueryParamCurrencyPair) param;
                if (orderQueryParamCurrencyPair.getCurrencyPair() == null || orderQueryParamCurrencyPair.getOrderId() == null) {
                    throw new ExchangeException("You need to provide the currency pair and the order id to query an order.");
                }

                orders.add(BinanceAdapters.adaptOrder(super.orderStatus(orderQueryParamCurrencyPair.getCurrencyPair()
                        , BinanceAdapters.id(orderQueryParamCurrencyPair.getOrderId()), null)));
            }
            return orders;
        } catch (BinanceException e) {
            throw BinanceErrorAdapter.adapt(e);
        }
    }

}
