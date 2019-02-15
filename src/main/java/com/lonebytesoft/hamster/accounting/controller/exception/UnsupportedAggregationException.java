package com.lonebytesoft.hamster.accounting.controller.exception;

public class UnsupportedAggregationException extends BadRequestException {

    public UnsupportedAggregationException(final String aggregation) {
        super("Unsupported aggregation: " + aggregation);
    }
}
