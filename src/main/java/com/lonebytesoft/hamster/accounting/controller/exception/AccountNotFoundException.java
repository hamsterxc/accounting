package com.lonebytesoft.hamster.accounting.controller.exception;

public class AccountNotFoundException extends EntityNotFoundException {

    public AccountNotFoundException(final long id) {
        super("account", "id = " + id);
    }

}
