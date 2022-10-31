package org.knowm.xchange.binance.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class FundimgRatereq {
    /**
     * "symbol": "BTCUSDT",            // 交易对
     *  "fundingRate": "-0.03750000",   // 资金费率
     *  "fundingTime": 1570608000000,   // 资金费时
     */

    @JsonProperty("symbol")
    String symbol;
    @JsonProperty("fundingRate")
    BigDecimal fundingRate;
    @JsonProperty("fundingTime")
    private long fundingTime;

    public FundimgRatereq(String symbol, BigDecimal fundingRate, long fundingTime) {
        this.symbol = symbol;
        this.fundingRate = fundingRate;
        this.fundingTime = fundingTime;
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getFundingRate() {
        return fundingRate;
    }

    public long getFundingTime() {
        return fundingTime;
    }
}
