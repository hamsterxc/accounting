package com.lonebytesoft.hamster.accounting.dao;

import com.lonebytesoft.hamster.accounting.model.Category;

import java.util.Map;

public interface CategoryDao {

    Map<Long, Category> getAll();

    void save(Category category);

}
