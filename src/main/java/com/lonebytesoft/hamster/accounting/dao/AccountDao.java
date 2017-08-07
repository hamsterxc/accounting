package com.lonebytesoft.hamster.accounting.dao;

import com.lonebytesoft.hamster.accounting.model.Account;

import java.util.Map;

public interface AccountDao {

    Map<Long, Account> getAll();

    void save(Account account);

}
