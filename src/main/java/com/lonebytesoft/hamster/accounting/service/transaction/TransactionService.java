package com.lonebytesoft.hamster.accounting.service.transaction;

import com.lonebytesoft.hamster.accounting.model.Currency;
import com.lonebytesoft.hamster.accounting.model.Operation;
import com.lonebytesoft.hamster.accounting.model.Transaction;
import com.lonebytesoft.hamster.accounting.service.EntityAction;

public interface TransactionService {

    void performTimeAction(Transaction transaction, EntityAction action);

    Transaction addLastInDay(Transaction transaction);

    double calculateTotal(Transaction transaction);

    double calculateTotal(Operation operation, Currency currency);

}
