package com.lonebytesoft.hamster.accounting.repository;

import com.lonebytesoft.hamster.accounting.model.ConfigEntry;
import org.springframework.data.repository.CrudRepository;

public interface ConfigEntryRepository extends CrudRepository<ConfigEntry, String> {
}
