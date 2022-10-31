package tide.trader.bot.strategy.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import tide.trader.bot.dto.strategy.StrategyDTO;
import tide.trader.bot.strategy.BasicCassandreStrategy;
import tide.trader.bot.strategy.BasicTa4jCassandreStrategy;

import static lombok.AccessLevel.PRIVATE;

/**
 * CassandreStrategyConfiguration contains the configuration of the strategy.
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
public class CassandreStrategyConfiguration<T> {

    /** Strategy. */
    StrategyDTO strategyDTO;

    /** Dry mode. */
    boolean dryMode;

    /** Leverage */
    String leverage;

    /** parameters **/
    String parameters;

    /** Expired millisecond **/
    long expireSec;

    /**
     * Returns strategy uid in database.
     *
     * @return strategy uid
     */
    long getStrategyUid() {
        return strategyDTO.getUid();
    }

    /**
     * Get Strategy parameters
     * @param <T>
     * @return

    public <T> Optional<T> getParameters() {
        if(StringUtils.isBlank(this.parameters)) return Optional.empty();
        return Optional.of(JacksonParser.jsonToBean(parameters, new TypeReference<>(){}));
    }*/

    /**
     * Get Strategy parameters
     * @param <T>
     * @return

    public <T> Optional<List<T>> getParametersList(Class<?> clazz) {
        if(StringUtils.isBlank(this.parameters)) return Optional.empty();
        return Optional.of(JacksonParser.jsonToBeanList(parameters, clazz));
    }*/

    /**
     * Whether the policy is configured with short configuration
     * @return false or true
     */
    public boolean isShort() {
        return this.strategyDTO.isShort();
    }

}
