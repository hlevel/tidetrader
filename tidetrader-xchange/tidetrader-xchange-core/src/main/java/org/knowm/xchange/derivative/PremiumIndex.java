package org.knowm.xchange.derivative;

import java.math.BigDecimal;

/**
 * 标记价格和资金费率
 * */
public class PremiumIndex {

    /**
     *  "symbol": "BTCUSDT", 交易对
     */
    private String symbol;

    /**
     * 标记价格
     * */
    private BigDecimal markPrice;

    /**
     * 指数价格
     * */
    private BigDecimal indexPrice;

    /**
     *  最近更新的资金费率
     * */
    private BigDecimal lastFundingRate;

    /**
     *  下次资金费时间
     * */
    private Long nextFundingTime;

    /**
     *  标的资产基础利率
     * */
    private BigDecimal interestRate;

    /**
     *  更新时间
     * */
    private Long time;


    public PremiumIndex(String symbol, BigDecimal markPrice, BigDecimal indexPrice, BigDecimal lastFundingRate, long nextFundingTime, BigDecimal interestRate, long time) {
        this.symbol = symbol;
        this.markPrice = markPrice;
        this.indexPrice = indexPrice;
        this.lastFundingRate = lastFundingRate;
        this.nextFundingTime = nextFundingTime;
        this.interestRate = interestRate;
        this.time = time;
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getMarkPrice() {
        return markPrice;
    }

    public BigDecimal getIndexPrice() {
        return indexPrice;
    }

    public BigDecimal getLastFundingRate() {
        return lastFundingRate;
    }

    public long getNextFundingTime() {
        return nextFundingTime;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public long getTime() {
        return time;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setMarkPrice(BigDecimal markPrice) {
        this.markPrice = markPrice;
    }

    public void setIndexPrice(BigDecimal indexPrice) {
        this.indexPrice = indexPrice;
    }

    public void setLastFundingRate(BigDecimal lastFundingRate) {
        this.lastFundingRate = lastFundingRate;
    }

    public void setNextFundingTime(Long nextFundingTime) {
        this.nextFundingTime = nextFundingTime;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
