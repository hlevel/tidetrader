package tide.trader.bot.domain;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.Hibernate;
import tide.trader.bot.dto.trade.OrderTypeDTO;
import tide.trader.bot.dto.trade.SignalStatusDTO;
import tide.trader.bot.dto.trade.SideDTO;
import tide.trader.bot.dto.util.CurrencyPairDTO;
import tide.trader.bot.util.base.domain.BaseDomain;
import tide.trader.bot.util.jpa.CurrencyAmount;
import tide.trader.bot.util.test.ExcludeFromCoverageGeneratedReport;

import javax.persistence.*;
import java.util.Objects;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.GenerationType.IDENTITY;

/**
 * tradingviewSignal (map "IMPORTED_TICKERS" table).
 * {"strategyName":"BNMVStrategyV1","price": {{strategy.order.price}},"orderAction":"{{strategy.order.action}}","orderId":"{{strategy.order.id}}","position":"{{strategy.market_position}}","orderNum":{{strategy.order.contracts}},"time":"{{time}}"}
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "SIGNALS")
public class Signal extends BaseDomain {

    /** TradingViewSignal ID. */
    @Id
    @Column(name = "UID")
    @GeneratedValue(strategy = IDENTITY)
    private Long uid;

    /** The position that created the order. */
    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "FK_STRATEGY_UID", updatable = false)
    private Strategy strategy;

    /** The currency-pair. */
    @CsvBindByName(column = "CURRENCY_PAIR")
    @Column(name = "CURRENCY_PAIR")
    private String currencyPair;

    /** Order type i.e. bid (buy) or ask (sell). */
    @Enumerated(STRING)
    @Column(name = "SIDE")
    private SideDTO side;

    /** Position type - Short or Long. */
    @Enumerated(STRING)
    @Column(name = "TYPE")
    private OrderTypeDTO type;

    /** The opening price is the first trade price that was recorded during the day’s trading. */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "PRICE_VALUE")),
            @AttributeOverride(name = "currency", column = @Column(name = "PRICE_CURRENCY"))
    })
    private CurrencyAmount price;

    /** status */
    @Enumerated(STRING)
    @Column(name = "STATUS")
    private SignalStatusDTO status;

    /**
     * Returns currency pair DTO.
     *
     * @return currency pair DTO
     */
    public CurrencyPairDTO getCurrencyPairDTO() {
        if (currencyPair != null) {
            return new CurrencyPairDTO(currencyPair);
        } else {
            return null;
        }
    }

    @Override
    @ExcludeFromCoverageGeneratedReport
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        Signal that = (Signal) o;
        return Objects.equals(uid, that.uid);
    }

    @Override
    @ExcludeFromCoverageGeneratedReport
    public final int hashCode() {
        return new HashCodeBuilder()
                .append(uid)
                .toHashCode();
    }

}
