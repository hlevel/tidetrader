package tide.trader.bot.configuration;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import reactor.core.publisher.ConnectableFlux;
import tide.trader.bot.batch.AccountFlux;
import tide.trader.bot.batch.OrderFlux;
import tide.trader.bot.batch.PositionFlux;
import tide.trader.bot.batch.TickerFlux;
import tide.trader.bot.batch.TradeFlux;
import tide.trader.bot.domain.ImportedTicker;
import tide.trader.bot.domain.Strategy;
import tide.trader.bot.dto.market.TickerDTO;
import tide.trader.bot.dto.position.PositionDTO;
import tide.trader.bot.dto.strategy.StrategyDTO;
import tide.trader.bot.dto.strategy.StrategyDomainDTO;
import tide.trader.bot.dto.trade.OrderDTO;
import tide.trader.bot.dto.trade.TradeDTO;
import tide.trader.bot.dto.account.AccountDTO;
import tide.trader.bot.dto.account.UserDTO;
import tide.trader.bot.dto.util.CurrencyPairDTO;
import tide.trader.bot.repository.*;
import tide.trader.bot.service.*;
import tide.trader.bot.strategy.BasicCassandreStrategy;
import tide.trader.bot.strategy.BasicSingalCassandreStrategy;
import tide.trader.bot.strategy.BasicTa4jCassandreStrategy;
import tide.trader.bot.strategy.CassandreStrategy;
import tide.trader.bot.strategy.internal.CassandreStrategyConfiguration;
import tide.trader.bot.strategy.internal.CassandreStrategyDependencies;
import tide.trader.bot.strategy.internal.CassandreStrategyInterface;
import tide.trader.bot.util.base.Base;
import tide.trader.bot.util.base.configuration.BaseConfiguration;
import tide.trader.bot.util.exception.ConfigurationException;
import tide.trader.bot.util.parameters.ExchangeParameters;
import tide.trader.bot.dto.position.PositionStatusDTO;
import tide.trader.bot.dto.strategy.StrategyTypeDTO;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.math.BigDecimal.ZERO;

/**
 * StrategyAutoConfiguration configures the strategies.
 */
@Configuration
@EnableConfigurationProperties(ExchangeParameters.class)
@RequiredArgsConstructor
public class StrategiesAutoConfiguration extends BaseConfiguration {

    /** Application context. */
    private final ApplicationContext applicationContext;

    /** Exchange parameters. */
    private final ExchangeParameters exchangeParameters;

    /** Strategy repository. */
    private final StrategyRepository strategyRepository;

    /** Order repository. */
    private final OrderRepository orderRepository;

    /** Trade repository. */
    private final TradeRepository tradeRepository;

    /** Position repository. */
    private final PositionRepository positionRepository;

    /** Imported tickers' repository. */
    private final ImportedTickersRepository importedTickersRepository;

    /** signalRepository repository. */
    private final SignalRepository signalRepository;

    /** Exchange service. */
    private final ExchangeService exchangeService;

    /** User service. */
    private final UserService userService;

    /** Trade service. */
    private final TradeService tradeService;

    /** Position service. */
    private final PositionService positionService;

    /** Market service. */
    private final MarketService marketService;

    /** Message service. */
    private final MessageService messageService;

    /** Account flux. */
    private final AccountFlux accountFlux;

    /** Ticker flux. */
    private final TickerFlux tickerFlux;

    /** Order flux. */
    private final OrderFlux orderFlux;

    /** Trade flux. */
    private final TradeFlux tradeFlux;

    /** Position flux. */
    private final PositionFlux positionFlux;

