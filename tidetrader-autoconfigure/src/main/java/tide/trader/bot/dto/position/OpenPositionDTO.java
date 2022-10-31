package tide.trader.bot.dto.position;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.knowm.xchange.dto.account.OpenPosition;
import tide.trader.bot.dto.trade.OrderDTO;
import tide.trader.bot.dto.util.CurrencyAmountDTO;
import tide.trader.bot.dto.util.CurrencyPairDTO;
import tide.trader.bot.util.java.EqualsBuilder;
import tide.trader.bot.util.math.MathConstants;
import tide.trader.bot.util.test.ExcludeFromCoverageGeneratedReport;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

/**
 *  //强制平仓价（多仓）= (开仓价 * 杠杆) / {杠杆+1-(维持保证金率 * 杠杆)}
 *  //强制平仓价（空仓）= (开仓价 * 杠杆) / {杠杆-1+(维持保证金率 * 杠杆)}
 *  //在价格为10,000美金时，以10倍杠杆做多进场的话，该仓位的强制平仓价如下：
 *  //强制平仓价 = (10,000(开仓价) * 10(杠杆)) / {10(杠杆)+1-(0.005(维持保证金率) * 10(杠杆))}
 *  //代入公式计算的话，该仓位的强制平仓价为 $9,132.42
 */
@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class OpenPositionDTO {

    /** Currency pair. */
    CurrencyPairDTO currencyPair;

    /** type. */
    OpenPosition.Type type;

    /** Amount to be ordered / amount that was ordered. */
    BigDecimal amount;

    /** Weighted Average price of the fills in the order. */
    BigDecimal price;

    /** liquidationPrice **/
    BigDecimal liquidationPrice;

    /** margin **/
    BigDecimal margin;

    /**
     * Consolidated results
     * @param openPosition
     * @return amount
     */
    public OpenPositionResultDTO positionLiquidation(OpenPositionDTO openPosition) {
        synchronized (openPosition) {
            OpenPositionDTO newOpenPosition = null;
            BigDecimal quoteBalance = BigDecimal.ZERO;

            if(openPosition.getType() == this.type) {
                BigDecimal cost = this.getAmount().multiply(this.getPrice());
                BigDecimal thatCost = openPosition.getAmount().multiply(openPosition.getPrice());

                ////Calculate total amount
                BigDecimal newAmount = this.getAmount().add(openPosition.getAmount());
                //Calculate average price
                BigDecimal newPrice = cost.add(thatCost).divide(newAmount, MathConstants.BIGINTEGER_SCALE, RoundingMode.DOWN);
                BigDecimal marginBalance = openPosition.getMargin().multiply(new BigDecimal("-1"));
                quoteBalance = marginBalance.add(BigDecimal.ZERO);

                newOpenPosition = OpenPositionDTO.builder()
                        .currencyPair(this.currencyPair)
                        .type(this.type)
                        .price(newPrice)
                        .amount(newAmount)
                        .margin(openPosition.getMargin().add(this.getMargin()))
                        .build();
                //Calculate occupation amount
            } else {
                //Quantity and cost involved in calculation
                BigDecimal balAmount = this.amount.subtract(openPosition.amount);
                if(balAmount.compareTo(BigDecimal.ZERO) == 0) {
                    //No position required for completion
                    BigDecimal offsetAmount = this.amount.subtract(balAmount);
                    BigDecimal offsetPrice = this.type == OpenPosition.Type.LONG ? this.price.subtract(openPosition.price).multiply(new BigDecimal("-1")) : this.price.subtract(openPosition.price);
                    BigDecimal marginBalance = this.margin;
                    BigDecimal gain = offsetPrice.multiply(offsetAmount);
                    quoteBalance = marginBalance.add(gain);

                    newOpenPosition = OpenPositionDTO.builder()
                            .currencyPair(this.currencyPair)
                            .type(null)
                            .price(BigDecimal.ZERO)
                            .amount(BigDecimal.ZERO)
                            .margin(BigDecimal.ZERO)
                            .build();
                } else if (balAmount.compareTo(BigDecimal.ZERO) > 0){
                    BigDecimal offsetMargin = this.margin.subtract(this.margin.divide(this.amount, MathConstants.BIGINTEGER_SCALE, RoundingMode.DOWN).multiply(openPosition.amount));
                    BigDecimal offsetAmount = this.amount.subtract(balAmount);
                    BigDecimal offsetPrice = (this.type == OpenPosition.Type.LONG) ? this.price.subtract(openPosition.price).multiply(new BigDecimal("-1")) : this.price.subtract(openPosition.price);

                    BigDecimal marginBalance = this.margin.subtract(offsetMargin);
                    BigDecimal gain = offsetPrice.multiply(offsetAmount);
                    quoteBalance = marginBalance.add(gain);

                    newOpenPosition = OpenPositionDTO.builder()
                            .currencyPair(this.currencyPair)
                            .type(this.type)
                            .price(this.price)
                            .amount(balAmount)
                            .margin(offsetMargin)
                            .build();
                } else if (balAmount.compareTo(BigDecimal.ZERO) < 0){
                    BigDecimal offsetMargin = openPosition.margin.subtract(openPosition.margin.divide(openPosition.amount, MathConstants.BIGINTEGER_SCALE, RoundingMode.DOWN).multiply(this.amount));
                    BigDecimal offsetAmount = openPosition.amount.add(balAmount);
                    BigDecimal offsetPrice = (this.type == OpenPosition.Type.SHORT) ? openPosition.price.subtract(this.price).multiply(new BigDecimal("-1")) : openPosition.price.subtract(this.price);

                    BigDecimal marginBalance = this.margin.subtract(offsetMargin);
                    BigDecimal gain = offsetPrice.multiply(offsetAmount);
                    quoteBalance = marginBalance.add(gain);

                    newOpenPosition = OpenPositionDTO.builder()
                            .currencyPair(this.currencyPair)
                            .type(openPosition.type)
                            .price(openPosition.price)
                            .amount(balAmount.abs())
                            .margin(offsetMargin)
                            .build();
                }
            }
            return OpenPositionResultDTO.builder()
                    .openPosition(newOpenPosition)
                    .quoteBalance(quoteBalance)
                    .build();
        }
    }


    public enum Type {
        LONG,
        SHORT
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
        final OpenPositionDTO that = (OpenPositionDTO) o;
        return new EqualsBuilder()
                .append(this.type, that.type)
                .append(this.currencyPair, that.currencyPair)
                .append(this.amount, that.amount)
                .append(this.price, that.price)
                .append(this.liquidationPrice, that.liquidationPrice)
                .isEquals();
    }

    @Override
    @ExcludeFromCoverageGeneratedReport
    @SuppressWarnings("checkstyle:DesignForExtension")
    public int hashCode() {
        return new HashCodeBuilder()
                .append(currencyPair)
                .append(amount)
                .toHashCode();
    }
}
