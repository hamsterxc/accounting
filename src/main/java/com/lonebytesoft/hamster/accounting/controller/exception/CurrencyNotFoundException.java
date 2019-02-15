package com.lonebytesoft.hamster.accounting.controller.exception;

public class CurrencyNotFoundException extends EntityNotFoundException {

    public CurrencyNotFoundException(final long id) {
        super("currency", "id = " + id);
    }

    public CurrencyNotFoundException(final String code) {
        super("currency", "code = " + code);
    }

}
