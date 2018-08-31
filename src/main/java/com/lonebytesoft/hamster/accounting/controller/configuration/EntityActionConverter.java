package com.lonebytesoft.hamster.accounting.controller.configuration;

import com.lonebytesoft.hamster.accounting.service.EntityAction;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class EntityActionConverter implements Converter<String, EntityAction> {

    @Override
    public EntityAction convert(String source) {
        return EntityAction.parse(source);
    }

}
