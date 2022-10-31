package org.knowm.xchange.binance.dto.trade;


public class BinancePerpetualLeverage {

    /**
     * "leverage": 21, // 杠杆倍数
     * */
    private Integer leverage;

    /**
     * "maxNotionalValue": "1000000", // 当前杠杆倍数下允许的最大名义价值
     * */
    private Integer maxNotionalValue;

    /**
     * "symbol": "BTCUSDT" // 交易对
     * */
    private String symbol;




}
