package com.lonebytesoft.hamster.accounting.controller.exception;

public class CategoryNotFoundException extends EntityNotFoundException {

    public CategoryNotFoundException(final long id) {
        super("category", "id = " + id);
    }

}
