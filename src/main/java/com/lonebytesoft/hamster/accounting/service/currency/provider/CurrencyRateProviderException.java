package com.lonebytesoft.hamster.accounting.service.currency.provider;

public class CurrencyRateProviderException extends RuntimeException {

    public CurrencyRateProviderException(String message) {
        super(message);
    }

    public CurrencyRateProviderException(String message, Throwable cause) {
        super(message, cause);
    }

}
