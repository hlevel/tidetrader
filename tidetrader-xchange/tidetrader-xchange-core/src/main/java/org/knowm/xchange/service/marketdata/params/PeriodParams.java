package org.knowm.xchange.service.marketdata.params;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.instrument.Instrument;

import java.util.Collection;

public interface PeriodParams extends Params {

  CurrencyPair getCurrencyPair();

  long millis();

  Long startTime();

  Long endTime();

  int durationMaximum();

}
