package org.knowm.xchange.binance.perpetualswap.service;

import org.knowm.xchange.binance.BinanceErrorAdapter;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.dto.BinanceException;
import org.knowm.xchange.binance.dto.marketdata.BinanceOrderbook;
import org.knowm.xchange.binance.dto.marketdata.BinanceTicker24h;
import org.knowm.xchange.binance.dto.marketdata.KlineInterval;
import org.knowm.xchange.binance.perpetualswap.BinancePerpetualAuthenticated;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Kline;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.marketdata.params.Params;
import org.knowm.xchange.service.marketdata.params.PeriodParams;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author chuxianbo
 */
public class BinancePerpetualMarketDataService extends BinancePerpetualMarketDataServiceRaw  implements MarketDataService {

    public BinancePerpetualMarketDataService(BinanceExchange exchange, BinancePerpetualAuthenticated binance, ResilienceRegistries resilienceRegistries) {
        super(exchange, binance, resilienceRegistries);
    }

    @Override
    public Kline getKline(Instrument instrument, Object... args) throws IOException {
        try {
            KlineInterval interval = args.length > 0 ? KlineInterval.lookup(Long.valueOf(args[0].toString())) : KlineInterval.m1;
            return lastKline((CurrencyPair) instrument, interval);
        } catch (BinanceException e) {
            throw BinanceErrorAdapter.adapt(e);
        }
    }

    @Override
    public List<Kline> getKlines(Params params) throws IOException {
        try {
            PeriodParams period = (PeriodParams) params;
            KlineInterval interval = KlineInterval.lookup(Long.valueOf(period.millis()));
            return klines(period.getCurrencyPair(), interval, period.durationMaximum(), period.startTime(), period.endTime());
        } catch (BinanceException e) {
            throw BinanceErrorAdapter.adapt(e);
        }
    }
    @Override
    public List<Ticker> getTicker(Params params) throws IOException {
        try {
            return ticker24h().stream().map(BinanceTicker24h::toTicker).collect(Collectors.toList());
        } catch (BinanceException e) {
            throw BinanceErrorAdapter.adapt(e);
        }
    }

    @Override
    public Ticker getTicker(Instrument instrument) throws IOException {
        try {
            return ticker24h((CurrencyPair) instrument).toTicker();
        } catch (BinanceException e) {
            throw BinanceErrorAdapter.adapt(e);
        }
    }

    /**
     *  @param args args[0] limit  false  Integer
     *
     * */
    @Override
    public OrderBook getOrderBook(CurrencyPair currencyPair, Object... args) throws IOException {
        Integer limit = 10;
        if (args != null && args.length > 0) {
            if (args[0] instanceof Integer && (Integer) args[0] > 0) {
                limit = (Integer) args[0];
            }
        }
        BinanceOrderbook binanceOrderbook = getBinanceOrderbook(currencyPair, limit);
        return convertOrderBook(binanceOrderbook, currencyPair);
    }

/*
    @Override
    public List<Ticker> getTickers(CurrencyPair currencyPair, KlineInterval interval, Integer limit) throws IOException {
        return tickers(currencyPair,  interval,  limit, null, null);
    }


    @Override
    public PremiumIndex getPremiumIndex(ParsingCurrencyPair pair)throws IOException {
       return getPremiumIndexRaw(pair);
    }

    @Override
    public List<PremiumIndex> getAllPremiumIndex() throws IOException {
        return new ArrayList<PremiumIndex>(getAllPremiumIndexRaw());
    }


    @Override
    public List<FundimgRatereq> getFundingRate(ParsingCurrencyPair pair, Long startTime, Long endTime, Integer limit) throws IOException {
        return super.getFundingRate(pair, startTime, endTime, limit);
    }
*/

}
