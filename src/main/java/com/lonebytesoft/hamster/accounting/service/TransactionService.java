package com.lonebytesoft.hamster.accounting.service;

import com.lonebytesoft.hamster.accounting.model.Transaction;

public interface TransactionService {

    void performTimeAction(Transaction transaction, TransactionAction action);

    Transaction addLastInDay(Transaction transaction);

}
