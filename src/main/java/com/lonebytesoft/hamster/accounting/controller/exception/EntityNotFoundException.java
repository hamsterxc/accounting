package com.lonebytesoft.hamster.accounting.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class EntityNotFoundException extends ResponseStatusException {

    public EntityNotFoundException(final String entityName, final String filter) {
        super(HttpStatus.NOT_FOUND, "Could not find " + entityName + ": " + filter);
    }

}
