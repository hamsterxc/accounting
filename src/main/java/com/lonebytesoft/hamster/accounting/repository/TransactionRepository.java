package com.lonebytesoft.hamster.accounting.repository;

import com.lonebytesoft.hamster.accounting.model.Category;
import com.lonebytesoft.hamster.accounting.model.Transaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

public interface TransactionRepository extends CrudRepository<Transaction, Long> {

    @Override
    @Query("SELECT t FROM Transaction t ORDER BY time ASC")
    Iterable<Transaction> findAll();

    Transaction findFirstByTimeBeforeOrderByTimeDesc(long time);
    default Transaction findFirstBefore(long time) {
        return findFirstByTimeBeforeOrderByTimeDesc(time);
    }

    Transaction findFirstByTimeAfterOrderByTimeAsc(long time);
    default Transaction findFirstAfter(long time) {
        return findFirstByTimeAfterOrderByTimeAsc(time);
    }

    @Query("SELECT t FROM Transaction t WHERE time>=?1 AND time <?2 ORDER BY time ASC")
    List<Transaction> findAllBetweenTime(long from, long to);

    Collection<Transaction> findByCategory(Category category);

}
