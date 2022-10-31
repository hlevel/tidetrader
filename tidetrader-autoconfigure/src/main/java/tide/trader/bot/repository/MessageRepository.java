package tide.trader.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import tide.trader.bot.domain.Message;
import tide.trader.bot.domain.Trade;

import java.util.List;
import java.util.Optional;

/**
 * {@link Message} repository.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long>, JpaSpecificationExecutor<Message> {

    /**
     * Find a trade by its trade id.
     *
     * @param tradeId trade id
     * @return trade
    Optional<Trade> findByTradeId(String tradeId);
     */

    /**
     * Retrieve all trades (sorted by timestamp).
     *
     * @return trades
    List<Trade> findByOrderByTimestampAsc();
     */

}
