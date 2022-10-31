package tide.trader.bot.strategy.internal;

import lombok.NonNull;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import tide.trader.bot.domain.Strategy;
import tide.trader.bot.dto.market.TickerDTO;
import tide.trader.bot.dto.position.PositionCreationResultDTO;
import tide.trader.bot.dto.position.PositionDTO;
import tide.trader.bot.dto.position.PositionRulesDTO;
import tide.trader.bot.dto.position.PositionStatusDTO;
import tide.trader.bot.dto.strategy.StrategyDomainDTO;
import tide.trader.bot.dto.trade.OrderCreationResultDTO;
import tide.trader.bot.dto.trade.OrderDTO;
import tide.trader.bot.dto.trade.TradeDTO;
import tide.trader.bot.dto.account.AccountDTO;
import tide.trader.bot.dto.account.BalanceDTO;
import tide.trader.bot.dto.util.*;
import tide.trader.bot.strategy.BasicCassandreStrategy;
import tide.trader.bot.strategy.BasicTa4jCassandreStrategy;
import tide.trader.bot.util.math.MathConstants;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.FLOOR;

/**
 * CassandreStrategy is the class that every strategy used by user ({@link BasicCassandreStrategy} or {@link BasicTa4jCassandreStrategy}) must extend.
 * It contains methods to access data and manage orders, trades, positions.
 * <p>
 * These are the classes used to manage a position.
 * - CassandreStrategyInterface list the methods a strategy type must implement to be able to interact with the Cassandre framework.
 * - CassandreStrategyConfiguration contains the configuration of the strategy.
 * - CassandreStrategyDependencies contains all the dependencies required by a strategy and provided by the Cassandre framework.
 * - CassandreStrategyImplementation is the default implementation of CassandreStrategyInterface, this code manages the interaction between Cassandre framework and a strategy.
 * - CassandreStrategy (class) is the class that every strategy used by user ({@link BasicCassandreStrategy} or {@link BasicTa4jCassandreStrategy}) must extend. It contains methods to access data and manage orders, trades, positions.
 * - CassandreStrategy (interface) is the annotation allowing you Cassandre to recognize a user strategy.
 * - BasicCassandreStrategy - User inherits this class this one to make a basic strategy.
 * - BasicCassandreStrategy - User inherits this class this one to make a strategy with ta4j.
 */
@SuppressWarnings("checkstyle:DesignForExtension")
public abstract class CassandreStrategy extends CassandreStrategyImplementation {

    // =================================================================================================================
    // Methods to retrieve data related to accounts.

    /**
     * Strategy loading parameter mechanism
     */
    public void initializeParameters(String jsonParameters) {
        // Can be implemented by a strategy developer.
    }

    /**
     * Returns the trade account selected by the strategy developer.
     *
     * @return trade account
     */
    public final Optional<AccountDTO> getTradeAccount() {
        return getTradeAccount(new LinkedHashSet<>(getAccounts().values()));
    }

    /**
     * Returns trade account balances.
     *
     * @return trade account balances
     */
    public final Map<CurrencyDTO, BalanceDTO> getTradeAccountBalances() {
        Map<CurrencyDTO, BalanceDTO> balances = new LinkedHashMap<>();
        getTradeAccount().ifPresent(accountDTO ->
                accountDTO.getBalances()
                        .forEach(balanceDTO -> balances.put(balanceDTO.getCurrency(), balanceDTO))
        );
        return balances;
    }

    /**
     * Returns list of accounts.
     *
     * @return accounts
     */
    public final Map<String, AccountDTO> getAccounts() {
        return userAccounts;
    }

