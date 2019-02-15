package com.lonebytesoft.hamster.accounting.controller.exception;

public class UnsupportedActionException extends BadRequestException {

    public UnsupportedActionException(final String action) {
        super("Unsupported action: " + action);
    }
}
