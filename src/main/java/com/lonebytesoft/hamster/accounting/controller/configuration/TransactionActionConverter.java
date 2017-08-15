package com.lonebytesoft.hamster.accounting.controller.configuration;

import com.lonebytesoft.hamster.accounting.service.TransactionAction;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class TransactionActionConverter implements Converter<String, TransactionAction> {

    @Override
    public TransactionAction convert(String source) {
        return TransactionAction.parse(source);
    }

}
