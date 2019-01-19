package com.lonebytesoft.hamster.accounting.service.currency;

import com.lonebytesoft.hamster.accounting.model.Currency;

import java.util.Collection;

public interface CurrencyRateProvider {

    Collection<Currency> getCurrencies(boolean useCache);

}
