package com.lonebytesoft.hamster.accounting.service;

import java.util.HashMap;
import java.util.Map;

public enum EntityAction {

    MOVE_UP("moveup"),
    MOVE_DOWN("movedown"),
    ;

    private static final Map<String, EntityAction> values;

    private final String paramValue;

    EntityAction(final String paramValue) {
        this.paramValue = paramValue;
    }

    public String getParamValue() {
        return paramValue;
    }

    public static EntityAction parse(final String value) {
        return values.get(value);
    }

    static {
        values = new HashMap<>();
        for(final EntityAction entityAction : values()) {
            values.put(entityAction.getParamValue(), entityAction);
        }
    }

}
