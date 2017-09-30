package com.lonebytesoft.hamster.accounting.service;

import com.lonebytesoft.hamster.accounting.model.Category;
import com.lonebytesoft.hamster.accounting.model.Transaction;

import java.util.Map;

public interface TransactionService {

    void performTimeAction(Transaction transaction, TransactionAction action);

    Transaction addAtExactTime(long time, Category category, String comment, boolean visible, Map<Long, Double> operations);
    Transaction add(long time, Category category, String comment, boolean visible, Map<Long, Double> operations);

    void remove(Transaction transaction);

}
