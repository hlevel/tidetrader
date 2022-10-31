package org.knowm.xchange.binance.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Date;

public final class BinancePerpetualTrade {

  public final long id;
  public final long orderId;
  public final BigDecimal price;
  public final BigDecimal qty;
  public final BigDecimal quoteQty;
  public final BigDecimal commission;
  public final BigDecimal realizedPnl;
  public final String commissionAsset;
  public final String symbol;
  public final long time;
  public final boolean buyer;
  public final boolean maker;
  public final OrderSide side;
  public final String positionSide;

  public BinancePerpetualTrade(
      @JsonProperty("buyer") boolean buyer,
      @JsonProperty("commission") BigDecimal commission,
      @JsonProperty("commissionAsset") String commissionAsset,
      @JsonProperty("id") long id,
      @JsonProperty("maker") boolean maker,
      @JsonProperty("orderId") long orderId,
      @JsonProperty("price") BigDecimal price,
      @JsonProperty("qty") BigDecimal qty,
      @JsonProperty("quoteQty") BigDecimal quoteQty,
      @JsonProperty("realizedPnl") BigDecimal realizedPnl,
      @JsonProperty("side") OrderSide side,
      @JsonProperty("positionSide") String positionSide,
      @JsonProperty("symbol") String symbol,
      @JsonProperty("time") long time) {
    this.buyer = buyer;
    this.commission = commission;
    this.commissionAsset = commissionAsset;
    this.id = id;
    this.maker = maker;
    this.orderId = orderId;
    this.price = price;
    this.qty = qty;
    this.quoteQty = quoteQty;
    this.realizedPnl = realizedPnl;
    this.side = side;
    this.positionSide = positionSide;
    this.symbol = symbol;
    this.time = time;
  }

  public Date getTime() {
    return new Date(time);
  }
}
