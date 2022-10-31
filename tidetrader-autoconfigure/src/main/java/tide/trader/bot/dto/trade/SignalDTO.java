package tide.trader.bot.dto.trade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import tide.trader.bot.dto.strategy.StrategyDTO;
import tide.trader.bot.dto.util.CurrencyAmountDTO;
import tide.trader.bot.dto.util.CurrencyPairDTO;
import tide.trader.bot.util.java.EqualsBuilder;
import tide.trader.bot.util.test.ExcludeFromCoverageGeneratedReport;

import java.math.BigDecimal;
import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

/**
 * DTO representing a trade.
 * A trade is the action of buying and selling goods and services.
 * <p>
 * This is how it works:
 * - Received ticker - It means 1 Ether can be bought with 0.034797 Bitcoin
 * currencyPair=ETH/BTC
 * last=0.034797 (Last trade field is the price set during the last trade).
 * <p>
 * - Account before buying
 * BTC: 0.99963006
 * ETH: 10
 * <p>
 * - Buying 0.004 Bitcoin (should cost 0.05748 ether).
 * TradeDTO{currencyPair=ETH/BTC, originalAmount=0.004, price=0.034797}
 * <p>
 * - Account after buying
 * BTC: 0.99949078
 * ETH: 10.004
 * It cost me 0.00013928 BTC (0.99949078 - 0.99963006).
 * price * amount = 0.034797 * 0.004
 */
@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
@SuppressWarnings("checkstyle:VisibilityModifier")
public class SignalDTO {

    /** Signal id. */
    Long uid;

    /** The strategy that created the order. */
    StrategyDTO strategy;

    /** Currency pair. */
    CurrencyPairDTO currencyPair;

    /** Position type (Long or Short). */
    SideDTO side;

    /** Trade type i.e. bid (buy) or ask (sell). */
    OrderTypeDTO type;

    /** Weighted Average price of the fills in the order. */
    CurrencyAmountDTO price;

    /** The status. */
    SignalStatusDTO status;

    /**
     * Returns price value.
     *
     * @return price value
     */
    public BigDecimal getPriceValue() {
        return Optional.ofNullable(price).map(CurrencyAmountDTO::getValue).orElse(null);
    }

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
        final SignalDTO that = (SignalDTO) o;
        return new EqualsBuilder()
                .append(this.uid, that.uid)
                .append(this.currencyPair, that.currencyPair)
                .append(this.type, that.type)
                .append(this.side, that.side)
                .append(this.price, that.price)
                .append(this.status, that.status)
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

}
