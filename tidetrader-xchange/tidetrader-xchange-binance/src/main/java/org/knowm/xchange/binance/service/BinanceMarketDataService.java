package org.knowm.xchange.binance.service;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.knowm.xchange.binance.BinanceAdapters;
import org.knowm.xchange.binance.BinanceAuthenticated;
import org.knowm.xchange.binance.BinanceErrorAdapter;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.dto.BinanceException;
import org.knowm.xchange.binance.dto.marketdata.*;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.marketdata.*;
import org.knowm.xchange.dto.marketdata.Trades.TradeSortType;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.marketdata.params.Params;
import org.knowm.xchange.service.marketdata.params.PeriodParams;

public class BinanceMarketDataService extends BinanceMarketDataServiceRaw implements MarketDataService {

  public BinanceMarketDataService(BinanceExchange exchange, BinanceAuthenticated binance, ResilienceRegistries resilienceRegistries) {
    super(exchange, binance, resilienceRegistries);
  }

  @Override
  public OrderBook getOrderBook(CurrencyPair pair, Object... args) throws IOException {
    try {
      int limitDepth = 100;

      if (args != null && args.length == 1) {
        Object arg0 = args[0];
        if (!(arg0 instanceof Integer)) {
          throw new ExchangeException("Argument 0 must be an Integer!");
        } else {
          limitDepth = (Integer) arg0;
        }
      }
      BinanceOrderbook binanceOrderbook = getBinanceOrderbook(pair, limitDepth);
      return convertOrderBook(binanceOrderbook, pair);
    } catch (BinanceException e) {
      throw BinanceErrorAdapter.adapt(e);
    }
  }

  public static OrderBook convertOrderBook(BinanceOrderbook ob, CurrencyPair pair) {
    List<LimitOrder> bids =
        ob.bids.entrySet().stream()
            .map(e -> new LimitOrder(OrderType.BID, e.getValue(), pair, null, null, e.getKey()))
            .collect(Collectors.toList());
    List<LimitOrder> asks =
        ob.asks.entrySet().stream()
            .map(e -> new LimitOrder(OrderType.ASK, e.getValue(), pair, null, null, e.getKey()))
            .collect(Collectors.toList());
    return new OrderBook(null, asks, bids);
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
   * optional parameters provided in the args array:
   *
   * <ul>
   *   <li>0: Long fromId optional, ID to get aggregate trades from INCLUSIVE.
   *   <li>1: Long startTime optional, Timestamp in ms to get aggregate trades from INCLUSIVE.
   *   <li>2: Long endTime optional, Timestamp in ms to get aggregate trades until INCLUSIVE.
   *   <li>3: Integer limit optional, Default 500; max 500.
   * </ul>
   *
   * <p>
   */
  @Override
  public Trades getTrades(CurrencyPair pair, Object... args) throws IOException {
    try {
      Long fromId = tradesArgument(args, 0, Long::valueOf);
      Long startTime = tradesArgument(args, 1, Long::valueOf);
      Long endTime = tradesArgument(args, 2, Long::valueOf);
      Integer limit = tradesArgument(args, 3, Integer::valueOf);
      List<BinanceAggTrades> aggTrades =
          binance.aggTrades(BinanceAdapters.toSymbol(pair), fromId, startTime, endTime, limit);
      List<Trade> trades =
          aggTrades.stream()
              .map(
                  at ->
                      new Trade.Builder()
                          .type(BinanceAdapters.convertType(at.buyerMaker))
                          .originalAmount(at.quantity)
                          .currencyPair(pair)
                          .price(at.price)
                          .timestamp(at.getTimestamp())
                          .id(Long.toString(at.aggregateTradeId))
                          .build())
              .collect(Collectors.toList());
      return new Trades(trades, TradeSortType.SortByTimestamp);
    } catch (BinanceException e) {
      throw BinanceErrorAdapter.adapt(e);
    }
  }

  private <T extends Number> T tradesArgument(
      Object[] args, int index, Function<String, T> converter) {
    if (index >= args.length) {
      return null;
    }
    Object arg = args[index];
    if (arg == null) {
      return null;
    }
    String argStr = arg.toString();
    try {
      return converter.apply(argStr);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          "Argument on index " + index + " is not a number: " + argStr, e);
    }
  }

  public List<Ticker> getAllBookTickers() throws IOException {
    List<BinancePriceQuantity> binanceTickers = tickerAllBookTickers();
    return BinanceAdapters.adaptPriceQuantities(binanceTickers);
  }
}
