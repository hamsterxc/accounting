package com.lonebytesoft.hamster.accounting.dao;

import com.lonebytesoft.hamster.accounting.model.Currency;

import java.util.Map;

public interface CurrencyDao {

    Map<Long, Currency> getAll();

    void save(Currency currency);

}
