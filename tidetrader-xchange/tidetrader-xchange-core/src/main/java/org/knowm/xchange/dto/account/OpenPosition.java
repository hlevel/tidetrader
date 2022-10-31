package org.knowm.xchange.dto.account;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.instrument.Instrument;

public class OpenPosition implements Serializable {
  /** The instrument */
  private final CurrencyPair currencyPair;
  /** Is this a long or a short position */
  private final Type type;
  /** The size of the position */
  private final BigDecimal amount;
  /** The average entry price for the position */
  @JsonIgnore private final BigDecimal price;
  /** The estimatedLiquidationPrice */
  @JsonIgnore private final BigDecimal liquidationPrice;
  /** The estimated margin */
  @JsonIgnore private final BigDecimal margin;

  public OpenPosition(
      @JsonProperty("currencyPair") CurrencyPair currencyPair,
      @JsonProperty("type") Type type,
      @JsonProperty("amount") BigDecimal amount,
      @JsonProperty("price") BigDecimal price,
      @JsonProperty("liquidationPrice") BigDecimal liquidationPrice,
      @JsonProperty("margin") BigDecimal margin) {
    this.currencyPair = currencyPair;
    this.type = type;
    this.amount = amount;
    this.price = price;
    this.liquidationPrice = liquidationPrice;
    this.margin = margin;
  }

  public CurrencyPair getCurrencyPair() {
    return currencyPair;
  }

  public Type getType() {
    return type;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public BigDecimal getLiquidationPrice() {
    return liquidationPrice;
  }
  public BigDecimal getMargin() {
    return margin;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final OpenPosition that = (OpenPosition) o;
    return Objects.equals(currencyPair, that.currencyPair)
        && type == that.type
        && Objects.equals(amount, that.amount)
        && Objects.equals(price, that.price)
        && Objects.equals(liquidationPrice, that.liquidationPrice);
  }

  @Override
  public int hashCode() {
    return Objects.hash(currencyPair, type, amount, price, liquidationPrice);
  }

  @Override
  public String toString() {
    return "OpenPosition{"
        + "currencyPair="
        + currencyPair
        + ", type="
        + type
        + ", amount="
        + amount
        + ", price="
        + price
        + ", liquidationPrice="
        + liquidationPrice
        + ", margin="
        + margin
        + '}';
  }

  public enum Type {
    LONG,
    SHORT
  }

  public static class Builder {
    private CurrencyPair currencyPair;
    private Type type;
    private BigDecimal amount;
    private BigDecimal price;
    private BigDecimal liquidationPrice;
    private BigDecimal margin;

    public static Builder from(OpenPosition openPosition) {
      return new Builder()
          .currencyPair(openPosition.getCurrencyPair())
          .type(openPosition.getType())
          .amount(openPosition.getAmount())
          .liquidationPrice(openPosition.getLiquidationPrice())
          .margin(openPosition.getMargin())
          .price(openPosition.getPrice());
    }

    public Builder currencyPair(final CurrencyPair currencyPair) {
      this.currencyPair = currencyPair;
      return this;
    }

    public Builder type(final Type type) {
      this.type = type;
      return this;
    }

    public Builder amount(final BigDecimal amount) {
      this.amount = amount;
      return this;
    }

    public Builder price(final BigDecimal price) {
      this.price = price;
      return this;
    }

    public Builder liquidationPrice(final BigDecimal liquidationPrice) {
      this.liquidationPrice = liquidationPrice;
      return this;
    }

    public Builder margin(final BigDecimal margin) {
      this.margin = margin;
      return this;
    }

    public OpenPosition build() {
      return new OpenPosition(currencyPair, type, amount, price, liquidationPrice, margin);
    }
  }
}
