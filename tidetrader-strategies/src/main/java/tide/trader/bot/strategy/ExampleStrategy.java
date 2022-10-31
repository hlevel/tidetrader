package tide.trader.bot.strategy;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import tide.trader.bot.dto.account.AccountDTO;
import tide.trader.bot.dto.util.CurrencyPairDTO;
import tide.trader.bot.util.JacksonUtil;

import java.util.Optional;
import java.util.Set;

//@CassandreStrategy(strategyId = "1", strategyName = "ExampleStrategy")
public class ExampleStrategy extends BasicCassandreStrategy{

    @Override
    public void initializeParameters(String jsonParameters) {
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
