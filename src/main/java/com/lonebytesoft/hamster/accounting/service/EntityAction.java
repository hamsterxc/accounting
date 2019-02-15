package com.lonebytesoft.hamster.accounting.service;

public enum EntityAction {

    MOVE_UP("moveup"),
    MOVE_DOWN("movedown"),
    ;

    private final String paramValue;

    EntityAction(final String paramValue) {
        this.paramValue = paramValue;
    }

    public String getParamValue() {
        return paramValue;
    }

}
