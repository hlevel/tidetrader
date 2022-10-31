package tide.trader.bot.service;

import tide.trader.bot.dto.account.AccountDTO;
import tide.trader.bot.dto.market.TickerDTO;
import tide.trader.bot.dto.util.CurrencyPairDTO;
import tide.trader.bot.util.java.ZonedDateTimeBetween;
import tide.trader.bot.util.ta4j.DurationMaximumBar;

import java.time.Duration;
import java.util.*;

/**
 * Service getting information about market prices.
 */
public interface MarketService {

    /**
     * Get a ticker for a currency pair.
     *
     * @param currencyPair currency pair
     * @return ticker
     */
    Optional<TickerDTO> getTicker(CurrencyPairDTO currencyPair);

    /**
     * Get tickers for several currency pairs.
     *
     * @param currencyPairs currency pairs
     * @return tickers
     */
    Set<TickerDTO> getTickers(Set<CurrencyPairDTO> currencyPairs);

    /**
     * Get history tickers for several currency pairs.
     * @param currencyPair
     * @param duration
     * @param between
     * @return
     */
    default List<TickerDTO> getHistoryTickers(CurrencyPairDTO currencyPair, Duration duration, ZonedDateTimeBetween between) {
        return Collections.emptyList();
    }

    /**
     * Retrieve tickers information from cache
     * @return accounts
     */
    default Set<TickerDTO> getTickersFromCache(){
        return Collections.emptySet();
    }

    /**
     * Retrieve history tickers information from cache
     * @param currencyPair
     * @param duration
     * @return
     */
    default List<TickerDTO> getHistoryTickersFromCache(CurrencyPairDTO currencyPair, Duration duration) {
        return Collections.emptyList();
    }

}
