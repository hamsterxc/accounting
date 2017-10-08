package com.lonebytesoft.hamster.accounting.service.currency;

public interface CurrencyRateProvider {

    /**
     * Returns the value of subject currency in terms of base currency
     * @param isoCodeSubject the variable currency ISO code
     * @param isoCodeBase the fixed currency ISO code
     * @return the exchange rate, i.e. subject value divided by base value; null if unknown
     */
    Double getCurrencyRate(String isoCodeSubject, String isoCodeBase);

}
