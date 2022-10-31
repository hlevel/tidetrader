package org.knowm.xchange.binance.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;

import java.math.BigDecimal;

@Data
public final class BalancePosition {

  /**
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
   */
  private final String symbol; //资产
  private final BigDecimal initialMargin; //当前所需起始保证金(基于最新标记价格)
  private final BigDecimal maintMargin;// 维持保证金
  private final BigDecimal unrealizedProfit;  // 持仓未实现盈亏
  private final BigDecimal positionInitialMargin; // 持仓所需起始保证金(基于最新标记价格)
  private final BigDecimal openOrderInitialMargin;  // 当前挂单所需起始保证金(基于最新标记价格)
  private final int leverage; // 杠杆倍率
  private final boolean isolated; // 是否是逐仓模式
  private final BigDecimal entryPrice; //持仓成本价
  private final BigDecimal maxNotional; // 当前杠杆下用户可用的最大名义价值
  private final BigDecimal bidNotional; // 买单净值，忽略
  private final BigDecimal askNotional; // 卖单净值，忽略
  private final String positionSide; // 持仓方向
  private final BigDecimal positionAmt; // 持仓数量
  private final long updateTime; //更新时间

  public BalancePosition(
      @JsonProperty("symbol") String symbol,
      @JsonProperty("initialMargin") BigDecimal initialMargin,
      @JsonProperty("maintMargin") BigDecimal maintMargin,
      @JsonProperty("unrealizedProfit") BigDecimal unrealizedProfit,
      @JsonProperty("positionInitialMargin") BigDecimal positionInitialMargin,
      @JsonProperty("openOrderInitialMargin") BigDecimal openOrderInitialMargin,
      @JsonProperty("leverage") int leverage,
      @JsonProperty("isolated") boolean isolated,
      @JsonProperty("entryPrice") BigDecimal entryPrice,
      @JsonProperty("maxNotional") BigDecimal maxNotional,
      @JsonProperty("bidNotional") BigDecimal bidNotional,
      @JsonProperty("askNotional") BigDecimal askNotional,
      @JsonProperty("positionSide") String positionSide,
      @JsonProperty("positionAmt") BigDecimal positionAmt,
      @JsonProperty("updateTime") long updateTime) {
    this.symbol = symbol;
    this.initialMargin = initialMargin;
    this.maintMargin = maintMargin;
    this.unrealizedProfit = unrealizedProfit;
    this.positionInitialMargin = positionInitialMargin;
    this.openOrderInitialMargin = openOrderInitialMargin;
    this.leverage = leverage;
    this.isolated = isolated;
    this.entryPrice = entryPrice;
    this.maxNotional = maxNotional;
    this.bidNotional = bidNotional;
    this.askNotional = askNotional;
    this.positionSide = positionSide;
    this.positionAmt = positionAmt;
    this.updateTime = updateTime;
  }


  public String toString() {
    return "[" + symbol + ", leverage=" + leverage + ", unrealizedProfit=" + unrealizedProfit + "]";
  }
}
