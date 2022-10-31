package tide.trader.bot.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.Hibernate;
import tide.trader.bot.dto.util.MessageStatusDTO;
import tide.trader.bot.util.base.domain.BaseDomain;
import tide.trader.bot.util.java.EqualsBuilder;
import tide.trader.bot.util.test.ExcludeFromCoverageGeneratedReport;

import javax.persistence.*;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.GenerationType.IDENTITY;

/**
 * Position (map "MESSAGES" table).
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "MESSAGES")
public class Message extends BaseDomain {

    /** Technical ID. */
    @Id
    @Column(name = "UID")
    @GeneratedValue(strategy = IDENTITY)
    private Long uid;

    /** The strategy that created the position. */
    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "FK_STRATEGY_UID", updatable = false)
    private Strategy strategy;

    /** The currency-pair. */
    @Column(name = "TITLE")
    private String title;

    /** The currency-pair. */
    @Column(name = "BODY")
    private String body;

    /** Order status. */
    @Enumerated(STRING)
    @Column(name = "STATUS")
    private MessageStatusDTO status;

    @Override
    @ExcludeFromCoverageGeneratedReport
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        final Message that = (Message) o;
        return new EqualsBuilder()
                .append(this.uid, that.uid)
                .append(this.title, that.title)
                .append(this.body, that.body)
                .append(this.status, that.status)
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
