package tide.trader.bot.dto.position;

import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import tide.trader.bot.dto.market.TickerDTO;
import tide.trader.bot.dto.strategy.StrategyDTO;
import tide.trader.bot.dto.trade.OrderDTO;
import tide.trader.bot.dto.trade.OrderTypeDTO;
import tide.trader.bot.dto.trade.TradeDTO;
import tide.trader.bot.dto.util.CurrencyAmountDTO;
import tide.trader.bot.dto.util.CurrencyPairDTO;
import tide.trader.bot.dto.util.GainDTO;
import tide.trader.bot.strategy.internal.CassandreStrategy;
import tide.trader.bot.util.exception.PositionException;
import tide.trader.bot.util.java.EqualsBuilder;
import tide.trader.bot.util.math.MathConstants;
import tide.trader.bot.util.test.ExcludeFromCoverageGeneratedReport;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.FLOOR;
import static java.math.RoundingMode.HALF_UP;
import static lombok.AccessLevel.PRIVATE;
import static tide.trader.bot.dto.position.PositionStatusDTO.*;

/**
 * DTO representing a position.
 * A position is the amount of a security, commodity or currency which is owned by an individual, dealer, institution, or other fiscal entity.
 */
@Getter
@Builder
@ToString
@AllArgsConstructor(access = PRIVATE)
@SuppressWarnings({"checkstyle:VisibilityModifier", "DuplicatedCode"})
public class MultPositionDTO {

    private final CassandreStrategy strategy;

    private final PositionRulesDTO rules;

    private final CurrencyPairDTO currencyPair;

    /** Price of the lowest gain reached by this position. */
    private CurrencyAmountDTO lowestGainPrice;

    /** Price of the highest gain reached by this position. */
    private CurrencyAmountDTO highestGainPrice;

    /** Price of the latest gain price for this position. */
    private CurrencyAmountDTO latestGainPrice;

    /**
     * Constructor.
     *
     * @param newStrategy     strategy
     * @param newCurrencyPair currency pair
     * @param newRules        position rules
     */
    public MultPositionDTO(final CassandreStrategy newStrategy,
                           final PositionRulesDTO newRules,
                           final CurrencyPairDTO newCurrencyPair) {
        this.strategy = newStrategy;
        this.rules = newRules;
        this.currencyPair = newCurrencyPair;
    }

    /**
     * 获取活跃仓位
     * @return
     */
    private List<PositionDTO> getKeepPosition(){
        return strategy.getPositions().values().stream().filter(p -> p.getCurrencyPair() == currencyPair).filter(p -> p.getStatus() == OPENED).collect(Collectors.toList());
    }

    /**
     * 是否多方
     * @return
     */
    public boolean isLong() {
        return (getKeepPosition().isEmpty() || getKeepPosition().get(0).getType() == PositionTypeDTO.LONG) ? true : false;
    }

    /**
     * 是否空方
     * @return
     */
    public boolean isShort() {
        return (!getKeepPosition().isEmpty() || getKeepPosition().get(0).getType() == PositionTypeDTO.SHORT) ? true : false;
    }

    /**
     * Calculate the gain from a price.
     * @param positions
     * @param price
     * @return
     */
    public Optional<GainDTO> calculateGainFromPrice(final List<PositionDTO> positions, final BigDecimal price) {
        if (price != null && ZERO.compareTo(price) != 0 && !positions.isEmpty()) {

            if(isLong()) {
                final BigDecimal totalBought = positions.stream().map(p -> p.getOpeningOrder()).map(o -> {
                    o.getAveragePriceValue().multiply(strategy.takeFee(o.getAmountValue()) );
                    final BigDecimal valueIBought;
                    if(o.isFulfilled()) {
                        // If we received all the trades, I can calculate exactly the amount I bought.
                        valueIBought = o.getTrades()
                                .stream()
                                .map(t -> t.getAmountValue().multiply(t.getPriceValue()))
                                .reduce(ZERO, BigDecimal::add);
                    } else {
                        // If we did not receive all trades, I use order information.
                        valueIBought = o.getAmountValue().multiply(o.getAveragePriceValue());
                    }
                    return valueIBought;
                }).reduce(ZERO, BigDecimal::add);

                final BigDecimal totalSold = positions.stream().map(p -> p.getAmount().getValue().multiply(price)).reduce(ZERO, BigDecimal::add);

                final BigDecimal gain = totalSold.subtract(totalBought).setScale(MathConstants.BIGINTEGER_SCALE, FLOOR).stripTrailingZeros();
                // Percentage.
                final BigDecimal gainPercentage = (gain.divide(totalBought, MathConstants.ONE_HUNDRED_SCALE_DECIMAL, FLOOR))
                        .multiply(MathConstants.ONE_HUNDRED_BIG_DECIMAL);

                return Optional.of(GainDTO.builder()
                        .percentage(gainPercentage.floatValue())
                        .amount(CurrencyAmountDTO.builder()
                                .value(gain)
                                .currency(currencyPair.getQuoteCurrency())
                                .build())
                        .build());
            }

        }
        return Optional.empty();
    }

