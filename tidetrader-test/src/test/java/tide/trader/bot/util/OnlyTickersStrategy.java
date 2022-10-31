package tide.trader.bot.util;

import lombok.Getter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import tide.trader.bot.dto.market.TickerDTO;
import tide.trader.bot.dto.account.AccountDTO;
import tide.trader.bot.dto.util.CurrencyPairDTO;
import tide.trader.bot.strategy.BasicCassandreStrategy;
import tide.trader.bot.strategy.CassandreStrategy;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@CassandreStrategy
@ConditionalOnProperty(
        value = BaseTest.PARAMETER_ONLY_TICKERS_STRATEGY_ENABLED,
        havingValue = "true")
@Getter
public class OnlyTickersStrategy extends BasicCassandreStrategy {

    /** Sequence - Which service call are we treating. */
    private final AtomicLong sequence = new AtomicLong(1);

    private final Map<Long, Map<CurrencyPairDTO, TickerDTO>> tickersReceived = new LinkedHashMap<>();

    @Override
    public Set<CurrencyPairDTO> getRequestedCurrencyPairs() {
        Set<CurrencyPairDTO> list = new LinkedHashSet<>();
        list.add(BaseTest.BTC_USDT);
        list.add(BaseTest.ETH_USDT);
        list.add(BaseTest.KCS_USDT);
        list.add(BaseTest.KCS_BTC);  // Currency pair not in the imported files.
        return list;
    }

    @Override
    public Optional<AccountDTO> getTradeAccount(Set<AccountDTO> accounts) {
        return accounts.stream().filter(a -> "trade".equals(a.getAccountId())).findFirst();
    }

    @Override
    public void onTickersUpdates(Map<CurrencyPairDTO, TickerDTO> tickers) {
        // In this method, to allow testing, we retrieve all the reply for each call.
        if (!tickers.isEmpty()) {
            tickersReceived.put(sequence.getAndIncrement(), tickers);
        }
    }

}
