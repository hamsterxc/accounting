package com.lonebytesoft.hamster.accounting.controller.configuration;

import com.lonebytesoft.hamster.accounting.controller.exception.UnsupportedActionException;
import com.lonebytesoft.hamster.accounting.service.EntityAction;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class EntityActionConverter implements Converter<String, EntityAction> {

    private final Map<String, EntityAction> values;

    public EntityActionConverter() {
        this.values = Stream.of(EntityAction.values())
                .collect(Collectors.toMap(
                        entityAction -> entityAction.getParamValue().toLowerCase(),
                        Function.identity()
                ));
    }

    @Override
    public EntityAction convert(String source) {
        if (source == null) {
            throw new UnsupportedActionException(source);
        }
        return Optional.ofNullable(values.get(source.toLowerCase()))
                .orElseThrow(() -> new UnsupportedActionException(source));
    }

}
