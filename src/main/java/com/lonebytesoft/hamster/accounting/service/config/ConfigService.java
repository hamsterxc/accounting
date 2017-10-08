package com.lonebytesoft.hamster.accounting.service.config;

import com.lonebytesoft.hamster.accounting.model.Config;

public interface ConfigService {

    Config get();

    void save(Config config);

}
