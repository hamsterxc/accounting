package com.lonebytesoft.hamster.accounting.model;

public class Config {

    private long currencyIdDefault;

    public long getCurrencyIdDefault() {
        return currencyIdDefault;
    }

    public void setCurrencyIdDefault(long currencyIdDefault) {
        this.currencyIdDefault = currencyIdDefault;
    }

    @Override
    public String toString() {
        return "Config{" +
                "currencyIdDefault=" + currencyIdDefault +
                '}';
    }

}
