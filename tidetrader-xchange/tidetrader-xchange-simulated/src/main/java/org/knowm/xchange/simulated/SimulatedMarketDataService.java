package org.knowm.xchange.simulated;

import java.io.IOException;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.service.BaseExchangeService;
import org.knowm.xchange.service.marketdata.MarketDataService;

public class SimulatedMarketDataService extends BaseExchangeService<SimulatedExchange>
    implements MarketDataService {

  protected SimulatedMarketDataService(SimulatedExchange exchange) {
    super(exchange);
  }

  @Override
  public Ticker getTicker(Instrument instrument) throws IOException {
    exchange.maybeThrow();
    return exchange.getEngine((CurrencyPair) instrument).ticker();
  }

  @Override
  public OrderBook getOrderBook(CurrencyPair currencyPair, Object... args) throws IOException {
    exchange.maybeThrow();
    return exchange.getEngine(currencyPair).getLevel2OrderBook();
  }

  @Override
  public Trades getTrades(CurrencyPair currencyPair, Object... args) throws IOException {
    exchange.maybeThrow();
    return new Trades(exchange.getEngine(currencyPair).publicTrades());
  }
}