    /**
     * Search and return an account by its id.
     *
     * @param accountId account id
     * @return account
     */
    public final Optional<AccountDTO> getAccountByAccountId(final String accountId) {
        if (userAccounts.containsKey(accountId)) {
            return Optional.of(userAccounts.get(accountId));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Returns the amounts locked by position.
     *
     * @return amounts locked
     */
    public final Map<Long, CurrencyAmountDTO> getAmountsLockedByPosition() {
        return dependencies.getPositionService().getAmountsLockedByPosition();
    }

    /**
     * Returns the amounts locked for a specific currency.
     *
     * @param currency currency
     * @return amounts locked
     */
    public final BigDecimal getAmountsLockedByCurrency(final CurrencyDTO currency) {
        return getAmountsLockedByPosition()
                .values()
                .stream()
                .filter(currencyAmount -> currencyAmount.getCurrency().equals(currency))
                .map(CurrencyAmountDTO::getValue)
                .reduce(ZERO, BigDecimal::add);
    }

    // =================================================================================================================
    // Methods to retrieve data related to tickers.

    /**
     * Return last received tickers.
     *
     * @return ticker
     */
    public final Map<CurrencyPairDTO, TickerDTO> getLastTickers() {
        return lastTickers;
    }

    /**
     * Return the list of imported tickers (ordered by timestamp).
     *
     * @return imported tickers
     */
    public final List<TickerDTO> getImportedTickers() {
        return dependencies.getImportedTickersRepository()
                .findByOrderByTimestampAsc()
                .stream()
                .map(TICKER_MAPPER::mapToTickerDTO)
                .collect(Collectors.toList());
    }

    /**
     * Return the list of imported tickers for a specific currency pair (ordered by timestamp).
     *
     * @param currencyPair currency pair
     * @return imported tickers
     */
    public final List<TickerDTO> getImportedTickers(@NonNull final CurrencyPairDTO currencyPair) {
        return dependencies.getImportedTickersRepository()
                .findByCurrencyPairOrderByTimestampAsc(currencyPair.toString())
                .stream()
                .map(TICKER_MAPPER::mapToTickerDTO)
                .collect(Collectors.toList());
    }

    // =================================================================================================================
    // Methods to retrieve data related to orders.

    /**
     * Returns list of orders (order id is key).
     *
     * @return orders
     */
    public final Map<String, OrderDTO> getOrders() {
        return dependencies.getOrderRepository().findByOrderByTimestampAsc()
                .stream()
                .filter(order -> order.getStrategy().getUid().equals(configuration.getStrategyUid()))
                .map(ORDER_MAPPER::mapToOrderDTO)
                .collect(Collectors.toMap(OrderDTO::getOrderId, orderDTO -> orderDTO));
    }

    /**
     * Get an order by its order id.
     *
     * @param orderId order id
     * @return order
     */
    public final Optional<OrderDTO> getOrderByOrderId(final String orderId) {
        return getOrders().values()
                .stream()
                .filter(order -> order.getOrderId().equals(orderId))
                .findFirst();
    }

    // =================================================================================================================
    // Methods to retrieve data related to trades.

    /**
     * Returns list of trades (trade id is key).
     *
     * @return trades
     */
    public final Map<String, TradeDTO> getTrades() {
        return dependencies.getTradeRepository().findByOrderByTimestampAsc()
                .stream()
                .filter(trade -> trade.getOrder().getStrategy().getUid().equals(configuration.getStrategyUid()))
                .map(TRADE_MAPPER::mapToTradeDTO)
                .collect(Collectors.toMap(TradeDTO::getTradeId, tradeDTO -> tradeDTO));
    }

    /**
     * Get a trade by its trade id.
     *
     * @param tradeId trade id
     * @return trade
     */
    public final Optional<TradeDTO> getTradeByTradeId(final String tradeId) {
        return getTrades().values()
                .stream()
                .filter(trade -> trade.getTradeId().equals(tradeId))
                .findFirst();
    }

    // =================================================================================================================
    // Methods to retrieve data related to positions.

    /**
     * Returns list of positions (position id the key).
     *
     * @return positions
     */
    public final Map<Long, PositionDTO> getPositions() {
        return dependencies.getPositionRepository()
                //.findByOrderByUid()
                .findByStrategyUid(configuration.getStrategyUid())
                .stream()
                //.filter(position -> position.getStrategy().getUid().equals(configuration.getStrategyUid()))
                .filter(position -> StringUtils.isNotBlank(position.getCurrencyPair()))
                .map(POSITION_MAPPER::mapToPositionDTO)
                .collect(Collectors.toMap(PositionDTO::getPositionId, positionDTO -> positionDTO));
    }

    /**
     * Returns list of positions (position id the key).
     * @param currencyPair
     * @return
     */
    public final Map<Long, PositionDTO> getPositions(CurrencyPairDTO currencyPair) {
        return getPositions()
                .values().stream()
                .filter(position -> position.getCurrencyPair().equals(currencyPair))
                .collect(Collectors.toMap(PositionDTO::getPositionId, positionDTO -> positionDTO));
    }

    /**
     * Returns list of positions (position id the key).
     * @param currencyPair
     * @param status
     * @return positions
     */
    public final Map<Long, PositionDTO> getPositions(CurrencyPairDTO currencyPair, PositionStatusDTO... status) {
        return getPositions(currencyPair)
                .values().stream()
                .filter(position -> (ArrayUtils.contains(status, position.getStatus())))
                .collect(Collectors.toMap(PositionDTO::getPositionId, positionDTO -> positionDTO));
    }

    /**
     * Get a position by its id.
     *
     * @param positionId position id
     * @return position
     */
    public final Optional<PositionDTO> getPositionByPositionId(final long positionId) {
        return getPositions()
                .values()
                .stream()
                .filter(positionDTO -> positionDTO.getPositionId() == positionId)
                .findFirst();
    }

    /**
     * Returns gains of all positions of the strategy.
     *
     * @return total gains
     */
    public final Map<CurrencyDTO, GainDTO> getGains() {
        return dependencies.getPositionService().getGains(configuration.getStrategyUid());
    }

    // =================================================================================================================
    // Methods to manage orders & positions (creation, cancellation, rules updates...).

    /**
     * Creates a buy market order.
     *
     * @param currencyPair currency pair
     * @param amount       amount
     * @return order result (order id or error)
     */
    public OrderCreationResultDTO createBuyMarketOrder(final CurrencyPairDTO currencyPair,
                                                       final BigDecimal amount) {
        return dependencies.getTradeService().createBuyMarketOrder(this, this.getPrecisionCurrencyPairDTO(currencyPair), amount);
    }

    /**
     * Creates a sell market order.
     *
     * @param currencyPair currency pair
     * @param amount       amount
     * @return order result (order id or error)
     */
    public OrderCreationResultDTO createSellMarketOrder(final CurrencyPairDTO currencyPair,
                                                        final BigDecimal amount) {
        return dependencies.getTradeService().createSellMarketOrder(this, this.getPrecisionCurrencyPairDTO(currencyPair), amount);
    }

    /**
     * Creates a buy limit order.
     *
     * @param currencyPair currency pair
     * @param amount       amount
     * @param limitPrice   the highest acceptable price
     * @return order result (order id or error)
     */
    @SuppressWarnings("unused")
    public OrderCreationResultDTO createBuyLimitOrder(final CurrencyPairDTO currencyPair,
                                                      final BigDecimal amount,
                                                      final BigDecimal limitPrice) {
        return dependencies.getTradeService().createBuyLimitOrder(this, this.getPrecisionCurrencyPairDTO(currencyPair), amount, limitPrice);
    }

    /**
     * Creates a sell limit order.
     *
     * @param currencyPair currency pair
     * @param amount       amount
     * @param limitPrice   the lowest acceptable price
     * @return order result (order id or error)
     */
    @SuppressWarnings("unused")
    public OrderCreationResultDTO createSellLimitOrder(final CurrencyPairDTO currencyPair,
                                                       final BigDecimal amount,
                                                       final BigDecimal limitPrice) {
        return dependencies.getTradeService().createSellLimitOrder(this, this.getPrecisionCurrencyPairDTO(currencyPair), amount, limitPrice);
    }

    /**
     * Cancel order.
     *
     * @param orderUid order uid
     * @return true if cancelled
     */
    @SuppressWarnings("unused")
    boolean cancelOrder(final long orderUid) {
        return dependencies.getTradeService().cancelOrder(orderUid);
    }

    /**
     * Creates a long position with its associated rules.
     * Long position is nothing but buying share.
     * If you are bullish (means you think that price of X share will rise) at that time you buy some amount of Share is called taking Long Position in share.
     *
     * @param currencyPair currency pair
     * @param amount       amount
     * @param rules        rules
     * @return position creation result
     */
    public PositionCreationResultDTO createLongPosition(final CurrencyPairDTO currencyPair,
                                                        final BigDecimal amount,
                                                        final PositionRulesDTO rules) {
        return dependencies.getPositionService().createLongPosition(this, this.getPrecisionCurrencyPairDTO(currencyPair), amount, rules);
    }

    /**
     * Creates a short position with its associated rules.
     * Short position is nothing but selling share.
     * If you are bearish (means you think that price of xyz share are going to fall) at that time you sell some amount of share is called taking Short Position in share.
     *
     * @param currencyPair currency pair
     * @param amount       amount
     * @param rules        rules
     * @return position creation result
     */
    public PositionCreationResultDTO createShortPosition(final CurrencyPairDTO currencyPair,
                                                         final BigDecimal amount,
                                                         final PositionRulesDTO rules) {
        return dependencies.getPositionService().createShortPosition(this, this.getPrecisionCurrencyPairDTO(currencyPair), amount, rules);
    }

    /**
     * Update position rules.
     *
     * @param positionUid position uid
     * @param newRules    new rules
     */
    public void updatePositionRules(final long positionUid, final PositionRulesDTO newRules) {
        dependencies.getPositionService().updatePositionRules(positionUid, newRules);
    }

    /**
     * Set auto close value on a specific position.
     * If true, Cassandre will close the position according to rules.
     * if false, Cassandre will never close the position.
     *
     * @param positionUid position uid
     * @param value       auto close value
     */
    public void setAutoClose(final long positionUid, final boolean value) {
        dependencies.getPositionService().setAutoClose(positionUid, value);
    }

    /**
     * Close position (no matter the rules).
     * The closing will happen when the next ticker arrives.
     *
     * @param positionId positionid
     * @param currencyPair currencyPair
     */
    public void closePosition(final long positionId, final CurrencyPairDTO currencyPair) {
        this.closePosition(positionId, currencyPair, "");
    }

    /**
     * Close position (no matter the rules).
     * The closing will happen when the next ticker arrives.
     *
     * @param positionId positionid
     * @param currencyPair currencyPair
     */
    public void closePosition(final long positionId, final CurrencyPairDTO currencyPair, String exitReason) {
        //dependencies.getPositionService().forcePositionClosing(position.getPositionId(), exitReason);
        dependencies.getPositionService().closePosition(this, positionId, getLastTickers().get(currencyPair), exitReason);
    }

    // =================================================================================================================
    // CanBuy & canSell methods.

    /**
     * Returns the amount of a currency I can buy with a certain amount of another currency.
     *
     * @param amountToUse    amount you want to use buy the currency you want
     * @param targetCurrency the currency you want to buy
     * @return amount of currencyWanted you can buy with amountToUse
     */
    public final Optional<BigDecimal> getEstimatedBuyableAmount(final CurrencyAmountDTO amountToUse,
                                                                final CurrencyDTO targetCurrency) {
        /*
            symbol=BTC-USDT
            {
              "time": 1637270267065,
              "sequence": "1622704211505",
              "price": "58098.3",
              "size": "0.00001747",
              "bestBid": "58098.2",
              "bestBidSize": "0.038",
              "bestAsk": "60000",
              "bestAskSize": "0.27476785"
            }
            This means 1 Bitcoin can be bought with 60000 USDT.
         */
        final TickerDTO ticker = lastTickers.get(new CurrencyPairDTO(targetCurrency, amountToUse.getCurrency()));
        if (ticker == null) {
            // No ticker for this currency pair.
            return Optional.empty();
        } else {
            // Make the calculation.
            // amountToUse: 150 000 USDT.
            // CurrencyWanted: BTC.
            // How much BTC I can buy ? amountToUse / last
            return Optional.of(amountToUse.getValue().divide(ticker.getLast(), MathConstants.BIGINTEGER_SCALE, FLOOR));
        }
    }

    /**
     * Returns the cost of buying an amount of a currency pair.
     *
     * @param currencyPair currency pair
     * @param amount       amount
     * @return cost
     */
    public final Optional<CurrencyAmountDTO> getEstimatedBuyingCost(final CurrencyPairDTO currencyPair,
                                                                    final BigDecimal amount) {
        /*
            symbol=ETH-BTC
            {
              "time": 1598626640265,
              "sequence": "1594421123246",
              "price": "0.034227",
              "size": "0.0200088",
              "bestBid": "0.034226",
              "bestBidSize": "6.3384368",
              "bestAsk": "0.034227",
              "bestAskSize": "18.6378851"
            }
            This means 1 Ether can be bought with 0.034227 Bitcoin.
         */

        // We get the last ticker from the last values received.
        final TickerDTO ticker = lastTickers.get(currencyPair);
        if (ticker == null) {
            // No ticker for this currency pair.
            return Optional.empty();
        } else {
            // Make the calculation.
            return Optional.of(CurrencyAmountDTO.builder()
                    .value(ticker.getLast().multiply(amount))
                    .currency(currencyPair.getQuoteCurrency())
                    .build());
        }
    }

    /**
     * Returns true if we have enough assets to buy.
     *
     * @param currencyPair currency pair
     * @param amount       amount
     * @return true if we have enough assets to buy
     */
    public final boolean canBuy(final CurrencyPairDTO currencyPair,
                                final BigDecimal amount) {
        final Optional<AccountDTO> tradeAccount = getTradeAccount(new LinkedHashSet<>(userAccounts.values()));
        return tradeAccount.filter(account -> canBuy(account, currencyPair, amount)).isPresent();
    }

    /**
     * Returns true if we have enough assets to buy.
     *
     * @param currencyPair            currency pair
     * @param amount                  amount
     * @param minimumBalanceLeftAfter minimum balance that should be left after buying
     * @return true if we have enough assets to buy
     */
    public final boolean canBuy(final CurrencyPairDTO currencyPair,
                                final BigDecimal amount,
                                final BigDecimal minimumBalanceLeftAfter) {
        final Optional<AccountDTO> tradeAccount = getTradeAccount(new LinkedHashSet<>(userAccounts.values()));
        return tradeAccount.filter(account -> canBuy(account, currencyPair, amount, minimumBalanceLeftAfter)).isPresent();
    }

    /**
     * Returns true if we have enough assets to buy.
     *
     * @param account      account
     * @param currencyPair currency pair
     * @param amount       amount
     * @return true if we have enough assets to buy
     */
    public final boolean canBuy(final AccountDTO account,
                                final CurrencyPairDTO currencyPair,
                                final BigDecimal amount) {
        return canBuy(account, currencyPair, amount, ZERO);
    }

    /**
     * Returns true if we have enough assets to buy and if minimumBalanceAfter is left on the account after.
     *
     * @param account                 account
     * @param currencyPair            currency pair
     * @param amount                  amount
     * @param minimumBalanceLeftAfter minimum balance that should be left after buying
     * @return true if we have enough assets to buy
     */
    public final boolean canBuy(final AccountDTO account,
                                final CurrencyPairDTO currencyPair,
                                final BigDecimal amount,
                                final BigDecimal minimumBalanceLeftAfter) {
        // We get the amount.
        final Optional<BalanceDTO> balance = account.getBalance(currencyPair.getQuoteCurrency());
        if (balance.isPresent()) {
            //Minimum amount of exchange
            if(!this.canTradeMinimumQuote(currencyPair, amount)) {
                return false;
            }
            // We get the estimated cost of buying.
            final Optional<CurrencyAmountDTO> estimatedBuyingCost = getEstimatedBuyingCost(currencyPair, amount);

            //If the short mode, calculate the amount multiple
            final BigDecimal balanceAvailable;
            if(isShort()) {
                balanceAvailable = balance.get().getAvailable().multiply(new BigDecimal(getConfiguration().getLeverage()));
            } else {
                balanceAvailable = balance.get().getAvailable();
            }
            // We calculate.
            // Balance in the account
            // Minus
            // Estimated cost
            // Must be superior to zero
            // If there is no way to calculate the price for the moment (no ticker).
            return estimatedBuyingCost.filter(currencyAmountDTO -> balanceAvailable
                    .subtract(currencyAmountDTO.getValue().add(minimumBalanceLeftAfter))/*.add(getAmountsLockedByCurrency(currencyAmountDTO.getCurrency()))*/
                    .compareTo(ZERO) > 0).isPresent();
        } else {
            // If the is no balance in this currency, we can't buy.
            return false;
        }
    }

    /**
     * Returns true if we have enough assets to sell.
     *
     * @param currencyPair currency
     * @param amount   amount
     * @return true if we have enough assets to sell
     */
    public final boolean canSell(final CurrencyPairDTO currencyPair,
                                 final BigDecimal amount) {
        final Optional<AccountDTO> tradeAccount = getTradeAccount(new LinkedHashSet<>(userAccounts.values()));
        return tradeAccount.filter(account -> canSell(account, currencyPair, amount)).isPresent();
    }

    /**
     * Returns true if we have enough assets to sell.
     *
     * @param currencyPair                currency
     * @param amount                  amount
     * @param minimumBalanceLeftAfter minimum balance that should be left after selling
     * @return true if we have enough assets to sell
     */
    public final boolean canSell(final CurrencyPairDTO currencyPair,
                                 final BigDecimal amount,
                                 final BigDecimal minimumBalanceLeftAfter) {
        final Optional<AccountDTO> tradeAccount = getTradeAccount(new LinkedHashSet<>(userAccounts.values()));
        return tradeAccount.filter(account -> canSell(account, currencyPair, amount, minimumBalanceLeftAfter)).isPresent();
    }

    /**
     * Returns true if we have enough assets to sell.
     *
     * @param account  account
     * @param currencyPair currency pair
     * @param amount   amount
     * @return true if we have enough assets to sell
     */
    public final boolean canSell(final AccountDTO account,
                                 final CurrencyPairDTO currencyPair,
                                 final BigDecimal amount) {
        return canSell(account, currencyPair, amount, ZERO);
    }

    /**
     * Returns true if we have enough assets to sell and if minimumBalanceAfter is left on the account after.
     * @param account account
     * @param currencyPair currencyPair
     * @param amount amount
     * @param minimumBalanceLeftAfter minimum balance that should be left after selling
     * @return rue if we have enough assets to sell
     */
    public final boolean canSell(final AccountDTO account,
                                 final CurrencyPairDTO currencyPair,
                                 final BigDecimal amount,
                                 final BigDecimal minimumBalanceLeftAfter) {

        if(getConfiguration().getStrategyDTO().getDomain() == StrategyDomainDTO.PERPETUAL) {
            if(!this.canTradeMinimumQuote(currencyPair, amount)) {
                return false;
            }

            final Optional<BalanceDTO> balance = account.getBalance(currencyPair.getQuoteCurrency());
            final BigDecimal ownedQuote = balance.get().getAvailable().multiply(new BigDecimal(getConfiguration().getLeverage()));
            final BigDecimal last = this.getLastPriceForCurrencyPair(currencyPair);
            if(last != null) {
                final BigDecimal spendQuote = last.multiply(amount);
                return ownedQuote.subtract(spendQuote).compareTo(ZERO) > 0;
            }
            return false;
        } else {
            final CurrencyDTO currency = currencyPair.getBaseCurrency();
            // We get the amount.
            final Optional<BalanceDTO> balance = account.getBalance(currency);
            // public int compareTo(BigDecimal bg) returns
            // 1: if value of this BigDecimal is greater than that of BigDecimal object passed as parameter.
            // If the is no balance in this currency, we can't buy.
            return balance.filter(balanceDTO -> balanceDTO.getAvailable().subtract(amount).subtract(minimumBalanceLeftAfter).subtract(getAmountsLockedByCurrency(currency)).compareTo(ZERO) > 0
                    /*|| balanceDTO.getAvailable().subtract(amount).subtract(minimumBalanceLeftAfter).subtract(getAmountsLockedByCurrency(currency)).compareTo(ZERO) == 0*/).isPresent();
        }
    }

    /**
     * Returns Is it in line with the minimum cost of the exchange
     *
     * @param currencyPair currency pair
     * @param amount       amount
     * @return cost
     */
    public final boolean canTradeMinimumQuote(final CurrencyPairDTO currencyPair, final BigDecimal amount) {
        //Minimum amount of exchange
        CurrencyPairMetaDataDTO currencyPairMetaData = this.dependencies.getExchangeService().getCurrencyPairMetaData(currencyPair);
        if(currencyPairMetaData == null) {
            currencyPairMetaData = CurrencyPairMetaDataDTO.builder().counterMinimumAmount(BigDecimal.TEN).currencyPair(currencyPair).baseScale(8).build();
        }

        // We get the last ticker from the last values received.
        final TickerDTO ticker = lastTickers.get(currencyPair);
        if (ticker == null) {
            // No ticker for this currency pair.
            return false;
        } else {
            BigDecimal newAmount = amount.setScale(currencyPairMetaData.getBaseScale(), FLOOR);
            // Make the calculation.
            return ticker.getLast().multiply(newAmount).compareTo(currencyPairMetaData.getCounterMinimumAmount()) > 0;
        }
    }

    /**
     * Return exchange currencyPair amount precision
     * @param currencyPair
     * @return
     */
    private final CurrencyPairDTO getPrecisionCurrencyPairDTO(final CurrencyPairDTO currencyPair) {
        //Minimum amount of exchange
        final CurrencyPairMetaDataDTO currencyPairMetaData = this.dependencies.getExchangeService().getCurrencyPairMetaData(currencyPair);
        if(currencyPairMetaData == null) {
            return currencyPair;
        }
        return new CurrencyPairDTO(currencyPair.getBaseCurrency(), currencyPair.getQuoteCurrency(), currencyPairMetaData.getBaseScale(), currencyPairMetaData.getPriceScale());
    }

    /**
     * Calculation of handling charges
     * @param amount
     * @return
     */
    public final BigDecimal takeFee(final BigDecimal amount) {
        return amount.multiply(BigDecimal.ONE.subtract(this.dependencies.getExchangeService().getTradingFee()));
    }

    /**
     * Calculate the actual purchase or sale volume of the exchange
     * @param currencyPair
     * @param amount
     * @return
     */
    public final BigDecimal takePrecisionAmount(final CurrencyPairDTO currencyPair, final BigDecimal amount) {
        //Minimum amount of exchange
        CurrencyPairMetaDataDTO currencyPairMetaData = this.dependencies.getExchangeService().getCurrencyPairMetaData(currencyPair);
        if(currencyPairMetaData == null) {
            currencyPairMetaData = CurrencyPairMetaDataDTO.builder().counterMinimumAmount(BigDecimal.TEN).currencyPair(currencyPair).baseScale(8).build();
        }
        return amount.setScale(currencyPairMetaData.getBaseScale(), FLOOR);
    }

    /**
     * is Back test mode
     * @return
     */
    public final boolean isSimulated(){
        return this.dependencies.getExchangeService().isSimulatedExchange();
    }

    /**
     * is Back test mode
     * @return
     */
    public final boolean isShort(){
        return this.configuration.isShort();
    }

    /**
     * New message push
     * @param title
     * @param body
     */
    public final void addMessage(String title, String body) {
        this.dependencies.getMessageService().newMessage(this.configuration.getStrategyDTO(), title, body);
    }

    /**
     * Get real-time Ticker
     * @return
     */
    public ColumnsDTO getColumnTickers() {
        //Market data
        ColumnsDTO tickerColumns = new ColumnsDTO("Latest Tickers");
        tickerColumns.setColName("CurrencyPair", "Open", "Close", "High", "Low", "Time");
        this.getLastTickers().values().forEach(tickerDTO -> {
            tickerColumns.addRow(tickerDTO.getCurrencyPair().toString(), tickerDTO.getOpen().stripTrailingZeros(), tickerDTO.getLast().stripTrailingZeros(), tickerDTO.getHigh().stripTrailingZeros(), tickerDTO.getLow().stripTrailingZeros(), tickerDTO.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        });
        return tickerColumns;
    }

    /**
     * Get real-time Balance
     * @return
     */
    public ColumnsDTO getColumnBalances() {
        //Account balance
        ColumnsDTO balanceColumn = new ColumnsDTO("Latest Balances");
        balanceColumn.setColName("Currency", "Available", "Frozen", "Total");
        List<CurrencyDTO> currencyDTOList = this.getRequestedCurrencyPairs().stream().map(currencyPairDTO -> currencyPairDTO.getBaseCurrency()).collect(Collectors.toList());
        this.getRequestedCurrencyPairs().stream().forEach(currencyPairDTO -> {
            if(!currencyDTOList.contains(currencyPairDTO.getQuoteCurrency())){
                currencyDTOList.add(currencyPairDTO.getQuoteCurrency());
            }
        });
        this.getTradeAccountBalances().values().stream().forEach(balanceDTO -> {
            if(currencyDTOList.contains(balanceDTO.getCurrency())) {
                balanceColumn.addRow(balanceDTO.getCurrency().getSymbol(), balanceDTO.getAvailable().stripTrailingZeros(), balanceDTO.getFrozen().stripTrailingZeros(), balanceDTO.getTotal().stripTrailingZeros());
            }
        });

        return balanceColumn;
    }

    /**
     * Get current bin information
     * @return
     */
    public ColumnsDTO getColumnPositions(PositionStatusDTO status) {
        if(status == PositionStatusDTO.OPENED) {
            ColumnsDTO positionColumn = new ColumnsDTO("Opened Positions");
            positionColumn.setColName("CurrencyPair", "Side", "Quantity", "OpenPrice", "OpenTime", "LatestPrice", "LatestTime", "UnrealizedGain", "Action");
            this.getPositions().values().stream().filter(p -> status == p.getStatus()).forEach(p -> {
                String close = "<a href=\"javascript:closePostion('/strategy/close/" + configuration.getStrategyDTO().getStrategyId() + "/" + p.getUid() + "/"+ p.getCurrencyPair().getBaseCurrency() + "_" + p.getCurrencyPair().getQuoteCurrency() + "');\" ><i class='fas fa-times'></i></a>";
                Optional<GainDTO> latestGain = p.getLatestCalculatedGain();
                positionColumn.addRow(p.getCurrencyPair().toString(),
                        p.getType(), p.getAmount().getValue().stripTrailingZeros().toPlainString(),
                        p.getOpeningOrder().getAveragePriceValue().stripTrailingZeros().toPlainString(),
                        p.getOpeningOrder().getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        (p.getLatestGainPrice() == null ? ZERO : p.getLatestGainPrice().getValue().stripTrailingZeros()),
                        (getLastTickers().containsKey(p.getCurrencyPair()) ? getLastTickers().get(p.getCurrencyPair()).getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "" ),
                        (latestGain.isEmpty() ? ZERO : latestGain.get().getAmount().getValue().stripTrailingZeros()),
                        close);
            });
            return positionColumn;
        } else if(status == PositionStatusDTO.CLOSED) {
            ColumnsDTO positionColumn = new ColumnsDTO("Closed Positions");
            positionColumn.setColName("CurrencyPair", "Side", "Quantity", "OpenPrice", "OpenTime", "ClosePrice", "CloseTime", "Gain", "Percentage");
            this.getPositions().values().stream().filter(p -> p.getStatus().equals(PositionStatusDTO.CLOSED)).forEach(p -> {
                positionColumn.addReversedRow(p.getCurrencyPair().toString(), p.getType(), p.getAmount().getValue().stripTrailingZeros(),
                        p.getOpeningOrder().getAveragePriceValue().stripTrailingZeros().toPlainString(), p.getOpeningOrder().getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        p.getClosingOrder().getAveragePriceValue().stripTrailingZeros().toPlainString(), p.getClosingOrder().getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        p.getGain().getAmount().getValue().stripTrailingZeros(), p.getGain().getPercentage() + "%");
            });
            return positionColumn;
        }
        return null;
    }

}
