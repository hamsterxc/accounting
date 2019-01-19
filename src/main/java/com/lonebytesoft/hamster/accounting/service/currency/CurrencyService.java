package com.lonebytesoft.hamster.accounting.service.currency;

import com.lonebytesoft.hamster.accounting.model.Currency;

import java.util.Collection;

public interface CurrencyService {

    void updateCurrencyValues();

    void updateCurrencyRates();

    double convert(Currency from, Currency to, double amount);

    Collection<Currency> getKnownCurrencies();

}
