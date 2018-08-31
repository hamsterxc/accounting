package com.lonebytesoft.hamster.accounting.repository;

import com.lonebytesoft.hamster.accounting.model.Account;
import com.lonebytesoft.hamster.accounting.model.Currency;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

public interface AccountRepository extends CrudRepository<Account, Long> {

    @Override
    @Query("SELECT a FROM Account a ORDER BY ordering ASC")
    Iterable<Account> findAll();

    Collection<Account> findByCurrency(Currency currency);

    Account findFirstByOrderingGreaterThanOrderByOrderingAsc(long ordering);
    default Account findFirstAfter(long ordering) {
        return findFirstByOrderingGreaterThanOrderByOrderingAsc(ordering);
    }

    Account findFirstByOrderingLessThanOrderByOrderingDesc(long ordering);
    default Account findFirstBefore(long ordering) {
        return findFirstByOrderingLessThanOrderByOrderingDesc(ordering);
    }

}
