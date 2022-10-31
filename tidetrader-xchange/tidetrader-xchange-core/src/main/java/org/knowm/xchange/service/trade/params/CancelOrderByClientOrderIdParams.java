package org.knowm.xchange.service.trade.params;

import org.knowm.xchange.currency.CurrencyPair;

public interface CancelOrderByClientOrderIdParams extends CancelOrderParams {
  CurrencyPair getCurrencyPair();
  String getClientOrderId();
}
