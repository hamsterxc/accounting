package com.lonebytesoft.hamster.accounting.dao;

import com.lonebytesoft.hamster.accounting.model.Operation;

import java.util.Collection;

public interface OperationDao {

    Collection<Operation> get(long transactionId);

    void save(Operation operation);

    void remove(Operation operation);

}
