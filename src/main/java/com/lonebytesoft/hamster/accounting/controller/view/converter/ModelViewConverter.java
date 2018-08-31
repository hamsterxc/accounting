package com.lonebytesoft.hamster.accounting.controller.view.converter;

public interface ModelViewConverter<T, I, O> {

    T populateFromInput(T base, I input);

    O convertToOutput(T model);

}
