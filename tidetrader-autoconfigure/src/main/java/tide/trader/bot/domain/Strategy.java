package tide.trader.bot.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.Hibernate;
import tide.trader.bot.dto.strategy.StrategyDomainDTO;
import tide.trader.bot.dto.strategy.StrategyTypeDTO;
import tide.trader.bot.util.base.domain.BaseDomain;
import tide.trader.bot.util.java.EqualsBuilder;
import tide.trader.bot.util.test.ExcludeFromCoverageGeneratedReport;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.IDENTITY;

/**
 * Strategy (map "STRATEGIES" table).
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "STRATEGIES")
public class Strategy extends BaseDomain {

    /** Technical ID. */
    @Id
    @Column(name = "UID")
    @GeneratedValue(strategy = IDENTITY)
    private Long uid;

    /** An identifier that uniquely identifies the strategy - Comes from the Java annotation. */
    @Column(name = "STRATEGY_ID")
    private String strategyId;

    /** Strategy type - Basic or Ta4j. */
    @Enumerated(STRING)
    @Column(name = "TYPE")
    private StrategyTypeDTO type;

    /** Strategy name - Comes from the Java annotation. */
    @Column(name = "NAME")
    private String name;

    /** Class name - Comes from the Java class simpleName. */
    @Column(name = "CLASS_NAME")
    private String className;

    /** Strategy domain - spot or perpetual. */
    @Enumerated(STRING)
    @Column(name = "DOMAIN")
    private StrategyDomainDTO domain;

    @Override
    @ExcludeFromCoverageGeneratedReport
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        final Strategy that = (Strategy) o;
        return new EqualsBuilder()
                .append(this.uid, that.uid)
                .append(this.strategyId, that.strategyId)
                .append(this.type, that.type)
                .append(this.name, that.name)
                .append(this.className, that.className)
                .isEquals();
    }

    @Override
    @ExcludeFromCoverageGeneratedReport
    public final int hashCode() {
        return new HashCodeBuilder()
                .append(uid)
                .toHashCode();
    }

}
