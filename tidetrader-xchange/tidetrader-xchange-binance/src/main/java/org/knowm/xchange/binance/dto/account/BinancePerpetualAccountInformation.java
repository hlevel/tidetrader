package org.knowm.xchange.binance.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Value;

import javax.ws.rs.GET;
import java.math.BigDecimal;
import java.util.List;

@Getter
public final class BinancePerpetualAccountInformation {

  /*
  {
    "feeTier": 0,  // 手续费等级
    "canTrade": true,  // 是否可以交易
    "canDeposit": true,  // 是否可以入金
    "canWithdraw": true, // 是否可以出金
    "updateTime": 0,     // 保留字段，请忽略
    "totalInitialMargin": "0.00000000",  // 当前所需起始保证金总额(存在逐仓请忽略), 仅计算usdt资产
    "totalMaintMargin": "0.00000000",  // 维持保证金总额, 仅计算usdt资产
    "totalWalletBalance": "23.72469206",   // 账户总余额, 仅计算usdt资产
    "totalUnrealizedProfit": "0.00000000",  // 持仓未实现盈亏总额, 仅计算usdt资产
    "totalMarginBalance": "23.72469206",  // 保证金总余额, 仅计算usdt资产
    "totalPositionInitialMargin": "0.00000000",  // 持仓所需起始保证金(基于最新标记价格), 仅计算usdt资产
    "totalOpenOrderInitialMargin": "0.00000000",  // 当前挂单所需起始保证金(基于最新标记价格), 仅计算usdt资产
    "totalCrossWalletBalance": "23.72469206",  // 全仓账户余额, 仅计算usdt资产
    "totalCrossUnPnl": "0.00000000",    // 全仓持仓未实现盈亏总额, 仅计算usdt资产
    "availableBalance": "23.72469206",       // 可用余额, 仅计算usdt资产
    "maxWithdrawAmount": "23.72469206"     // 最大可转出余额, 仅计算usdt资产
    "assets": [
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
        },
        {
            "asset": "BUSD",        //资产
            "walletBalance": "103.12345678",  //余额
            "unrealizedProfit": "0.00000000",  // 未实现盈亏
            "marginBalance": "103.12345678",  // 保证金余额
            "maintMargin": "0.00000000",    // 维持保证金
            "initialMargin": "0.00000000",  // 当前所需起始保证金
            "positionInitialMargin": "0.00000000",  // 持仓所需起始保证金(基于最新标记价格)
            "openOrderInitialMargin": "0.00000000", // 当前挂单所需起始保证金(基于最新标记价格)
            "crossWalletBalance": "103.12345678",  //全仓账户余额
            "crossUnPnl": "0.00000000" // 全仓持仓未实现盈亏
            "availableBalance": "103.12345678",       // 可用余额
            "maxWithdrawAmount": "103.12345678",     // 最大可转出余额
            "marginAvailable": true,   // 否可用作联合保证金
            "updateTime": 0  // 更新时间
           }
    ],
    "positions": [  // 头寸，将返回所有市场symbol。
        //根据用户持仓模式展示持仓方向，即单向模式下只返回BOTH持仓情况，双向模式下只返回 LONG 和 SHORT 持仓情况
        {
            "symbol": "BTCUSDT",  // 交易对
            "initialMargin": "0",   // 当前所需起始保证金(基于最新标记价格)
            "maintMargin": "0", //维持保证金
            "unrealizedProfit": "0.00000000",  // 持仓未实现盈亏
            "positionInitialMargin": "0",  // 持仓所需起始保证金(基于最新标记价格)
            "openOrderInitialMargin": "0",  // 当前挂单所需起始保证金(基于最新标记价格)
            "leverage": "100",  // 杠杆倍率
            "isolated": true,  // 是否是逐仓模式
            "entryPrice": "0.00000",  // 持仓成本价
            "maxNotional": "250000",  // 当前杠杆下用户可用的最大名义价值
            "bidNotional": "0",  // 买单净值，忽略
            "askNotional": "0",  // 卖单净值，忽略
            "positionSide": "BOTH",  // 持仓方向
            "positionAmt": "0",      // 持仓数量
            "updateTime": 0         // 更新时间
        }
    ]
}
   */

