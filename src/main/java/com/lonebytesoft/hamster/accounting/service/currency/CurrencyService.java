package com.lonebytesoft.hamster.accounting.service.currency;

import com.lonebytesoft.hamster.accounting.model.Currency;

public interface CurrencyService {

    double getActualCurrencyValue(Currency currency);

    void updateCurrencyValues();

    void updateCurrencyRates();

    double convert(Currency from, Currency to, double amount);

}
