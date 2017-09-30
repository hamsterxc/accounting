package com.lonebytesoft.hamster.accounting.service;

import com.lonebytesoft.hamster.accounting.model.Config;

import java.util.function.BiConsumer;
import java.util.function.Function;

class ConfigEntryMapper {

    private final BiConsumer<Config, String> read;
    private final Function<Config, String> write;

    public ConfigEntryMapper(final BiConsumer<Config, String> read, final Function<Config, String> write) {
        this.read = read;
        this.write = write;
    }

    public BiConsumer<Config, String> getRead() {
        return read;
    }

    public Function<Config, String> getWrite() {
        return write;
    }

}