    /**
     * Search for strategies and runs them.
     */
    @PostConstruct
    @SuppressWarnings("checkstyle:MethodLength")
    public void configure() {
        // Retrieving all the beans have the @Strategy annotation.
        final Map<String, Object> strategies = applicationContext.getBeansWithAnnotation(CassandreStrategy.class);
        // =============================================================================================================
        // Configuration check.
        // We run tests to display and check if everything is ok with the configuration.
        final UserDTO user = checkConfiguration(strategies);

        // =============================================================================================================
        // Maintenance code.
        // If a position is blocked in OPENING or CLOSING, we send again the trades.
        // This could happen if cassandre crashes after saving a trade and did not have time to send it to
        // positionService. Here we force the status recalculation in PositionDTO, and we save it.
        positionRepository.findByStatusIn(Stream.of(PositionStatusDTO.OPENING, PositionStatusDTO.CLOSING).collect(Collectors.toList()))
                .stream()
                .map(Base.POSITION_MAPPER::mapToPositionDTO)
                .map(Base.POSITION_MAPPER::mapToPosition)
                .forEach(positionRepository::save);

        // =============================================================================================================
        // Importing user tickers into database.
        // Feature documentation is here: https://trading-bot.cassandre.tech/learn/import-historical-data.html
        loadImportedTickers();

        // =============================================================================================================
        // Creating flux.
        final ConnectableFlux<Set<AccountDTO>> connectableAccountFlux = accountFlux.getFlux().publish();
        final ConnectableFlux<Set<PositionDTO>> connectablePositionFlux = positionFlux.getFlux().publish();
        final ConnectableFlux<Set<OrderDTO>> connectableOrderFlux = orderFlux.getFlux().publish();
        final ConnectableFlux<Set<TickerDTO>> connectableTickerFlux = tickerFlux.getFlux().publish();
        final ConnectableFlux<Set<TradeDTO>> connectableTradeFlux = tradeFlux.getFlux().publish();

        // =============================================================================================================
        // Configuring strategies.
        // Data in database, services, flux...
        logger.info("Running the following strategies:");
        strategies.values()
                .forEach(s -> {
                    CassandreStrategyInterface strategy = (CassandreStrategyInterface) s;
                    CassandreStrategy annotation = s.getClass().getAnnotation(CassandreStrategy.class);

                    // Retrieving strategy information from annotation.
                    final String strategyId = annotation.strategyId();
                    final String strategyName = annotation.strategyName();

                    // StrategyDTO : saving or updating the strategy in database.
                    StrategyDTO strategyDTO;
                    final Optional<Strategy> strategyInDatabase = strategyRepository.findByStrategyId(annotation.strategyId());
                    if (strategyInDatabase.isEmpty()) {
                        // =============================================================================================
                        // If the strategy is NOT in database.
                        Strategy newStrategy = new Strategy();
                        newStrategy.setStrategyId(annotation.strategyId());
                        newStrategy.setName(annotation.strategyName());
                        if (strategy instanceof BasicCassandreStrategy) {
                            newStrategy.setType(StrategyTypeDTO.BASIC_STRATEGY);
                        }
                        if (strategy instanceof BasicTa4jCassandreStrategy) {
                            newStrategy.setType(StrategyTypeDTO.BASIC_TA4J_STRATEGY);
                        }
                        if (strategy instanceof BasicSingalCassandreStrategy) {
                            newStrategy.setType(StrategyTypeDTO.BASIC_SINGAL_STRATEGY);
                        }
                        newStrategy.setDomain(StrategyDomainDTO.valueOf(exchangeParameters.getDomain().toUpperCase()));
                        newStrategy.setClassName(strategy.getClass().getSimpleName());
                        strategyDTO = Base.STRATEGY_MAPPER.mapToStrategyDTO(strategyRepository.save(newStrategy));
                        logger.debug("Strategy created in database: {}", newStrategy);
                    } else {
                        // =============================================================================================
                        // If the strategy is in database.
                        strategyInDatabase.get().setName(strategyName);
                        strategyDTO = Base.STRATEGY_MAPPER.mapToStrategyDTO(strategyRepository.save(strategyInDatabase.get()));
                        logger.debug("Strategy updated in database: {}", strategyInDatabase.get());
                    }
                    strategyDTO.initializeLastPositionIdUsed(positionRepository.getLastPositionIdUsedByStrategy(strategyDTO.getUid()));

                    // Setting up configuration, dependencies and accounts in strategy.
                    strategy.initializeAccounts(user.getAccounts());
                    strategy.setConfiguration(getCassandreStrategyConfiguration(strategyDTO));
                    strategy.setDependencies(getCassandreStrategyDependencies());

                    // Calling user defined initialize() method.
                    strategy.initialize();

                    // Displaying information about strategy.
                    logger.info("- Strategy '{}/{}' (requires {})",
                            strategyId,
                            strategyName,
                            strategy.getRequestedCurrencyPairs().stream()
                                    .map(CurrencyPairDTO::toString)
                                    .collect(Collectors.joining(", ")));

                    // Connecting flux to strategy.
                    connectableAccountFlux.subscribe(strategy::accountsUpdates, throwable -> logger.error("AccountsUpdates failing: {}", throwable.getMessage(), throwable));
                    connectablePositionFlux.subscribe(strategy::positionsUpdates, throwable -> logger.error("PositionsUpdates failing: {}", throwable.getMessage(), throwable));
                    connectableOrderFlux.subscribe(strategy::ordersUpdates, throwable -> logger.error("OrdersUpdates failing: {}", throwable.getMessage(), throwable));
                    connectableTradeFlux.subscribe(strategy::tradesUpdates, throwable -> logger.error("TradesUpdates failing: {}", throwable.getMessage(), throwable));
                    connectableTickerFlux.subscribe(strategy::tickersUpdates, throwable -> logger.error("TickersUpdates failing: {}", throwable.getMessage(), throwable));
                });

        // =============================================================================================================
        // Starting flux.
        connectableAccountFlux.connect();
        connectablePositionFlux.connect();
        connectableOrderFlux.connect();
        connectableTradeFlux.connect();
        connectableTickerFlux.connect();
    }