    /**
     * Returns amount locked by this position.
     *
     * @return amount
     */
    public CurrencyAmountDTO getAmountToLock() {
        BigDecimal keepAmount = this.getKeepPosition().stream().map(p -> p.getAmountToLock().getValue()).reduce(ZERO, BigDecimal::add);
        return new CurrencyAmountDTO(keepAmount, currencyPair.getBaseCurrency());
    }

    /**
     * Returns true if the position should be closed.
     *
     * @return true if the rules says the position should be closed.
     */
    public boolean shouldBeClosed() {
        // Returns true if one of the rule is triggered.
        /*
        final Optional<GainDTO> latestCalculatedGain = getLatestCalculatedGain();
        return latestCalculatedGain
                .filter(gainDTO -> rules.isStopGainPercentageSet() && gainDTO.getPercentage() >= rules.getStopGainPercentage()
                        || rules.isStopLossPercentageSet() && gainDTO.getPercentage() <= -rules.getStopLossPercentage())
                .isPresent();
         */
        final Optional<GainDTO> latestCalculatedGain = getLatestCalculatedGain();
        if(!latestCalculatedGain.isPresent()) {
            return false;
        }
        GainDTO gainDTO = latestCalculatedGain.get();

        //Conditions for calculating profit callback ratio
        if(rules.isStopGainPercentageSet()) {
            final Optional<GainDTO> highestCalculatedGain = getHighestCalculatedGain();
            if(highestCalculatedGain.isPresent() && gainDTO.getPercentage() >= rules.getStopGainPercentage()) {
                return highestCalculatedGain.get().getPercentage() - gainDTO.getPercentage() >= rules.getStopGainPercentage();
            }
        }

        //Conditions for calculating loss proportion
        if(rules.isStopLossPercentageSet()) {
            return latestCalculatedGain.get().getPercentage() <= -rules.getStopLossPercentage();
        }

        return false;
    }

    /**
     * Close position with order.
     *
     * @param newCloseOrder the order closing the position
     */
    public final void closePositionWithOrder(final OrderDTO newCloseOrder) {
        // This method should only be called when the position is in the OPENED status.
        /*
        if (getStatus() != OPENED) {
            throw new PositionException("Impossible to close position " + uid + " because of its status " + getStatus());
        }
        closingOrder = newCloseOrder;
        */
    }

