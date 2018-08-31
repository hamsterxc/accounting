package com.lonebytesoft.hamster.accounting.repository;

import com.lonebytesoft.hamster.accounting.model.Account;
import com.lonebytesoft.hamster.accounting.model.Currency;
import com.lonebytesoft.hamster.accounting.model.Operation;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

public interface OperationRepository extends CrudRepository<Operation, Long> {

    Collection<Operation> findByCurrency(Currency currency);

    Collection<Operation> findByAccount(Account account);

}
