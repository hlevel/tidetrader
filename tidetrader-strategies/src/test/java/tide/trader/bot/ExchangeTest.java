package tide.trader.bot;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.derivative.Domain;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.OpenPosition;
import org.knowm.xchange.dto.marketdata.Kline;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.dto.trade.*;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.marketdata.params.CurrencyPairsParam;
import org.knowm.xchange.service.marketdata.params.DefaultCancelOrderByClientOrderIdParams;
import org.knowm.xchange.service.marketdata.params.PeriodParams;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.CancelOrderByClientOrderIdParams;
import org.knowm.xchange.service.trade.params.DefaultTradeHistoryParamCurrencyPair;
import org.knowm.xchange.service.trade.params.TradeHistoryParamsAll;
import org.knowm.xchange.service.trade.params.orders.DefaultQueryOrderParamCurrencyPair;
import org.knowm.xchange.service.trade.params.orders.OrderQueryParamCurrencyPair;
import tide.trader.bot.common.extensions.TickerHistoryBackFill;
import tide.trader.bot.dto.market.TickerDTO;
import tide.trader.bot.dto.util.CurrencyPairDTO;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;


public class ExchangeTest {

    /**
     * 加载历史数据
     */
    @Test
    public void loaderHistoryTickers() {
        ExchangeSpecification specification = new BinanceExchange().getDefaultExchangeSpecification();
        specification.setUserName("historyLoader");
        //specification.setProxyHost("localhost");
        //specification.setProxyPort(1087);
        Exchange exchange = ExchangeFactory.INSTANCE.createExchange(specification, Domain.PERPETUAL);

        TickerHistoryBackFill tickerHistoryBackFill = new TickerHistoryBackFill(exchange, "src/test/resources");
        //tickerHistoryBackFill.backfillFrom(LocalDate.of(2021, 9, 5).atStartOfDay(), Duration.ofHours(4));
        tickerHistoryBackFill.backfillFrom(LocalDate.of(2022, 2, 20).atStartOfDay(), Duration.ofDays(1));
        tickerHistoryBackFill.backfillHistoryListFile(new CurrencyPairDTO("BTC", "USDT"));
    }

}
