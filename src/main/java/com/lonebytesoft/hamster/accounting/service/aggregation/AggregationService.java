package com.lonebytesoft.hamster.accounting.service.aggregation;

import com.lonebytesoft.hamster.accounting.model.Aggregation;
import com.lonebytesoft.hamster.accounting.model.Transaction;

import java.util.Collection;

public interface AggregationService {

    Aggregation aggregateByAccount(Collection<Transaction> transactions);

    Aggregation aggregateByCategory(Collection<Transaction> transactions);

}