    /**
     * Check and display Cassandre configuration.
     *
     * @param strategies strategies
     * @return user information
     */
    private UserDTO checkConfiguration(final Map<String, Object> strategies) {
        // Prints all the supported currency pairs.
        logger.info("Supported currency pairs by the exchange: {}",
                exchangeService.getAvailableCurrencyPairs()
                        .stream()
                        .map(CurrencyPairDTO::toString)
                        .collect(Collectors.joining(", ")));

        // Retrieve accounts information.
        final Optional<UserDTO> user = userService.getUser();
        if (user.isEmpty()) {
            // Unable to retrieve user information.
            throw new ConfigurationException("Impossible to retrieve your user information",
                    "Impossible to retrieve your user information - Check logs");
        } else {
            if (user.get().getAccounts().isEmpty()) {
                // We were able to retrieve the user from the exchange but no account was found.
                throw new ConfigurationException("User information retrieved but no associated accounts found",
                        "Check the permissions you set on the API you created");
            } else {
                logger.info("Accounts available on the exchange:");
                user.get()
                        .getAccounts()
                        .values()
                        .forEach(account -> {
                            logger.info("- Account id / name: {} / {}",
                                    account.getAccountId(),
                                    account.getName());
                            account.getBalances()
                                    .stream()
                                    .filter(balance -> balance.getAvailable().compareTo(ZERO) != 0)
                                    .forEach(balance -> logger.info(" - {} {}", balance.getAvailable(), balance.getCurrency()));
                        });
            }
        }

        // Check that there is at least one strategy.
        if (strategies.isEmpty()) {
            throw new ConfigurationException("No strategy found", "You must have, at least, one class with @CassandreStrategy annotation");
        }

        // Check that all strategies extends CassandreStrategyInterface.
        Set<String> strategiesWithoutExtends = strategies.values()
                .stream()
                .filter(strategy -> !(strategy instanceof CassandreStrategyInterface))
                .map(strategy -> strategy.getClass().getSimpleName())
                .collect(Collectors.toSet());
        if (!strategiesWithoutExtends.isEmpty()) {
            final String list = String.join(",", strategiesWithoutExtends);
            throw new ConfigurationException(list + " doesn't extend BasicCassandreStrategy or BasicTa4jCassandreStrategy",
                    list + " must extend BasicCassandreStrategy or BasicTa4jCassandreStrategy");
        }

        // Check that all strategies specifies an existing trade account.
        final Set<AccountDTO> accountsAvailableOnExchange = new HashSet<>(user.get().getAccounts().values());
        Set<String> strategiesWithoutTradeAccount = strategies.values()
                .stream()
                .filter(strategy -> ((CassandreStrategyInterface) strategy).getTradeAccount(accountsAvailableOnExchange).isEmpty())
                .map(strategy -> strategy.getClass().toString())
                .collect(Collectors.toSet());
        if (!strategiesWithoutTradeAccount.isEmpty()) {
            final String strategyList = String.join(",", strategiesWithoutTradeAccount);
            throw new ConfigurationException("Your strategies specify a trading account that doesn't exist",
                    "Check your getTradeAccount(Set<AccountDTO> accounts) method as it returns an empty result - Strategies in error: " + strategyList + "\r\n"
                            + "See https://trading-bot.cassandre.tech/ressources/how-tos/how-to-fix-common-problems.html#your-strategies-specifies-a-trading-account-that-doesn-t-exist");
        }

        // Check that there is no duplicated strategy ids.
        final List<String> strategyIds = strategies.values()
                .stream()
                .map(o -> o.getClass().getAnnotation(CassandreStrategy.class).strategyId())
                .collect(Collectors.toList());
        final Set<String> duplicatedStrategyIds = strategies.values()
                .stream()
                .map(o -> o.getClass().getAnnotation(CassandreStrategy.class).strategyId())
                .filter(strategyId -> Collections.frequency(strategyIds, strategyId) > 1)
                .collect(Collectors.toSet());
        if (!duplicatedStrategyIds.isEmpty()) {
            throw new ConfigurationException("You have duplicated strategy ids",
                    "You have duplicated strategy ids: " + String.join(", ", duplicatedStrategyIds));
        }

        // Check that the currency pairs required by the strategies are available on the exchange.
        //涉及到未initialize 获取空方法
        /*
        final Set<CurrencyPairDTO> availableCurrencyPairs = exchangeService.getAvailableCurrencyPairs();
        final Set<String> notAvailableCurrencyPairs = applicationContext
                .getBeansWithAnnotation(CassandreStrategy.class)
                .values()
                .stream()
                .map(o -> (CassandreStrategyInterface) o)
                .map(CassandreStrategyInterface::getRequestedCurrencyPairs)
                .flatMap(Set::stream)
                .filter(currencyPairDTO -> !availableCurrencyPairs.contains(currencyPairDTO))
                .map(CurrencyPairDTO::toString)
                .collect(Collectors.toSet());
        if (!notAvailableCurrencyPairs.isEmpty()) {
            logger.warn("Your exchange doesn't support the following currency pairs you requested: {}", String.join(", ", notAvailableCurrencyPairs));
        }
        */
        return user.get();
    }