  private final Integer feeTier;   //手续费等级
  private final boolean canTrade; //是否可以交易
  private final boolean canDeposit; //是否可以入金
  private final boolean canWithdraw; // 是否可以出金
  private final long updateTime; // 保留字段，请忽略
  private final BigDecimal totalInitialMargin; // 当前所需起始保证金总额(存在逐仓请忽略), 仅计算usdt资产
  private final BigDecimal totalMaintMargin; // 维持保证金总额, 仅计算usdt资产
  private final BigDecimal totalWalletBalance; //  账户总余额, 仅计算usdt资产
  private final BigDecimal totalUnrealizedProfit; //  持仓未实现盈亏总额, 仅计算usdt资产
  private final BigDecimal totalMarginBalance; //  保证金总余额, 仅计算usdt资产
  private final BigDecimal totalPositionInitialMargin; //  持仓所需起始保证金(基于最新标记价格), 仅计算usdt资产
  private final BigDecimal totalOpenOrderInitialMargin; //  当前挂单所需起始保证金(基于最新标记价格), 仅计算usdt资产
  private final BigDecimal totalCrossWalletBalance; //  全仓账户余额, 仅计算usdt资产
  private final BigDecimal totalCrossUnPnl; // 全仓持仓未实现盈亏总额, 仅计算usdt资产
  private final BigDecimal availableBalance; //  可用余额, 仅计算usdt资产
  private final BigDecimal maxWithdrawAmount; //  最大可转出余额, 仅计算usdt资产

  private final List<BinanceBalancePerpetual> balancePerpetuals;
  private final List<BalancePosition> balancePositions;

  public BinancePerpetualAccountInformation(
      @JsonProperty("feeTier") Integer feeTier,
      @JsonProperty("canTrade") boolean canTrade,
      @JsonProperty("canDeposit") boolean canDeposit,
      @JsonProperty("canWithdraw") boolean canWithdraw,
      @JsonProperty("updateTime") long updateTime,
      @JsonProperty("totalInitialMargin") BigDecimal totalInitialMargin,
      @JsonProperty("totalMaintMargin") BigDecimal totalMaintMargin,
      @JsonProperty("totalWalletBalance") BigDecimal totalWalletBalance,
      @JsonProperty("totalUnrealizedProfit") BigDecimal totalUnrealizedProfit,
      @JsonProperty("totalMarginBalance") BigDecimal totalMarginBalance,
      @JsonProperty("totalPositionInitialMargin") BigDecimal totalPositionInitialMargin,
      @JsonProperty("totalOpenOrderInitialMargin") BigDecimal totalOpenOrderInitialMargin,
      @JsonProperty("totalCrossWalletBalance") BigDecimal totalCrossWalletBalance,
      @JsonProperty("totalCrossUnPnl") BigDecimal totalCrossUnPnl,
      @JsonProperty("availableBalance") BigDecimal availableBalance,
      @JsonProperty("maxWithdrawAmount") BigDecimal maxWithdrawAmount,
      @JsonProperty("assets") List<BinanceBalancePerpetual> balancePerpetuals,
      @JsonProperty("positions") List<BalancePosition> balancePositions) {
    this.feeTier = feeTier;
    this.canTrade = canTrade;
    this.canDeposit = canDeposit;
    this.canWithdraw = canWithdraw;
    this.updateTime = updateTime;
    this.totalInitialMargin = totalInitialMargin;
    this.totalMaintMargin = totalMaintMargin;
    this.totalWalletBalance = totalWalletBalance;
    this.totalUnrealizedProfit = totalUnrealizedProfit;
    this.totalMarginBalance = totalMarginBalance;
    this.totalPositionInitialMargin = totalPositionInitialMargin;
    this.totalOpenOrderInitialMargin = totalOpenOrderInitialMargin;
    this.totalCrossWalletBalance = totalCrossWalletBalance;
    this.totalCrossUnPnl = totalCrossUnPnl;
    this.availableBalance = availableBalance;
    this.maxWithdrawAmount = maxWithdrawAmount;
    this.balancePerpetuals = balancePerpetuals;
    this.balancePositions = balancePositions;
  }
}
