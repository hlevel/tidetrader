package org.knowm.xchange.service.marketdata.params;

import org.knowm.xchange.currency.CurrencyPair;

public class DefaultCancelOrderByClientOrderIdParams implements PeriodParams{

    private CurrencyPair currencyPair;
    private long millis;
    private Long startTime;
    private Long endTime;
    private int durationMaximum;

    public DefaultCancelOrderByClientOrderIdParams(CurrencyPair currencyPair, long millis) {
        this(currencyPair, millis, null, null, 100);
    }

    public DefaultCancelOrderByClientOrderIdParams(CurrencyPair currencyPair, long millis, int durationMaximum) {
        this(currencyPair, millis, null, null, durationMaximum);
    }

    public DefaultCancelOrderByClientOrderIdParams(CurrencyPair currencyPair, long millis, Long startTime, Long endTime) {
        this(currencyPair, millis, startTime, endTime, 1000);
    }

    public DefaultCancelOrderByClientOrderIdParams(CurrencyPair currencyPair, long millis, Long startTime, Long endTime, int durationMaximum) {
        this.currencyPair = currencyPair;
        this.millis = millis;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMaximum = durationMaximum;
    }

    @Override
    public CurrencyPair getCurrencyPair() {
        return currencyPair;
    }

    @Override
    public long millis() {
        return millis;
    }

    @Override
    public Long startTime() {
        return startTime;
    }

    @Override
    public Long endTime() {
        return endTime;
    }

    @Override
    public int durationMaximum() {
        return durationMaximum;
    }
}
