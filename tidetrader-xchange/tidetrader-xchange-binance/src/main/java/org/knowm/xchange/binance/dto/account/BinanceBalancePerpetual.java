package org.knowm.xchange.binance.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.knowm.xchange.currency.Currency;

import java.math.BigDecimal;

@Data
public final class BinanceBalancePerpetual {

  /**
   {
   "asset": "USDT",        //资产
   "walletBalance": "23.72469206",  //余额
   "unrealizedProfit": "0.00000000",  // 未实现盈亏
   "marginBalance": "23.72469206",  // 保证金余额
   "maintMargin": "0.00000000",    // 维持保证金
   "initialMargin": "0.00000000",  // 当前所需起始保证金
   "positionInitialMargin": "0.00000000",  // 持仓所需起始保证金(基于最新标记价格)
   "openOrderInitialMargin": "0.00000000", // 当前挂单所需起始保证金(基于最新标记价格)
   "crossWalletBalance": "23.72469206",  //全仓账户余额
   "crossUnPnl": "0.00000000" // 全仓持仓未实现盈亏
   "availableBalance": "23.72469206",       // 可用余额
   "maxWithdrawAmount": "23.72469206",     // 最大可转出余额
   "marginAvailable": true,   // 是否可用作联合保证金
   "updateTime": 1625474304765  //更新时间
   }
   */
  private final Currency currency; //资产
  private final BigDecimal walletBalance; //余额
  private final BigDecimal unrealizedProfit;// 未实现盈亏
  private final BigDecimal marginBalance;  // 保证金余额
  private final BigDecimal maintMargin; // 维持保证金
  private final BigDecimal initialMargin;  // 当前所需起始保证金
  private final BigDecimal positionInitialMargin; // 持仓所需起始保证金(基于最新标记价格)
  private final BigDecimal openOrderInitialMargin; // 当前挂单所需起始保证金(基于最新标记价格)
  private final BigDecimal crossWalletBalance; //全仓账户余额
  private final BigDecimal crossUnPnl; // 全仓持仓未实现盈亏
  private final BigDecimal availableBalance; // 可用余额
  private final BigDecimal maxWithdrawAmount; // 最大可转出余额
  private final boolean marginAvailable; // 是否可用作联合保证金
  private final long updateTime; //更新时间

  public BinanceBalancePerpetual(
      @JsonProperty("asset") String asset,
      @JsonProperty("walletBalance") BigDecimal walletBalance,
      @JsonProperty("unrealizedProfit") BigDecimal unrealizedProfit,
      @JsonProperty("marginBalance") BigDecimal marginBalance,
      @JsonProperty("maintMargin") BigDecimal maintMargin,
      @JsonProperty("initialMargin") BigDecimal initialMargin,
      @JsonProperty("positionInitialMargin") BigDecimal positionInitialMargin,
      @JsonProperty("openOrderInitialMargin") BigDecimal openOrderInitialMargin,
      @JsonProperty("crossWalletBalance") BigDecimal crossWalletBalance,
      @JsonProperty("crossUnPnl") BigDecimal crossUnPnl,
      @JsonProperty("availableBalance") BigDecimal availableBalance,
      @JsonProperty("maxWithdrawAmount") BigDecimal maxWithdrawAmount,
      @JsonProperty("marginAvailable") boolean marginAvailable,
      @JsonProperty("updateTime") long updateTime) {
    this.currency = Currency.getInstance(asset);
    this.walletBalance = walletBalance;
    this.unrealizedProfit = unrealizedProfit;
    this.marginBalance = marginBalance;
    this.maintMargin = maintMargin;
    this.initialMargin = initialMargin;
    this.positionInitialMargin = positionInitialMargin;
    this.openOrderInitialMargin = openOrderInitialMargin;
    this.crossWalletBalance = crossWalletBalance;
    this.crossUnPnl = crossUnPnl;
    this.availableBalance = availableBalance;
    this.maxWithdrawAmount = maxWithdrawAmount;
    this.marginAvailable = marginAvailable;
    this.updateTime = updateTime;
  }

  public Currency getCurrency() {
    return currency;
  }


  public String toString() {
    return "[" + currency + ", walletBalance=" + walletBalance + ", unrealizedProfit=" + unrealizedProfit + "]";
  }
}
