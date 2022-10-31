package org.knowm.xchange.service.trade.params;

import org.knowm.xchange.currency.CurrencyPair;

public class DefaultCancelOrderByClientOrderIdParams implements CancelOrderByClientOrderIdParams {

  private final CurrencyPair currencyPair;
  private final String clientOrderId;

  public DefaultCancelOrderByClientOrderIdParams(CurrencyPair currencyPair, String clientOrderId) {
    this.currencyPair = currencyPair;
    this.clientOrderId = clientOrderId;
  }


  @Override
  public CurrencyPair getCurrencyPair() {
    return currencyPair;
  }

  @Override
  public String getClientOrderId() {
    return clientOrderId;
  }
}