    /**
     * Getter lowestCalculatedGain.
     *
     * @return lowestCalculatedGain
     */
    public final Optional<GainDTO> getLowestCalculatedGain() {
        if (lowestGainPrice != null) {
            return calculateGainFromPrice(this.getKeepPosition(), lowestGainPrice.getValue());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Getter highestCalculatedGain.
     *
     * @return highestCalculatedGain
     */
    public final Optional<GainDTO> getHighestCalculatedGain() {
        if (highestGainPrice != null) {
            return calculateGainFromPrice(this.getKeepPosition(), highestGainPrice.getValue());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Getter latestCalculatedGain.
     *
     * @return latestCalculatedGain
     */
    public final Optional<GainDTO> getLatestCalculatedGain() {
        if (latestGainPrice != null) {
            return calculateGainFromPrice(this.getKeepPosition(), latestGainPrice.getValue());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Returns the gain of the position.
     * Of course the position should be closed to have a gain.
     *
     * @return gain
     */
    public GainDTO getGain() {
        Stream<PositionDTO> positionStream = strategy.getPositions().values().stream().filter(p -> p.getStatus() == CLOSED);
        if(positionStream.findFirst().isPresent()) {

            final List<CurrencyAmountDTO> totalOpeningOrderFees = new ArrayList<>();
            final List<CurrencyAmountDTO> totalClosingOrderFees = new ArrayList<>();

            final BigDecimal totalBought = positionStream.map(p -> {
                if(p.getType() == PositionTypeDTO.LONG) {
                    BigDecimal bought = p.getOpeningOrder().getTrades()
                            .stream()
                            .map(t -> t.getAmountValue().multiply(t.getPriceValue()))
                            .reduce(ZERO, BigDecimal::add);

                    // Opening & closing order fees.
                    final List<CurrencyAmountDTO> openingOrderFees = p.getOpeningOrder().getTrades()
                            .stream()
                            .filter(tradeDTO -> tradeDTO.getFee() != null)
                            .map(tradeDTO -> new CurrencyAmountDTO(tradeDTO.getFee().getValue(), tradeDTO.getFee().getCurrency()))
                            .collect(Collectors.toList());
                    totalOpeningOrderFees.addAll(openingOrderFees);
                    return bought;
                }
                return ZERO;
            }).reduce(ZERO, BigDecimal::add);

            final BigDecimal totalSold = positionStream.map(p -> {
                if(p.getType() == PositionTypeDTO.LONG) {
                    BigDecimal sold = p.getClosingOrder().getTrades()
                            .stream()
                            .map(t -> t.getAmountValue().multiply(t.getPriceValue()))
                            .reduce(ZERO, BigDecimal::add);

                    final List<CurrencyAmountDTO> closingOrderFees = p.getClosingOrder().getTrades()
                            .stream()
                            .filter(tradeDTO -> tradeDTO.getFee() != null)
                            .map(tradeDTO -> new CurrencyAmountDTO(tradeDTO.getFee().getValue(), tradeDTO.getFee().getCurrency()))
                            .collect(Collectors.toList());
                    totalClosingOrderFees.addAll(closingOrderFees);
                    return sold;
                }
                return ZERO;
            }).reduce(ZERO, BigDecimal::add);

            // Calculate gain.
            BigDecimal gainAmount = totalSold.subtract(totalBought);
            BigDecimal gainPercentage = ((totalSold.subtract(totalBought)).divide(totalBought, MathConstants.ONE_HUNDRED_SCALE_DECIMAL, HALF_UP)).multiply(MathConstants.ONE_HUNDRED_BIG_DECIMAL);


            // Return position gain.
            return GainDTO.builder()
                    .percentage(gainPercentage.setScale(2, HALF_UP).doubleValue())
                    .amount(CurrencyAmountDTO.builder()
                            .value(gainAmount)
                            .currency(currencyPair.getQuoteCurrency())
                            .build())
                    .openingOrderFees(totalOpeningOrderFees)
                    .closingOrderFees(totalClosingOrderFees)
                    .build();
        }
        // If the position is not closed: Zero gain.
        return GainDTO.ZERO;
    }

    /**
     * Get position description.
     *
     * @return description
     */
    @SuppressWarnings("unused")
    public final String getDescription() {
        try {/*
            String value = StringUtils.capitalize(type.toString().toLowerCase(Locale.ROOT)) + " position n°" + positionId + " of " + amount;
            // Rules.
            value += " (rules: ";
            if (!rules.isStopGainPercentageSet() && !rules.isStopLossPercentageSet()) {
                value += "no rules";
            }
            if (rules.isStopGainPercentageSet() && !rules.isStopLossPercentageSet()) {
                value += rules.getStopGainPercentage() + " % gain";
            }
            if (rules.isStopLossPercentageSet() && !rules.isStopGainPercentageSet()) {
                value += rules.getStopLossPercentage() + " % loss";
            }
            if (rules.isStopGainPercentageSet() && rules.isStopLossPercentageSet()) {
                value += rules.getStopGainPercentage() + " % gain / ";
                value += rules.getStopLossPercentage() + " % loss";
            }
            value += ")";
            switch (getStatus()) {
                case OPENING:
                    value += " - Opening - Waiting for the trades of order " + openingOrder.getOrderId();
                    break;
                case OPENED:
                    value += " - Opened";
                    final Optional<GainDTO> lastGain = getLatestCalculatedGain();
                    if (lastGain.isPresent() && getLatestCalculatedGain().isPresent()) {
                        value += " - Last gain calculated " + getFormattedValue(getLatestCalculatedGain().get().getPercentage()) + " %";
                    }
                    break;
                case OPENING_FAILURE:
                    value = "Position " + this.getUid() + " - Opening failure";
                    break;
                case CLOSING:
                    value += " - Closing - Waiting for the trades of order " + closingOrder.getOrderId();
                    break;
                case CLOSING_FAILURE:
                    value = "Position " + this.getUid() + " - Closing failure";
                    break;
                case CLOSED:
                    final GainDTO gain = getGain();
                    if (gain != null) {
                        value += " - Closed - " + gain;
                    } else {
                        value += " - Closed - Error during gain calculation";
                    }
                    break;
                default:
                    value = "Incorrect state for position " + this.getUid();
                    break;
            }
            return value;
            */
            return null;
        } catch (Exception e) {
            return "Position " + " (error in getDescription() method)";
        }
    }

    /**
     * Returns formatted value.
     *
     * @param value value
     * @return formatted value
     */
    private String getFormattedValue(final double value) {
        return new DecimalFormat("#0.##").format(value);
    }


    @Override
    @ExcludeFromCoverageGeneratedReport
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final MultPositionDTO that = (MultPositionDTO) o;
        return new EqualsBuilder()
                .append(this.currencyPair, that.currencyPair)
                .append(this.rules, that.rules)
                .append(this.lowestGainPrice, that.lowestGainPrice)
                .append(this.highestGainPrice, that.highestGainPrice)
                .append(this.latestGainPrice, that.latestGainPrice)
                .isEquals();
    }


}
