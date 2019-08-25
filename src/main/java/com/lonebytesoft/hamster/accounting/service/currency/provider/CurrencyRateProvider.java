package com.lonebytesoft.hamster.accounting.service.currency.provider;

import com.lonebytesoft.hamster.accounting.model.Currency;

import java.util.Collection;

public interface CurrencyRateProvider {

    Collection<Currency> getCurrencies(boolean useCache);

}
