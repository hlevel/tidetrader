package tide.trader.bot.service;

import tide.trader.bot.dto.util.CurrencyPairDTO;
import tide.trader.bot.dto.util.CurrencyPairMetaDataDTO;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Service getting information about the exchange features.
 */
public interface ExchangeService {

    /**
     * Get the list of available currency pairs for trading.
     *
     * @return list of currency pairs
     */
    Set<CurrencyPairDTO> getAvailableCurrencyPairs();

    /**
     * Get the list of available currency pairs for trading.
     *
     * @return list of currency pairs
     */
    CurrencyPairMetaDataDTO getCurrencyPairMetaData(CurrencyPairDTO currencyPair);

    /**
     * Exchange rate
     * @return
     */
    BigDecimal getTradingFee();

    /**
     * is Simulated mode
     * @return
     */
    boolean isSimulatedExchange();


}
