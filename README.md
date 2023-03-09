tidetrader
============================

#### 介绍
本项目是虚拟币量化交易机器人，扩展了xchange的源码支持币安交易所的现货、永续两种类型，基于 [https://github.com/cassandre-tech/cassandre-trading-bot](https://github.com/cassandre-tech/cassandre-trading-bot) 做的二次开发。
，在策略支持三种类型 基础策略、ta4j趋势策略、tv信号策略，在功能方面支持回测、仓位管理、图形化界面、追踪止赢止损、杠杆倍数。本项目没有现成的策略提供，只是一个交易量化平台项目，需要自己动手构建自己策略，所以适用对象是具有java开发编程能力并且熟悉货币市场情况的用户。

#### 实盘预览
http://b.ks1.top:9082/strategy/3

#### 功能支持
* binance现货
* binance永续
* 模拟回测
* 普通策略
* ta4j指标策略
* tv信号策略
* 图形webui

软件架构:
* Java 11
* SpringBoot2.6.4
* graphql

#### 普通策略
```java
@CassandreStrategy(strategyId = "1", strategyName = "ExampleStrategy")
public class ExampleStrategy extends BasicCassandreStrategy {
 
}
```
#### ta4j指标策略
```java
@CassandreStrategy(strategyId = "2", strategyName = "ExampleTa4jStrategy")
public class ExampleTa4jStrategy extends BasicTa4jCassandreStrategy {

    @Override
    public Set<DurationMaximumBar> getRequestedDurationMaximumBars() {
        //三个参数分别为 品种、周期、最大k线线数
        return Set.of(new DurationMaximumBar(new CurrencyPairDTO(CurrencyDTO.BTC, CurrencyDTO.USDT), Duration.ofDays(1), 10));
    }

    @Override
    public IndicatorRule getIndicatorRule(DurationMaximumBar bar) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(getSeries(bar));
        SMAIndicator sma = new SMAIndicator(closePrice, 10);

        Rule entryLongRule = new UnderIndicatorRule(sma, closePrice);
        Rule entryShortRule = new OverIndicatorRule(sma, closePrice);
        Rule exitLongRule = new OverIndicatorRule(sma, closePrice);
        Rule exitShortRule = new OverIndicatorRule(sma, closePrice);
        return new BaseIndicatorRule(entryLongRule, entryShortRule, exitLongRule, exitShortRule);
    }

    @Override
    public void shouldPosition(SideDTO side, TickerDTO durationTicker) {
        if(side == SideDTO.LONG || side == SideDTO.SHORT) {

        } else {

        }
    }

    @Override
    public Optional<AccountDTO> getTradeAccount(Set<AccountDTO> accounts) {
        // From all the accounts we have on the exchange, we must return the one we use for trading.
        if (accounts.size() == 1) {
            // If there is only one on the exchange, we choose this one.
            return accounts.stream().findFirst();
        } else {
            // If there are several accounts on the exchange, we choose the one with the name "trade".
            return accounts.stream()
                    .filter(a -> "trade".equalsIgnoreCase(a.getName()))
                    .findFirst();
        }
    }
}
```
#### tradingview 信号策略
```java
@CassandreStrategy(strategyId = "3", strategyName = "ExampleTVStrategy")
public class ExampleTVStrategy extends BasicSingalCassandreStrategy {

}
```
#### 交易所配置
配置文件配置了加密方法, dpw:xxx 通过 [DESUtil](./tidetrader-strategies/src/main/java/tide/trader/bot/util/DESUtil.java) 进行加解密
```properties
# Exchange configuration.domain[spot,perpetual]
trading.bot.exchange.driver-class-name=binance
trading.bot.exchange.domain=perpetual
trading.bot.exchange.username=xx@gmail.com
trading.bot.exchange.passphrase=IE5HU2VFBJ
trading.bot.exchange.key=dpw:加密
trading.bot.exchange.secret=dpw:加密
trading.bot.exchange.proxyHost=
trading.bot.exchange.proxyPort=
```
#### 通知配置
支持三种通知、邮件、钉钉、微信插件通知
```properties
trading.bot.exchange.mail.enable=true
trading.bot.exchange.mail.host=smtp.163.com
trading.bot.exchange.mail.port=465
trading.bot.exchange.mail.ssl=true
trading.bot.exchange.mail.username=hlevel@163.com
trading.bot.exchange.mail.password=dpw:加密
trading.bot.exchange.mail.timout=25000
trading.bot.exchange.mail.encoding=utf8
trading.bot.exchange.mail.to=

#Notification configuration
trading.bot.exchange.wechat.username=zkang

trading.bot.exchange.dingding.username=zkang
trading.bot.exchange.dingding.atMobiles=
```
#### webui后台登录配置
```properties
#Web login authentication
trading.bot.security.username=dpw:xxxxx
trading.bot.security.password=dpw:xxxxx
```
#### 模拟运行
[SimulatedStrategyTest](./tidetrader-strategies/src/test/java/tide/trader/bot/SimulatedStrategyTest.java) 运行后输出结果
```java
Account balances:
 - USDT : 739.7970198833083071
 - LTC : 0.2
Account openPositions:
 - OpenPositionDTO(currencyPair=BTC/USDT, type=LONG, amount=0.02312442, price=19560.70000000, liquidationPrice=null, margin=452.3209268566639630)
Cumulated gains:
 - USDT : 177.54723 USDT
Position closed:
 - Long position n°4 of 0.0107 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -9.7905 USDT (-1.97 %) - 10 Day line exit  
  + 2022-04-03 08:00:00 DRY_ORDER_000000007 46392.4 BID 0.0107 FILLED  
  + 2022-04-05 08:00:00 DRY_ORDER_000000008 45477.4 ASK 0.0107 FILLED
 - Short position n°5 of 0.0109 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 25.26293 USDT (5.37 %) - 10 Day line exit  
  + 2022-04-05 08:00:00 DRY_ORDER_000000009 45477.4 ASK 0.0109 FILLED  
  + 2022-04-06 08:00:00 DRY_ORDER_000000010 43159.7 BID 0.0109 FILLED
 - Short position n°6 of 0.0115 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 13.66085 USDT (2.81 %) - 10 Day line exit  
  + 2022-04-07 08:00:00 DRY_ORDER_000000011 43417.1 ASK 0.0115 FILLED  
  + 2022-04-08 08:00:00 DRY_ORDER_000000012 42229.2 BID 0.0115 FILLED
 - Short position n°7 of 0.0117 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 6.87141 USDT (1.39 %) - 10 Day line exit  
  + 2022-04-09 08:00:00 DRY_ORDER_000000013 42727.7 ASK 0.0117 FILLED  
  + 2022-04-10 08:00:00 DRY_ORDER_000000014 42140.4 BID 0.0117 FILLED
 - Short position n°8 of 0.0126 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -6.99426 USDT (-1.39 %) - 10 Day line exit  
  + 2022-04-11 08:00:00 DRY_ORDER_000000015 39505.6 ASK 0.0126 FILLED  
  + 2022-04-12 08:00:00 DRY_ORDER_000000016 40060.7 BID 0.0126 FILLED
 - Short position n°9 of 0.0121 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 14.68819 USDT (3.04 %) - 10 Day line exit  
  + 2022-04-13 08:00:00 DRY_ORDER_000000017 41129.8 ASK 0.0121 FILLED  
  + 2022-04-14 08:00:00 DRY_ORDER_000000018 39915.9 BID 0.0121 FILLED
 - Short position n°10 of 0.0123 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 2.07255 USDT (0.42 %) - 10 Day line exit  
  + 2022-04-15 08:00:00 DRY_ORDER_000000019 40529.5 ASK 0.0123 FILLED  
  + 2022-04-16 08:00:00 DRY_ORDER_000000020 40361 BID 0.0123 FILLED
 - Short position n°11 of 0.0126 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -14.3199 USDT (-2.79 %) - Reverse position  
  + 2022-04-17 08:00:00 DRY_ORDER_000000021 39649.1 ASK 0.0126 FILLED  
  + 2022-04-18 08:00:00 DRY_ORDER_000000022 40785.6 BID 0.0126 FILLED
 - Long position n°12 of 0.0122 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -4.09676 USDT (-0.82 %) - 10 Day line exit  
  + 2022-04-18 08:00:00 DRY_ORDER_000000023 40785.6 BID 0.0122 FILLED  
  + 2022-04-21 08:00:00 DRY_ORDER_000000024 40449.8 ASK 0.0122 FILLED
 - Short position n°13 of 0.0123 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 9.53988 USDT (1.95 %) - 10 Day line exit  
  + 2022-04-21 08:00:00 DRY_ORDER_000000025 40449.8 ASK 0.0123 FILLED  
  + 2022-04-22 08:00:00 DRY_ORDER_000000026 39674.2 BID 0.0123 FILLED
 - Short position n°14 of 0.0126 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -0.23058 USDT (-0.05 %) - 10 Day line exit  
  + 2022-04-23 08:00:00 DRY_ORDER_000000027 39415.1 ASK 0.0126 FILLED  
  + 2022-04-24 08:00:00 DRY_ORDER_000000028 39433.4 BID 0.0126 FILLED
 - Long position n°15 of 0.0123 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -28.50279 USDT (-5.73 %) - Stop loss  
  + 2022-04-25 08:00:00 DRY_ORDER_000000029 40411 BID 0.0123 FILLED  
  + 2022-04-26 08:00:00 DRY_ORDER_000000030 38093.7 ASK 0.0123 FILLED
 - Short position n°16 of 0.0131 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -14.61043 USDT (-2.84 %) - 10 Day line exit  
  + 2022-04-26 08:00:00 DRY_ORDER_000000031 38093.7 ASK 0.0131 FILLED  
  + 2022-04-27 08:00:00 DRY_ORDER_000000032 39209 BID 0.0131 FILLED
 - Short position n°17 of 0.0125 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 14.50375 USDT (3.01 %) - 10 Day line exit  
  + 2022-04-28 08:00:00 DRY_ORDER_000000033 39732.5 ASK 0.0125 FILLED  
  + 2022-04-29 08:00:00 DRY_ORDER_000000034 38572.2 BID 0.0125 FILLED
 - Short position n°18 of 0.0132 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -10.9032 USDT (-2.15 %) - 10 Day line exit  
  + 2022-04-30 08:00:00 DRY_ORDER_000000035 37614.5 ASK 0.0132 FILLED  
  + 2022-05-01 08:00:00 DRY_ORDER_000000036 38440.5 BID 0.0132 FILLED
 - Short position n°19 of 0.0129 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 10.48383 USDT (2.15 %) - 10 Day line exit  
  + 2022-05-02 08:00:00 DRY_ORDER_000000037 38525.6 ASK 0.0129 FILLED  
  + 2022-05-03 08:00:00 DRY_ORDER_000000038 37712.9 BID 0.0129 FILLED
 - Long position n°20 of 0.0125 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -39.36625 USDT (-7.94 %) - Stop loss  
  + 2022-05-04 08:00:00 DRY_ORDER_000000039 39684 BID 0.0125 FILLED  
  + 2022-05-05 08:00:00 DRY_ORDER_000000040 36534.7 ASK 0.0125 FILLED
 - Short position n°21 of 0.0136 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 7.33584 USDT (1.5 %) - 10 Day line exit  
  + 2022-05-05 08:00:00 DRY_ORDER_000000041 36534.7 ASK 0.0136 FILLED  
  + 2022-05-06 08:00:00 DRY_ORDER_000000042 35995.3 BID 0.0136 FILLED
 - Short position n°22 of 0.014 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 20.0858 USDT (4.22 %) - 10 Day line exit  
  + 2022-05-07 08:00:00 DRY_ORDER_000000043 35466.7 ASK 0.014 FILLED  
  + 2022-05-08 08:00:00 DRY_ORDER_000000044 34032 BID 0.014 FILLED
 - Short position n°23 of 0.0166 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -15.6953 USDT (-3.05 %) - 10 Day line exit  
  + 2022-05-09 08:00:00 DRY_ORDER_000000045 30056.6 ASK 0.0166 FILLED  
  + 2022-05-10 08:00:00 DRY_ORDER_000000046 31002.1 BID 0.0166 FILLED
 - Short position n°24 of 0.0171 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 0.9234 USDT (0.19 %) - 10 Day line exit  
  + 2022-05-11 08:00:00 DRY_ORDER_000000047 29074.7 ASK 0.0171 FILLED  
  + 2022-05-12 08:00:00 DRY_ORDER_000000048 29020.7 BID 0.0171 FILLED
 - Short position n°25 of 0.017 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -13.6935 USDT (-2.68 %) - 10 Day line exit  
  + 2022-05-13 08:00:00 DRY_ORDER_000000049 29274.3 ASK 0.017 FILLED  
  + 2022-05-14 08:00:00 DRY_ORDER_000000050 30079.8 BID 0.017 FILLED
 - Short position n°26 of 0.0159 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 23.17743 USDT (4.88 %) - 10 Day line exit  
  + 2022-05-15 08:00:00 DRY_ORDER_000000051 31324.4 ASK 0.0159 FILLED  
  + 2022-05-16 08:00:00 DRY_ORDER_000000052 29866.7 BID 0.0159 FILLED
 - Long position n°27 of 0.0164 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -28.47696 USDT (-5.7 %) - Stop loss  
  + 2022-05-17 08:00:00 DRY_ORDER_000000053 30437 BID 0.0164 FILLED  
  + 2022-05-18 08:00:00 DRY_ORDER_000000054 28700.6 ASK 0.0164 FILLED
 - Short position n°28 of 0.0174 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -28.17582 USDT (-5.34 %) - Stop loss  
  + 2022-05-18 08:00:00 DRY_ORDER_000000055 28700.6 ASK 0.0174 FILLED  
  + 2022-05-19 08:00:00 DRY_ORDER_000000056 30319.9 BID 0.0174 FILLED
 - Long position n°29 of 0.0164 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -18.5484 USDT (-3.73 %) - 10 Day line exit  
  + 2022-05-19 08:00:00 DRY_ORDER_000000057 30319.9 BID 0.0164 FILLED  
  + 2022-05-20 08:00:00 DRY_ORDER_000000058 29188.9 ASK 0.0164 FILLED
 - Short position n°30 of 0.0171 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -4.07835 USDT (-0.81 %) - 10 Day line exit  
  + 2022-05-20 08:00:00 DRY_ORDER_000000059 29188.9 ASK 0.0171 FILLED  
  + 2022-05-21 08:00:00 DRY_ORDER_000000060 29427.4 BID 0.0171 FILLED
 - Long position n°31 of 0.0165 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -19.57395 USDT (-3.92 %) - 10 Day line exit  
  + 2022-05-22 08:00:00 DRY_ORDER_000000061 30284 BID 0.0165 FILLED  
  + 2022-05-23 08:00:00 DRY_ORDER_000000062 29097.7 ASK 0.0165 FILLED
 - Short position n°32 of 0.0171 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -9.32634 USDT (-1.84 %) - 10 Day line exit  
  + 2022-05-23 08:00:00 DRY_ORDER_000000063 29097.7 ASK 0.0171 FILLED  
  + 2022-05-24 08:00:00 DRY_ORDER_000000064 29643.1 BID 0.0171 FILLED
 - Short position n°33 of 0.0169 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 5.79163 USDT (1.17 %) - 10 Day line exit  
  + 2022-05-25 08:00:00 DRY_ORDER_000000065 29536.7 ASK 0.0169 FILLED  
  + 2022-05-26 08:00:00 DRY_ORDER_000000066 29194 BID 0.0169 FILLED
 - Short position n°34 of 0.0174 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -6.88344 USDT (-1.36 %) - 10 Day line exit  
  + 2022-05-27 08:00:00 DRY_ORDER_000000067 28623.2 ASK 0.0174 FILLED  
  + 2022-05-28 08:00:00 DRY_ORDER_000000068 29018.8 BID 0.0174 FILLED
 - Long position n°35 of 0.0169 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 3.887 USDT (0.78 %) - 10 Day line exit  
  + 2022-05-29 08:00:00 DRY_ORDER_000000069 29452.8 BID 0.0169 FILLED  
  + 2022-06-03 08:00:00 DRY_ORDER_000000070 29682.8 ASK 0.0169 FILLED
 - Short position n°36 of 0.0168 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -2.78208 USDT (-0.55 %) - 10 Day line exit  
  + 2022-06-03 08:00:00 DRY_ORDER_000000071 29682.8 ASK 0.0168 FILLED  
  + 2022-06-04 08:00:00 DRY_ORDER_000000072 29848.4 BID 0.0168 FILLED
 - Short position n°37 of 0.0167 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -24.23671 USDT (-4.63 %) - Reverse position  
  + 2022-06-05 08:00:00 DRY_ORDER_000000073 29909.9 ASK 0.0167 FILLED  
  + 2022-06-06 08:00:00 DRY_ORDER_000000074 31361.2 BID 0.0167 FILLED
 - Long position n°38 of 0.0159 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -18.58233 USDT (-3.73 %) - 10 Day line exit  
  + 2022-06-06 08:00:00 DRY_ORDER_000000075 31361.2 BID 0.0159 FILLED  
  + 2022-06-08 08:00:00 DRY_ORDER_000000076 30192.5 ASK 0.0159 FILLED
 - Short position n°39 of 0.0165 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 1.6368 USDT (0.33 %) - 10 Day line exit  
  + 2022-06-08 08:00:00 DRY_ORDER_000000077 30192.5 ASK 0.0165 FILLED  
  + 2022-06-09 08:00:00 DRY_ORDER_000000078 30093.3 BID 0.0165 FILLED
 - Short position n°40 of 0.0171 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 11.3031 USDT (2.33 %) - 10 Day line exit  
  + 2022-06-10 08:00:00 DRY_ORDER_000000079 29077.7 ASK 0.0171 FILLED  
  + 2022-06-11 08:00:00 DRY_ORDER_000000080 28416.7 BID 0.0171 FILLED
 - Short position n°41 of 0.0188 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 76.95216 USDT (18.22 %) - Take profit  
  + 2022-06-12 08:00:00 DRY_ORDER_000000081 26564.7 ASK 0.0188 FILLED  
  + 2022-06-13 08:00:00 DRY_ORDER_000000082 22471.5 BID 0.0188 FILLED
 - Short position n°42 of 0.0222 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 7.74336 USDT (1.58 %) - 10 Day line exit  
  + 2022-06-13 08:00:00 DRY_ORDER_000000083 22471.5 ASK 0.0222 FILLED  
  + 2022-06-14 08:00:00 DRY_ORDER_000000084 22122.7 BID 0.0222 FILLED
 - Short position n°43 of 0.0221 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 48.18021 USDT (10.69 %) - 10 Day line exit  
  + 2022-06-15 08:00:00 DRY_ORDER_000000085 22567.5 ASK 0.0221 FILLED  
  + 2022-06-16 08:00:00 DRY_ORDER_000000086 20387.4 BID 0.0221 FILLED
 - Short position n°44 of 0.0244 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 36.54144 USDT (7.9 %) - 10 Day line exit  
  + 2022-06-17 08:00:00 DRY_ORDER_000000087 20457.3 ASK 0.0244 FILLED  
  + 2022-06-18 08:00:00 DRY_ORDER_000000088 18959.7 BID 0.0244 FILLED
 - Short position n°45 of 0.0243 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -0.14337 USDT (-0.03 %) - 10 Day line exit  
  + 2022-06-19 08:00:00 DRY_ORDER_000000089 20564.2 ASK 0.0243 FILLED  
  + 2022-06-20 08:00:00 DRY_ORDER_000000090 20570.1 BID 0.0243 FILLED
 - Short position n°46 of 0.0241 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 17.73037 USDT (3.68 %) - 10 Day line exit  
  + 2022-06-21 08:00:00 DRY_ORDER_000000091 20713.1 ASK 0.0241 FILLED  
  + 2022-06-22 08:00:00 DRY_ORDER_000000092 19977.4 BID 0.0241 FILLED
 - Long position n°47 of 0.0237 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -19.6236 USDT (-3.92 %) - 10 Day line exit  
  + 2022-06-23 08:00:00 DRY_ORDER_000000093 21096.6 BID 0.0237 FILLED  
  + 2022-06-28 08:00:00 DRY_ORDER_000000094 20268.6 ASK 0.0237 FILLED
 - Short position n°48 of 0.0246 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 3.90402 USDT (0.79 %) - 10 Day line exit  
  + 2022-06-28 08:00:00 DRY_ORDER_000000095 20268.6 ASK 0.0246 FILLED  
  + 2022-06-29 08:00:00 DRY_ORDER_000000096 20109.9 BID 0.0246 FILLED
 - Short position n°49 of 0.025 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 16.2875 USDT (3.38 %) - 10 Day line exit  
  + 2022-06-30 08:00:00 DRY_ORDER_000000097 19923.5 ASK 0.025 FILLED  
  + 2022-07-01 08:00:00 DRY_ORDER_000000098 19272 BID 0.025 FILLED
 - Short position n°50 of 0.0259 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -1.6835 USDT (-0.34 %) - 10 Day line exit  
  + 2022-07-02 08:00:00 DRY_ORDER_000000099 19240.9 ASK 0.0259 FILLED  
  + 2022-07-03 08:00:00 DRY_ORDER_000000100 19305.9 BID 0.0259 FILLED
 - Long position n°51 of 0.0247 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -6.72581 USDT (-1.35 %) - 10 Day line exit  
  + 2022-07-04 08:00:00 DRY_ORDER_000000101 20226.6 BID 0.0247 FILLED  
  + 2022-07-11 08:00:00 DRY_ORDER_000000102 19954.3 ASK 0.0247 FILLED
 - Short position n°52 of 0.025 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 15.785 USDT (3.27 %) - 10 Day line exit  
  + 2022-07-11 08:00:00 DRY_ORDER_000000103 19954.3 ASK 0.025 FILLED  
  + 2022-07-12 08:00:00 DRY_ORDER_000000104 19322.9 BID 0.025 FILLED
 - Short position n°53 of 0.0247 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -8.85495 USDT (-1.74 %) - 10 Day line exit  
  + 2022-07-13 08:00:00 DRY_ORDER_000000105 20220.1 ASK 0.0247 FILLED  
  + 2022-07-14 08:00:00 DRY_ORDER_000000106 20578.6 BID 0.0247 FILLED
 - Long position n°54 of 0.024 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 61.5072 USDT (12.31 %) - Take profit  
  + 2022-07-15 08:00:00 DRY_ORDER_000000107 20823.1 BID 0.024 FILLED  
  + 2022-07-19 08:00:00 DRY_ORDER_000000108 23385.9 ASK 0.024 FILLED
 - Long position n°55 of 0.0213 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -44.42967 USDT (-8.92 %) - Stop loss  
  + 2022-07-19 08:00:00 DRY_ORDER_000000109 23385.9 BID 0.0213 FILLED  
  + 2022-07-25 08:00:00 DRY_ORDER_000000110 21300 ASK 0.0213 FILLED
 - Short position n°56 of 0.0234 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 1.3806 USDT (0.28 %) - 10 Day line exit  
  + 2022-07-25 08:00:00 DRY_ORDER_000000111 21300 ASK 0.0234 FILLED  
  + 2022-07-26 08:00:00 DRY_ORDER_000000112 21241 BID 0.0234 FILLED
 - Long position n°57 of 0.0217 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -3.02932 USDT (-0.61 %) - 10 Day line exit  
  + 2022-07-27 08:00:00 DRY_ORDER_000000113 22941.1 BID 0.0217 FILLED  
  + 2022-08-03 08:00:00 DRY_ORDER_000000114 22801.5 ASK 0.0217 FILLED
 - Short position n°58 of 0.0219 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 4.21794 USDT (0.85 %) - 10 Day line exit  
  + 2022-08-03 08:00:00 DRY_ORDER_000000115 22801.5 ASK 0.0219 FILLED  
  + 2022-08-04 08:00:00 DRY_ORDER_000000116 22608.9 BID 0.0219 FILLED
 - Long position n°59 of 0.0214 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -7.59272 USDT (-1.52 %) - 10 Day line exit  
  + 2022-08-05 08:00:00 DRY_ORDER_000000117 23298.1 BID 0.0214 FILLED  
  + 2022-08-06 08:00:00 DRY_ORDER_000000118 22943.3 ASK 0.0214 FILLED
 - Short position n°60 of 0.0217 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -4.87165 USDT (-0.97 %) - 10 Day line exit  
  + 2022-08-06 08:00:00 DRY_ORDER_000000119 22943.3 ASK 0.0217 FILLED  
  + 2022-08-07 08:00:00 DRY_ORDER_000000120 23167.8 BID 0.0217 FILLED
 - Long position n°61 of 0.021 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 0.8106 USDT (0.16 %) - 10 Day line exit  
  + 2022-08-08 08:00:00 DRY_ORDER_000000121 23804.4 BID 0.021 FILLED  
  + 2022-08-16 08:00:00 DRY_ORDER_000000122 23843 ASK 0.021 FILLED
 - Short position n°62 of 0.0209 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 10.65691 USDT (2.19 %) - 10 Day line exit  
  + 2022-08-16 08:00:00 DRY_ORDER_000000123 23843 ASK 0.0209 FILLED  
  + 2022-08-17 08:00:00 DRY_ORDER_000000124 23333.1 BID 0.0209 FILLED
 - Short position n°63 of 0.0215 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 50.7443 USDT (11.33 %) - Take profit  
  + 2022-08-18 08:00:00 DRY_ORDER_000000125 23185.1 ASK 0.0215 FILLED  
  + 2022-08-19 08:00:00 DRY_ORDER_000000126 20824.9 BID 0.0215 FILLED
 - Short position n°64 of 0.024 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -7.2696 USDT (-1.43 %) - 10 Day line exit  
  + 2022-08-19 08:00:00 DRY_ORDER_000000127 20824.9 ASK 0.024 FILLED  
  + 2022-08-20 08:00:00 DRY_ORDER_000000128 21127.8 BID 0.024 FILLED
 - Short position n°65 of 0.0232 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 2.75152 USDT (0.55 %) - 10 Day line exit  
  + 2022-08-21 08:00:00 DRY_ORDER_000000129 21505.6 ASK 0.0232 FILLED  
  + 2022-08-22 08:00:00 DRY_ORDER_000000130 21387 BID 0.0232 FILLED
 - Short position n°66 of 0.0232 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 3.78856 USDT (0.76 %) - 10 Day line exit  
  + 2022-08-23 08:00:00 DRY_ORDER_000000131 21518.2 ASK 0.0232 FILLED  
  + 2022-08-24 08:00:00 DRY_ORDER_000000132 21354.9 BID 0.0232 FILLED
 - Short position n°67 of 0.0232 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 30.61472 USDT (6.52 %) - 10 Day line exit  
  + 2022-08-25 08:00:00 DRY_ORDER_000000133 21548.3 ASK 0.0232 FILLED  
  + 2022-08-26 08:00:00 DRY_ORDER_000000134 20228.7 BID 0.0232 FILLED
 - Short position n°68 of 0.0249 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 11.86236 USDT (2.44 %) - 10 Day line exit  
  + 2022-08-27 08:00:00 DRY_ORDER_000000135 20023.9 ASK 0.0249 FILLED  
  + 2022-08-28 08:00:00 DRY_ORDER_000000136 19547.5 BID 0.0249 FILLED
 - Short position n°69 of 0.0246 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 11.62104 USDT (2.39 %) - 10 Day line exit  
  + 2022-08-29 08:00:00 DRY_ORDER_000000137 20275.5 ASK 0.0246 FILLED  
  + 2022-08-30 08:00:00 DRY_ORDER_000000138 19803.1 BID 0.0246 FILLED
 - Short position n°70 of 0.0249 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -2.0169 USDT (-0.4 %) - 10 Day line exit  
  + 2022-08-31 08:00:00 DRY_ORDER_000000139 20041.5 ASK 0.0249 FILLED  
  + 2022-09-01 08:00:00 DRY_ORDER_000000140 20122.5 BID 0.0249 FILLED
 - Short position n°71 of 0.025 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 3.0075 USDT (0.61 %) - 10 Day line exit  
  + 2022-09-02 08:00:00 DRY_ORDER_000000141 19941 ASK 0.025 FILLED  
  + 2022-09-03 08:00:00 DRY_ORDER_000000142 19820.7 BID 0.025 FILLED
 - Long position n°72 of 0.025 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -5.195 USDT (-1.04 %) - 10 Day line exit  
  + 2022-09-04 08:00:00 DRY_ORDER_000000143 19992.7 BID 0.025 FILLED  
  + 2022-09-05 08:00:00 DRY_ORDER_000000144 19784.9 ASK 0.025 FILLED
 - Short position n°73 of 0.0252 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 25.28568 USDT (5.34 %) - 10 Day line exit  
  + 2022-09-05 08:00:00 DRY_ORDER_000000145 19784.9 ASK 0.0252 FILLED  
  + 2022-09-06 08:00:00 DRY_ORDER_000000146 18781.5 BID 0.0252 FILLED
 - Short position n°74 of 0.0259 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -0.57757 USDT (-0.12 %) - 10 Day line exit  
  + 2022-09-07 08:00:00 DRY_ORDER_000000147 19287 ASK 0.0259 FILLED  
  + 2022-09-08 08:00:00 DRY_ORDER_000000148 19309.3 BID 0.0259 FILLED
 - Long position n°75 of 0.0234 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -27.8343 USDT (-5.57 %) - Stop loss  
  + 2022-09-09 08:00:00 DRY_ORDER_000000149 21352 BID 0.0234 FILLED  
  + 2022-09-13 08:00:00 DRY_ORDER_000000150 20162.5 ASK 0.0234 FILLED
 - Short position n°76 of 0.0247 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -1.34615 USDT (-0.27 %) - 10 Day line exit  
  + 2022-09-13 08:00:00 DRY_ORDER_000000151 20162.5 ASK 0.0247 FILLED  
  + 2022-09-14 08:00:00 DRY_ORDER_000000152 20217 BID 0.0247 FILLED
 - Short position n°77 of 0.0253 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -2.57301 USDT (-0.51 %) - 10 Day line exit  
  + 2022-09-15 08:00:00 DRY_ORDER_000000153 19690.8 ASK 0.0253 FILLED  
  + 2022-09-16 08:00:00 DRY_ORDER_000000154 19792.5 BID 0.0253 FILLED
 - Short position n°78 of 0.0248 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 17.298 USDT (3.59 %) - 10 Day line exit  
  + 2022-09-17 08:00:00 DRY_ORDER_000000155 20102 ASK 0.0248 FILLED  
  + 2022-09-18 08:00:00 DRY_ORDER_000000156 19404.5 BID 0.0248 FILLED
 - Short position n°79 of 0.0256 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 16.94208 USDT (3.51 %) - 10 Day line exit  
  + 2022-09-19 08:00:00 DRY_ORDER_000000157 19528.6 ASK 0.0256 FILLED  
  + 2022-09-20 08:00:00 DRY_ORDER_000000158 18866.8 BID 0.0256 FILLED
 - Short position n°80 of 0.0271 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -25.6095 USDT (-4.87 %) - Stop loss  
  + 2022-09-21 08:00:00 DRY_ORDER_000000159 18447.5 ASK 0.0271 FILLED  
  + 2022-09-22 08:00:00 DRY_ORDER_000000160 19392.5 BID 0.0271 FILLED
 - Short position n°81 of 0.0257 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 2.87326 USDT (0.58 %) - 10 Day line exit  
  + 2022-09-22 08:00:00 DRY_ORDER_000000161 19392.5 ASK 0.0257 FILLED  
  + 2022-09-23 08:00:00 DRY_ORDER_000000162 19280.7 BID 0.0257 FILLED
 - Short position n°82 of 0.0264 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 2.93832 USDT (0.59 %) - 10 Day line exit  
  + 2022-09-24 08:00:00 DRY_ORDER_000000163 18911 ASK 0.0264 FILLED  
  + 2022-09-25 08:00:00 DRY_ORDER_000000164 18799.7 BID 0.0264 FILLED
 - Long position n°83 of 0.026 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -3.8012 USDT (-0.76 %) - 10 Day line exit  
  + 2022-09-26 08:00:00 DRY_ORDER_000000165 19216.3 BID 0.026 FILLED  
  + 2022-09-27 08:00:00 DRY_ORDER_000000166 19070.1 ASK 0.026 FILLED
 - Short position n°84 of 0.0262 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -8.83464 USDT (-1.74 %) - Reverse position  
  + 2022-09-27 08:00:00 DRY_ORDER_000000167 19070.1 ASK 0.0262 FILLED  
  + 2022-09-28 08:00:00 DRY_ORDER_000000168 19407.3 BID 0.0262 FILLED
 - Long position n°85 of 0.0257 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -9.19803 USDT (-1.84 %) - 10 Day line exit  
  + 2022-09-28 08:00:00 DRY_ORDER_000000169 19407.3 BID 0.0257 FILLED  
  + 2022-10-02 08:00:00 DRY_ORDER_000000170 19049.4 ASK 0.0257 FILLED
 - Short position n°86 of 0.0262 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -15.00212 USDT (-2.92 %) - Reverse position  
  + 2022-10-02 08:00:00 DRY_ORDER_000000171 19049.4 ASK 0.0262 FILLED  
  + 2022-10-03 08:00:00 DRY_ORDER_000000172 19622 BID 0.0262 FILLED
 - Long position n°87 of 0.0254 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: -2.56794 USDT (-0.52 %) - 10 Day line exit  
  + 2022-10-03 08:00:00 DRY_ORDER_000000173 19622 BID 0.0254 FILLED  
  + 2022-10-07 08:00:00 DRY_ORDER_000000174 19520.9 ASK 0.0254 FILLED
 - Short position n°88 of 0.0256 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 2.93632 USDT (0.59 %) - 10 Day line exit  
  + 2022-10-07 08:00:00 DRY_ORDER_000000175 19520.9 ASK 0.0256 FILLED  
  + 2022-10-08 08:00:00 DRY_ORDER_000000176 19406.2 BID 0.0256 FILLED
 - Short position n°89 of 0.0257 BTC (rules: 10.0 % gain / 5.0 % loss) - Closed - Gains: 7.8642 USDT (1.6 %) - 10 Day line exit  
  + 2022-10-09 08:00:00 DRY_ORDER_000000177 19430 ASK 0.0257 FILLED  
  + 2022-10-10 08:00:00 DRY_ORDER_000000178 19124 BID 0.0257 FILLED
Position not closed:
 - Long position n°96 of 0.0255 BTC (rules: 10.0 % gain / 5.0 % loss) - Opened - Last gain calculated 5.39 %  
  + 2022-10-23 08:00:00 DRY_ORDER_000000191 19560.7 BID 0.0255 FILLED  
  + 20615.6 USDT
Cumulated probability:
  Positive: 46 trades, pct: 47.92%
  Negative: 50 trades, pct: 52.08%
Side probability:
  Long: 25 trades, pct: 26.04%
  Short: 71 trades, pct: 73.96%
```
#### webui界面
![输入图片说明](https://oscimg.oschina.net/oscnet/up-deb4a72eabab80665c02387df2ac516b6a4.jpg "登录")

![输入图片说明](https://oscimg.oschina.net/oscnet/up-bb3a62ad2c4b119fa4cc62fb6fbb71712e6.jpg "首页")

![输入图片说明](https://oscimg.oschina.net/oscnet/up-46b245e2e40352157ec12961cda334dda55.jpg "详情")

### 感谢
* [cassandre-trading-bot](https://github.com/cassandre-tech/cassandre-trading-bot)
* [ta4j](https://github.com/ta4j/ta4j)
