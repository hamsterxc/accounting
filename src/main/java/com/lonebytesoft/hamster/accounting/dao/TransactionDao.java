package com.lonebytesoft.hamster.accounting.dao;

import com.lonebytesoft.hamster.accounting.model.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionDao {

    List<Transaction> get(long from, long to);
    Optional<Transaction> get(long id);

    List<Transaction> getAll();

    Optional<Transaction> getClosestBefore(Transaction transaction);
    Optional<Transaction> getClosestAfter(Transaction transaction);

    void save(Transaction transaction);

    void remove(Transaction transaction);

}
