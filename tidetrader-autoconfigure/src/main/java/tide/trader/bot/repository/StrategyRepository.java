package tide.trader.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import tide.trader.bot.domain.Strategy;

import java.util.Optional;

/**
 * {@link Strategy} repository.
 */
@Repository
public interface StrategyRepository extends JpaRepository<Strategy, Long>, JpaSpecificationExecutor<Strategy> {

    /**
     * Find a strategy by its strategy id.
     *
     * @param strategyId strategy id
     * @return strategy
     */
    Optional<Strategy> findByStrategyId(String strategyId);

    /**
     * Find a strategy by its strategy name.
     *
     * @param name strategy name
     * @return strategy
     */
    Optional<Strategy> findByName(String name);
}
