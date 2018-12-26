package com.lonebytesoft.hamster.accounting.repository;

import com.lonebytesoft.hamster.accounting.model.Currency;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CurrencyRepository extends CrudRepository<Currency, Long> {

    Optional<Currency> findByCode(String code);

}
