package com.lonebytesoft.hamster.accounting.controller.exception;

public class TransactionNotFoundException extends EntityNotFoundException {

    public TransactionNotFoundException(final long id) {
        super("transaction", "id = " + id);
    }

}
