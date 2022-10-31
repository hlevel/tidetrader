package org.knowm.xchange.binance.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class BinancePositionInfo {

    /**
     *   "entryPrice": "0.00000", // 开仓均价
     *         "marginType": "isolated", // 逐仓模式或全仓模式
     *         "isAutoAddMargin": "false",
     *         "isolatedMargin": "0.00000000", // 逐仓保证金
     *         "leverage": "10", // 当前杠杆倍数
     *         "liquidationPrice": "0", // 参考强平价格
     *         "markPrice": "6679.50671178",   // 当前标记价格
     *         "maxNotionalValue": "20000000", // 当前杠杆倍数允许的名义价值上限
     *         "positionAmt": "0.000", // 头寸数量，符号代表多空方向, 正数为多，负数为空
     *         "symbol": "BTCUSDT", // 交易对
     *         "unRealizedProfit": "0.00000000", // 持仓未实现盈亏
     *         "positionSide": "BOTH", // 持仓方向
     */

    BigDecimal entryPrice;

    String marginType;

    String isAutoAddMargin;
    @JsonProperty("isolatedMargin")
    BigDecimal isolatedMargin;
    @JsonProperty("leverage")
    Integer leverage;
    @JsonProperty("liquidationPrice")
    BigDecimal liquidationPrice;
    @JsonProperty("markPrice")
    BigDecimal markPrice;
    @JsonProperty("maxNotionalValue")
    Integer maxNotionalValue;
    @JsonProperty("positionAmt")
    BigDecimal positionAmt;
    @JsonProperty("symbol")
    String symbol;
    @JsonProperty("unRealizedProfit")
    BigDecimal unRealizedProfit;
    @JsonProperty("positionSide")
    String positionSide;

    public BinancePositionInfo(
            @JsonProperty("entryPrice") BigDecimal entryPrice,
            @JsonProperty("marginType") String marginType,
            @JsonProperty("isAutoAddMargin") String isAutoAddMargin,
            @JsonProperty("isolatedMargin") BigDecimal isolatedMargin,
            @JsonProperty("leverage") Integer leverage,
            @JsonProperty("liquidationPrice") BigDecimal liquidationPrice,
            @JsonProperty("markPrice") BigDecimal markPrice,
            @JsonProperty("maxNotionalValue") Integer maxNotionalValue,
            @JsonProperty("positionAmt") BigDecimal positionAmt,
            @JsonProperty("symbol") String symbol,
            @JsonProperty("unRealizedProfit") BigDecimal unRealizedProfit,
            @JsonProperty("positionSide") String positionSide) {
        this.entryPrice = entryPrice;
        this.marginType = marginType;
        this.isAutoAddMargin = isAutoAddMargin;
        this.isolatedMargin = isolatedMargin;
        this.leverage = leverage;
        this.liquidationPrice = liquidationPrice;
        this.markPrice = markPrice;
        this.maxNotionalValue = maxNotionalValue;
        this.positionAmt = positionAmt;
        this.symbol = symbol;
        this.unRealizedProfit = unRealizedProfit;
        this.positionSide = positionSide;
    }

    public BigDecimal getEntryPrice() {
        return entryPrice;
    }

    public String getMarginType() {
        return marginType;
    }

    public String getIsAutoAddMargin() {
        return isAutoAddMargin;
    }

    public BigDecimal getIsolatedMargin() {
        return isolatedMargin;
    }

    public Integer getLeverage() {
        return leverage;
    }

    public BigDecimal getLiquidationPrice() {
        return liquidationPrice;
    }

    public BigDecimal getMarkPrice() {
        return markPrice;
    }

    public Integer getMaxNotionalValue() {
        return maxNotionalValue;
    }

    public BigDecimal getPositionAmt() {
        return positionAmt;
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getUnRealizedProfit() {
        return unRealizedProfit;
    }

    public String getPositionSide() {
        return positionSide;
    }
}
