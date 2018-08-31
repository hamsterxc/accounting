package com.lonebytesoft.hamster.accounting.repository;

import com.lonebytesoft.hamster.accounting.model.Category;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface CategoryRepository extends CrudRepository<Category, Long> {

    @Override
    @Query("SELECT c FROM Category c ORDER BY ordering ASC")
    Iterable<Category> findAll();

    Category findFirstByOrderingGreaterThanOrderByOrderingAsc(long ordering);
    default Category findFirstAfter(long ordering) {
        return findFirstByOrderingGreaterThanOrderByOrderingAsc(ordering);
    }

    Category findFirstByOrderingLessThanOrderByOrderingDesc(long ordering);
    default Category findFirstBefore(long ordering) {
        return findFirstByOrderingLessThanOrderByOrderingDesc(ordering);
    }

}
