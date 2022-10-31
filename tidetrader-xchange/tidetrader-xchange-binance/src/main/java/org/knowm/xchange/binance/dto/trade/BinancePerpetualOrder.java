package org.knowm.xchange.binance.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.knowm.xchange.binance.dto.trade.OrderSide;
import org.knowm.xchange.binance.dto.trade.OrderType;
import org.knowm.xchange.binance.dto.trade.TimeInForce;

import java.math.BigDecimal;

public class BinancePerpetualOrder {

    private final long orderId;
    private final String symbol;
    private final OrderSide side;
    private final OrderType type;
    private final BigDecimal executedQty;
    private final BigDecimal price;
    private final TimeInForce timeInForce;
    private final String clientOrderId;
    private final BigDecimal stopPrice;

    public BinancePerpetualOrder(
            @JsonProperty("symbol") String symbol,
            @JsonProperty("orderId") long orderId,
            @JsonProperty("side") OrderSide side,
            @JsonProperty("type") OrderType type,
            @JsonProperty("timeInForce") TimeInForce timeInForce,
            @JsonProperty("executedQty") BigDecimal executedQty,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("clientOrderId") String clientOrderId,
            @JsonProperty("stopPrice") BigDecimal stopPrice) {
        this.symbol = symbol;
        this.orderId = orderId;
        this.side = side;
        this.type = type;
        this.timeInForce = timeInForce;
        this.executedQty = executedQty;
        this.price = price;
        this.clientOrderId = clientOrderId;
        this.stopPrice = stopPrice;
    }

    public long getOrderId() {
        return orderId;
    }

    public String getSymbol() {
        return symbol;
    }

    public OrderSide getSide() {
        return side;
    }

    public OrderType getType() {
        return type;
    }

    public TimeInForce getTimeInForce() {
        return timeInForce;
    }

    public BigDecimal getExecutedQty() {
        return executedQty;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getClientOrderId() {
        return clientOrderId;
    }

    public BigDecimal getStopPrice() {
        return stopPrice;
    }


}
