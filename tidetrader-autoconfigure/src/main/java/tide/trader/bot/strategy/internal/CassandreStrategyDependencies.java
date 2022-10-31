package tide.trader.bot.strategy.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import tide.trader.bot.batch.PositionFlux;
import tide.trader.bot.repository.*;
import tide.trader.bot.service.*;
import tide.trader.bot.strategy.BasicTa4jCassandreStrategy;
import tide.trader.bot.strategy.BasicCassandreStrategy;

import static lombok.AccessLevel.PRIVATE;

/**
 * CassandreStrategyDependencies contains all the dependencies required by a strategy and provided by the Cassandre framework.
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
@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
@SuppressWarnings("checkstyle:VisibilityModifier")
public class CassandreStrategyDependencies {

    // =================================================================================================================
    // Flux.

    /** Position flux. */
    PositionFlux positionFlux;

    // =================================================================================================================
    // Repositories.

    /** Order repository. */
    OrderRepository orderRepository;

    /** Trade repository. */
    TradeRepository tradeRepository;

    /** Position repository. */
    PositionRepository positionRepository;

    /** "Imported tickers" repository. */
    ImportedTickersRepository importedTickersRepository;

    /** Signal repository. */
    SignalRepository signalRepository;

    // =================================================================================================================
    // Services.

    /** Exchange service. */
    ExchangeService exchangeService;

    /** Trade service. */
    TradeService tradeService;

    /** Position service. */
    PositionService positionService;

    /** Market service. */
    MarketService marketService;

    /** Message service. */
    MessageService messageService;

}
