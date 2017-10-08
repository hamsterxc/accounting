package com.lonebytesoft.hamster.accounting.service.transaction;

import java.util.HashMap;
import java.util.Map;

public enum TransactionAction {

    MOVE_EARLIER("moveup"),
    MOVE_LATER("movedown"),
    ADD("add"),
    DELETE("delete")
    ;

    private static final Map<String, TransactionAction> values;

    private final String paramValue;

    TransactionAction(final String paramValue) {
        this.paramValue = paramValue;
    }

    public String getParamValue() {
        return paramValue;
    }

    public static TransactionAction parse(final String value) {
        return values.get(value);
    }

    static {
        values = new HashMap<>();
        for(final TransactionAction transactionAction : values()) {
            values.put(transactionAction.getParamValue(), transactionAction);
        }
    }

}