    /**
     * Returns cassandre strategy configuration.
     *
     * @param strategyDTO strategy
     * @return cassandre strategy configuration
     */
    private CassandreStrategyConfiguration getCassandreStrategyConfiguration(final StrategyDTO strategyDTO) {
        return CassandreStrategyConfiguration.builder()
                .strategyDTO(strategyDTO)
                .dryMode(exchangeParameters.getModes().getDry())
                .leverage(strategyDTO.getDomain() == StrategyDomainDTO.SPOT ? "1" : Optional.ofNullable(exchangeParameters.getModes().getLeverage()).orElse("1"))
                .parameters(exchangeParameters.getStrategyParameters().get(strategyDTO.getName()))
                .expireSec(Optional.ofNullable(exchangeParameters.getRates().getExpireValueInSec()).orElse(10l))
                .build();
    }

    /**
     * Returns cassandre strategy dependencies.
     *
     * @return cassandre strategy dependencies
     */
    private CassandreStrategyDependencies getCassandreStrategyDependencies() {
        return CassandreStrategyDependencies.builder()
                // Flux.
                .positionFlux(positionFlux)
                // Repositories.
                .orderRepository(orderRepository)
                .tradeRepository(tradeRepository)
                .positionRepository(positionRepository)
                .importedTickersRepository(importedTickersRepository)
                .signalRepository(signalRepository)
                // Services.
                .exchangeService(exchangeService)
                .tradeService(tradeService)
                .positionService(positionService)
                .marketService(marketService)
                .messageService(messageService)
                .build();
    }

    /**
     * Load imported tickers into database.
     */
    private void loadImportedTickers() {
        // Deleting everything before import.
        importedTickersRepository.deleteAllInBatch();

        // Getting the list of files to import and insert them in database.
        logger.info("Importing tickers...");
        AtomicLong counter = new AtomicLong(0);
        getFilesToLoad()
                .parallelStream()
                .filter(resource -> resource.getFilename() != null)
                .peek(resource -> logger.info("Importing file {}", resource.getFilename()))
                .forEach(resource -> {
                    try {
                        // Insert the tickers in database.
                        CsvToBean<ImportedTicker> csvToBean = new CsvToBeanBuilder<ImportedTicker>(Files.newBufferedReader(resource.getFile().toPath()))
                                .withType(ImportedTicker.class)
                                .withIgnoreLeadingWhiteSpace(true)
                                .build();

                        csvToBean.parse().forEach(importedTicker -> {
                            logger.debug("Importing ticker {}", importedTicker);
                            importedTicker.setUid(counter.incrementAndGet());
                            importedTickersRepository.save(importedTicker);
                        });
                        /*
                        new CsvToBeanBuilder<ImportedTicker>(Files.newBufferedReader(resource.getFile().toPath()))
                                .withType(ImportedTicker.class)
                                .withIgnoreLeadingWhiteSpace(true)
                                .build()
                                .parse()
                                .forEach(importedTicker -> {
                                    logger.debug("Importing ticker {}", importedTicker);
                                    importedTicker.setUid(counter.incrementAndGet());
                                    importedTickersRepository.save(importedTicker);
                                });
                         */
                    } catch (IOException e) {
                        logger.error("Impossible to load imported tickers: {}", e.getMessage());
                    }
                });
        logger.info("{} tickers imported", importedTickersRepository.count());
    }

    /**
     * Returns the list of files to import.
     *
     * @return files to import.
     */
    public List<Resource> getFilesToLoad() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            final Resource[] resources = resolver.getResources("classpath*:tickers-to-import*csv");
            return Arrays.asList(resources);
        } catch (IOException e) {
            logger.error("Impossible to load imported tickers: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

}
