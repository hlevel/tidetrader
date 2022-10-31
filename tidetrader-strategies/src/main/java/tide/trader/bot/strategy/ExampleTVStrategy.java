package tide.trader.bot.strategy;

import tide.trader.bot.dto.account.AccountDTO;
import tide.trader.bot.dto.trade.SideDTO;
import tide.trader.bot.dto.util.CurrencyPairDTO;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

//@CassandreStrategy(strategyId = "3", strategyName = "ExampleTVStrategy")
public class ExampleTVStrategy extends BasicSingalCassandreStrategy{

    @Override
    public boolean shouldEnter(CurrencyPairDTO currencyPair, SideDTO type, BigDecimal price) {
        return false;
    }

    @Override
    public boolean shouldExit(CurrencyPairDTO currencyPair, BigDecimal price) {
        return false;
    }

    @Override
    public Set<CurrencyPairDTO> getRequestedCurrencyPairs() {
        return null;
    }

    @Override
    public Optional<AccountDTO> getTradeAccount(Set<AccountDTO> accounts) {
        return Optional.empty();
    }
}
