package tide.trader.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tide.trader.bot.domain.Signal;
import tide.trader.bot.dto.trade.SignalStatusDTO;
import tide.trader.bot.dto.trade.SideDTO;

import java.util.Optional;

/**
 * {@link Signal} repository.
 */
@Repository
public interface SignalRepository extends JpaRepository<Signal, Long>, JpaSpecificationExecutor<Signal> {

    /**
     * Find Signal list by conditon
     * @param strategyId
     * @param currencyPair
     * @param status
     * @return
     */
    Optional<Signal> findFirstByStrategyStrategyIdAndCurrencyPairAndStatusOrderByCreatedOnAsc(String strategyId, String currencyPair, SignalStatusDTO status);

    /**
     * Find Signal list by conditon
     * @param strategyId
     * @param currencyPair
     * @param status
     * @return
     */
    Optional<Signal> findFirstByStrategyStrategyIdAndCurrencyPairAndStatusAndTypeOrderByCreatedOnAsc(String strategyId, String currencyPair, SignalStatusDTO status, SideDTO type);

    /**
     * Update status.
     *
     * @param uid   signal uid
     * @param status active to allow expired.
     */
    @Transactional
    @Modifying
    @Query("update Signal s set s.status = :status where s.uid = :uid")
    void updateStatus(@Param("uid") Long uid, @Param("status") SignalStatusDTO status);

}
