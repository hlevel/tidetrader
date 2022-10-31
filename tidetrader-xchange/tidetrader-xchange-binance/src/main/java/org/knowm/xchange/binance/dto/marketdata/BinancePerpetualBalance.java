package org.knowm.xchange.binance.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class BinancePerpetualBalance {

    /**
     *  [{
     *     "accountAlias": "mYuXAufWoCoCTi",// 账户唯一识别码
     *     "asset": "USDT",// 资产
     *     "balance": "125.34661415",// 总余额
     *     "crossWalletBalance": "125.34661415", // 全仓余额
     *     "crossUnPnl": "-0.01364922",// 全仓持仓未实现盈亏
     *     "availableBalance": "125.03275792", // 下单可用余额
     *     "maxWithdrawAmount": "125.03275792",// 最大可转出余额
     *     "marginAvailable": true,// 是否可用作联合保证金
     *     "updateTime": 1650700803561
     * }]
     */
    @JsonProperty("accountAlias")
    String accountAlias;
    @JsonProperty("asset")
    String asset;
    @JsonProperty("balance")
    BigDecimal balance;
    @JsonProperty("crossWalletBalance")
    BigDecimal crossWalletBalance;
    @JsonProperty("crossUnPnl")
    BigDecimal crossUnPnl;
    @JsonProperty("availableBalance")
    BigDecimal availableBalance;
    @JsonProperty("maxWithdrawAmount")
    BigDecimal maxWithdrawAmount;
    @JsonProperty("marginAvailable")
    boolean marginAvailable;
    @JsonProperty("updateTime")
    long updateTime;

    public BinancePerpetualBalance(
            @JsonProperty("accountAlias") String accountAlias,
            @JsonProperty("asset") String asset,
            @JsonProperty("balance") BigDecimal balance,
            @JsonProperty("crossWalletBalance") BigDecimal crossWalletBalance,
            @JsonProperty("crossUnPnl") BigDecimal crossUnPnl,
            @JsonProperty("availableBalance") BigDecimal availableBalance,
            @JsonProperty("maxWithdrawAmount") BigDecimal maxWithdrawAmount,
            @JsonProperty("marginAvailable") boolean marginAvailable,
            @JsonProperty("updateTime") long updateTime) {
        this.accountAlias = accountAlias;
        this.asset = asset;
        this.balance = balance;
        this.crossWalletBalance = crossWalletBalance;
        this.crossUnPnl = crossUnPnl;
        this.availableBalance = availableBalance;
        this.maxWithdrawAmount = maxWithdrawAmount;
        this.maxWithdrawAmount = maxWithdrawAmount;
        this.marginAvailable = marginAvailable;
        this.updateTime = updateTime;
    }

    public String getAccountAlias() {
        return accountAlias;
    }

    public String getAsset() {
        return asset;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getCrossWalletBalance() {
        return crossWalletBalance;
    }

    public BigDecimal getCrossUnPnl() {
        return crossUnPnl;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public BigDecimal getMaxWithdrawAmount() {
        return maxWithdrawAmount;
    }

    public boolean isMarginAvailable() {
        return marginAvailable;
    }

    public long getUpdateTime() {
        return updateTime;
    }

}
