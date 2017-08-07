package com.lonebytesoft.hamster.accounting.service;

import com.lonebytesoft.hamster.accounting.model.Transaction;

import java.util.Map;

public interface TransactionService {

    void performTimeAction(Transaction transaction, TransactionAction action);

    long addAtExactTime(long time, long categoryId, String comment, Map<Long, Double> operations);
    long add(long time, long categoryId, String comment, Map<Long, Double> operations);

    void remove(Transaction transaction);

}
