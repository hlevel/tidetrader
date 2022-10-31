package org.knowm.xchange.binance.perpetualswap.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.knowm.xchange.binance.BinanceAdapters;
import org.knowm.xchange.binance.BinanceErrorAdapter;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.dto.BinanceException;
import org.knowm.xchange.binance.dto.trade.*;
import org.knowm.xchange.binance.perpetualswap.BinancePerpetualAuthenticated;
import org.knowm.xchange.binance.dto.trade.BinancePerpetualLeverage;
import org.knowm.xchange.binance.dto.trade.BinancePerpetualOrder;
import org.knowm.xchange.binance.dto.trade.Binanceresult;
import org.knowm.xchange.binance.service.BinanceTradeService;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.knowm.xchange.binance.BinanceResilience.*;
import static org.knowm.xchange.client.ResilienceRegistries.NON_IDEMPOTENT_CALLS_RETRY_CONFIG_NAME;

public class BinancePerpetualTradeServiceRaw extends BinancePerpetualBaseService {

    protected BinancePerpetualTradeServiceRaw(BinanceExchange exchange, BinancePerpetualAuthenticated binance, ResilienceRegistries resilienceRegistries) {
        super(exchange, binance, resilienceRegistries);
    }

    public List<BinanceOrder> openOrders() throws BinanceException, IOException {
        return openOrders(null);
    }

    public List<BinanceOrder> openOrders(CurrencyPair pair) throws BinanceException, IOException {
        return decorateApiCall(
                () ->
                        binance.openOrders(
                                Optional.ofNullable(pair).map(BinanceAdapters::toSymbol).orElse(null),
                                getRecvWindow(),
                                getTimestampFactory(),
                                apiKey,
                                signatureCreator))
                .withRetry(retry("openOrders"))
                .withRateLimiter(rateLimiter(REQUEST_WEIGHT_RATE_LIMITER), openOrdersPermits(pair))
                .call();
    }

    protected int openOrdersPermits(CurrencyPair pair) {
        return pair != null ? 1 : 40;
    }

    public BinanceOrder orderStatus(CurrencyPair pair, Long orderId, String origClientOrderId)
            throws IOException, BinanceException {
        return decorateApiCall(
                () ->
                        binance.orderStatus(
                                BinanceAdapters.toSymbol(pair),
                                orderId,
                                origClientOrderId,
                                getRecvWindow(),
                                getTimestampFactory(),
                                super.apiKey,
                                super.signatureCreator))
                .withRetry(retry("orderStatus"))
                .withRateLimiter(rateLimiter(REQUEST_WEIGHT_RATE_LIMITER))
                .call();
    }

    public BinanceCancelledOrder cancelOrder(CurrencyPair pair, Long orderId, String origClientOrderId) throws IOException, BinanceException {
        return decorateApiCall(
                () ->
                        binance.cancelOrder(
                                BinanceAdapters.toSymbol(pair),
                                orderId,
                                origClientOrderId,
                                getRecvWindow(),
                                getTimestampFactory(),
                                super.apiKey,
                                super.signatureCreator))
                .withRetry(retry("cancelOrder"))
                .withRateLimiter(rateLimiter(REQUEST_WEIGHT_RATE_LIMITER))
                .call();
    }

    public List<BinanceCancelledOrder> cancelAllOpenOrders(CurrencyPair pair)
            throws IOException, BinanceException {
        return decorateApiCall(
                () ->
                        binance.cancelAllOpenOrders(
                                BinanceAdapters.toSymbol(pair),
                                getRecvWindow(),
                                getTimestampFactory(),
                                super.apiKey,
                                super.signatureCreator))
                .withRetry(retry("cancelAllOpenOrders"))
                .withRateLimiter(rateLimiter(REQUEST_WEIGHT_RATE_LIMITER))
                .call();
    }

    public List<BinanceOrder> allOrders(CurrencyPair pair, Long orderId, Integer limit)
            throws BinanceException, IOException {
        return decorateApiCall(
                () ->
                        binance.allOrders(
                                BinanceAdapters.toSymbol(pair),
                                orderId,
                                limit,
                                getRecvWindow(),
                                getTimestampFactory(),
                                apiKey,
                                signatureCreator))
                .withRetry(retry("allOrders"))
                .withRateLimiter(rateLimiter(REQUEST_WEIGHT_RATE_LIMITER))
                .call();
    }

    public List<BinanceTrade> myTrades(
            CurrencyPair pair, Long orderId, Long startTime, Long endTime, Long fromId, Integer limit)
            throws BinanceException, IOException {

        List<BinancePerpetualTrade> myTrades = decorateApiCall( () ->
                        binance.myTrades(
                                BinanceAdapters.toSymbol(pair),
                                limit,
                                startTime,
                                endTime,
                                fromId,
                                getRecvWindow(),
                                getTimestampFactory(),
                                apiKey,
                                signatureCreator))
                .withRetry(retry("myTrades"))
                .withRateLimiter(rateLimiter(REQUEST_WEIGHT_RATE_LIMITER), myTradesPermits(limit))
                .call();
        return myTrades.stream().map( trade -> new BinanceTrade(trade.id, trade.id, trade.orderId, trade.price, trade.qty, trade.commission, trade.commissionAsset, trade.time, trade.buyer, trade.maker, false)).collect(Collectors.toList());
    }

