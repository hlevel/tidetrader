package org.knowm.xchange.binance.perpetualswap.service;

import org.knowm.xchange.binance.BinanceAdapters;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.dto.marketdata.BinanceOrderbook;
import org.knowm.xchange.binance.dto.marketdata.BinanceTicker24h;
import org.knowm.xchange.binance.dto.marketdata.KlineInterval;
import org.knowm.xchange.binance.perpetualswap.BinancePerpetualAuthenticated;
import org.knowm.xchange.binance.dto.marketdata.BinancePremiumIndex;
import org.knowm.xchange.binance.dto.trade.FundimgRatereq;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Kline;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.utils.StreamUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.knowm.xchange.binance.BinanceResilience.REQUEST_WEIGHT_RATE_LIMITER;

public class BinancePerpetualMarketDataServiceRaw extends BinancePerpetualBaseService{
    public BinancePerpetualMarketDataServiceRaw(BinanceExchange exchange, BinancePerpetualAuthenticated binance, ResilienceRegistries resilienceRegistries) {
        super(exchange, binance, resilienceRegistries);
    }


    public BinanceOrderbook getBinanceOrderbook(CurrencyPair currencyPair, Integer limit) throws IOException {
        return decorateApiCall(() -> binance.depth(currencyPair.getParsing(""), limit))
                .withRetry(retry("depth"))
                .withRateLimiter(rateLimiter(REQUEST_WEIGHT_RATE_LIMITER), depthPermits(limit))
                .call();
    }

    public BinancePremiumIndex getPremiumIndexRaw(CurrencyPair currencyPair)throws IOException {
        return binance.getPremiumIndex(currencyPair.getParsing(""));
    }

    public List<BinancePremiumIndex> getAllPremiumIndexRaw()throws IOException {
        return binance.getAllPremiumIndex();
    }

    public List<FundimgRatereq>  getFundingRate(CurrencyPair currencyPair, Long startTime, Long endTime, Integer limit) throws IOException {
        return binance.getFundingRate(currencyPair.getParsing(""),startTime,endTime, limit);
    }

    public static OrderBook convertOrderBook(BinanceOrderbook ob, CurrencyPair pair) {
        List<LimitOrder> bids =
                ob.bids.entrySet().stream()
                        .map(e -> new LimitOrder(Order.OrderType.BID, e.getValue(), pair, null, null, e.getKey()))
                        .collect(Collectors.toList());
        List<LimitOrder> asks =
                ob.asks.entrySet().stream()
                        .map(e -> new LimitOrder(Order.OrderType.ASK, e.getValue(), pair, null, null, e.getKey()))
                        .collect(Collectors.toList());
        return new OrderBook(null, asks, bids);
    }

    protected int depthPermits(Integer limit) {
        if (limit == null || limit <= 100) {
            return 1;
        } else if (limit <= 500) {
            return 5;
        } else if (limit <= 1000) {
            return 10;
        }
        return 50;
    }

    public Kline lastKline(CurrencyPair pair, KlineInterval interval) throws IOException {
        return klines(pair, interval, 1, null, null).stream().collect(StreamUtils.singletonCollector());
    }

    public List<Kline> klines(CurrencyPair pair, KlineInterval interval) throws IOException {
        return klines(pair, interval, null, null, null);
    }

    public List<Kline> klines(CurrencyPair currencyPair, KlineInterval interval, Integer limit, Long startTime, Long endTime) throws IOException {
        List<Object[]> raw = decorateApiCall(() -> binance.klines(currencyPair.getParsing(""), interval.code(), limit, startTime, endTime))
                        .withRetry(retry("klines"))
                        .withRateLimiter(rateLimiter(REQUEST_WEIGHT_RATE_LIMITER))
                        .call();
        return raw.stream()
                .map(obj -> new Kline.Builder().from(currencyPair, obj).build())
                .collect(Collectors.toList());
    }

    public List<BinanceTicker24h> ticker24h() throws IOException {
        return decorateApiCall(() -> binance.ticker24h())
                .withRetry(retry("ticker24h"))
                .withRateLimiter(rateLimiter(REQUEST_WEIGHT_RATE_LIMITER), 40)
                .call();
    }

    public BinanceTicker24h ticker24h(CurrencyPair pair) throws IOException {
        BinanceTicker24h ticker24h =
                decorateApiCall(() -> binance.ticker24h(BinanceAdapters.toSymbol(pair)))
                        .withRetry(retry("ticker24h"))
                        .withRateLimiter(rateLimiter(REQUEST_WEIGHT_RATE_LIMITER))
                        .call();
        ticker24h.setCurrencyPair(pair);
        return ticker24h;
    }

}
