package tide.trader.bot.dto.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import tide.trader.bot.dto.strategy.StrategyDTO;
import tide.trader.bot.util.java.EqualsBuilder;
import tide.trader.bot.util.test.ExcludeFromCoverageGeneratedReport;

import static lombok.AccessLevel.PRIVATE;

/**
 * Message Dto.
 */
@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MessageDTO {

    /** Message id. */
    Long uid;

    /** strategy. */
    StrategyDTO strategy;

    /** An identifier that uniquely identifies the position. */
    long positionId;

    /** Message title. */
    String title;

    /** Message body. */
    String body;

    /** Message status. */
    MessageStatusDTO status;

    @Override
    @ExcludeFromCoverageGeneratedReport
    @SuppressWarnings("checkstyle:DesignForExtension")
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final MessageDTO that = (MessageDTO) o;
        return new EqualsBuilder()
                .append(this.title, that.title)
                .append(this.body, that.body)
                //.append(this.status, that.status)
                .isEquals();
    }

    @Override
    @ExcludeFromCoverageGeneratedReport
    @SuppressWarnings("checkstyle:DesignForExtension")
    public int hashCode() {
        return new HashCodeBuilder()
                .append(uid)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "MessageDTO{"
                + " uid=" + uid
                + ", positionId=" + positionId
                + ", title=" + title
                + ", body=" + body
                + '}';
    }
}
