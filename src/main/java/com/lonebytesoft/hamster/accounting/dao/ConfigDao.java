package com.lonebytesoft.hamster.accounting.dao;

import com.lonebytesoft.hamster.accounting.model.Config;

public interface ConfigDao {

    Config get();

    void save(Config config);

}