    protected int myTradesPermits(Integer limit) {
        if (limit != null && limit > 500) {
            return 10;
        }
        return 5;
    }

    public BinanceListenKey startUserDataStream() throws IOException {
        return decorateApiCall(() -> binance.startUserDataStream(apiKey))
                .withRateLimiter(rateLimiter(REQUEST_WEIGHT_RATE_LIMITER))
                .call();
    }

    public void keepAliveDataStream(String listenKey) throws IOException {
        decorateApiCall(() -> binance.keepAliveUserDataStream(apiKey, listenKey))
                .withRateLimiter(rateLimiter(REQUEST_WEIGHT_RATE_LIMITER))
                .call();
    }

    public void closeDataStream(String listenKey) throws IOException {
        decorateApiCall(() -> binance.closeUserDataStream(apiKey, listenKey))
                .withRateLimiter(rateLimiter(REQUEST_WEIGHT_RATE_LIMITER))
                .call();
    }

    protected String placeOrdersLimit(List<LimitOrder> limitOrders) throws IOException {
        try {
            OrderType type=OrderType.LIMIT;
            List<BinancePerpetualOrder> batchOrders=new ArrayList<BinancePerpetualOrder>();
            limitOrders.forEach(e->{
                BinancePerpetualOrder newOrder =
                        null;
                try {
                    newOrder = newOrder(
                            e.getCurrencyPair(),
                            BinanceAdapters.convert(e.getType()),
                            type,
                            null,
                            e.getOriginalAmount(),
                            e.getLimitPrice(),
                            getClientOrderId(e),
                            null);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                batchOrders.add(newOrder);
            });
            BinancePerpetualOrder[] strings = new BinancePerpetualOrder[batchOrders.size()];
            batchOrders.toArray(strings);

            binance.batchOrders(new ObjectMapper().writeValueAsString(strings), getTimestampFactory().createValue(),getTimestampFactory(),apiKey,signatureCreator);
            //binance.batchOrders(JSON.toJSONString(strings),getTimestampFactory().createValue(),getTimestampFactory(),apiKey,signatureCreator);
            return "success";
        } catch (BinanceException e) {
            throw BinanceErrorAdapter.adapt(e);
        }
    }

    protected String placeOrdersMarket(List<MarketOrder> marketOrder) throws IOException {
        try {
            OrderType type=OrderType.MARKET;
            List<BinancePerpetualOrder> batchOrders=new ArrayList<BinancePerpetualOrder>();
            marketOrder.forEach(e->{
                BinancePerpetualOrder newOrder = null;
                try {
                    newOrder = newOrder( e.getCurrencyPair(),
                            BinanceAdapters.convert(e.getType()),
                            type,
                            null,
                            e.getOriginalAmount(),
                            null,
                            getClientOrderId(e),
                            null);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                batchOrders.add(newOrder);
            });


            BinancePerpetualOrder[] strings = new BinancePerpetualOrder[batchOrders.size()];
            batchOrders.toArray(strings);
            binance.batchOrders(new ObjectMapper().writeValueAsString(strings),getRecvWindow(),getTimestampFactory(),apiKey,signatureCreator);
            //binance.batchOrders(JSON.toJSONString(strings),getRecvWindow(),getTimestampFactory(),apiKey,signatureCreator);
            return "success";
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

    public BinancePerpetualOrder newOrder(
            CurrencyPair currencyPair,
            OrderSide side,
            OrderType type,
            TimeInForce timeInForce,
            BigDecimal quantity,
            BigDecimal price,
            String newClientOrderId,
            BigDecimal stopPrice) throws IOException {

        return decorateApiCall(
                () -> binance.newOrder(
                                BinanceAdapters.toSymbol(currencyPair),
                                side,
                                type,
                                timeInForce,
                                quantity,
                                price,
                                newClientOrderId,
                                stopPrice,
                                getRecvWindow(),
                                getTimestampFactory(),
                                apiKey,
                                signatureCreator))
                .withRetry(retry("newOrder", NON_IDEMPOTENT_CALLS_RETRY_CONFIG_NAME))
                .withRateLimiter(rateLimiter(ORDERS_PER_SECOND_RATE_LIMITER))
                .withRateLimiter(rateLimiter(ORDERS_PER_DAY_RATE_LIMITER))
                .withRateLimiter(rateLimiter(REQUEST_WEIGHT_RATE_LIMITER))
                .call();
    }

    public  boolean getPositionSideDual(Long recvWindow) throws IOException {
        return binance.getPositionSideDual(getTimestampFactory(),null,apiKey,signatureCreator);
    }

    public HashMap setPositionSideDual(String dualSidePosition, Long recvWindow) throws IOException {
        return binance.setPositionSideDual(dualSidePosition,recvWindow,getTimestampFactory(),apiKey,signatureCreator);

    }

    public Binanceresult setMarginType(CurrencyPair currencyPair, MarginType marginType) throws IOException {
        return binance.setMarginType(currencyPair.getParsing(""), marginType,null,getTimestampFactory(),apiKey,signatureCreator);

    }

    public BinancePerpetualLeverage setLeverageRaw(CurrencyPair currencyPair, Integer leverage) throws IOException {
        return binance.setLeverage(currencyPair.getParsing(""), leverage,null,getTimestampFactory(), apiKey, signatureCreator);
    }

}
