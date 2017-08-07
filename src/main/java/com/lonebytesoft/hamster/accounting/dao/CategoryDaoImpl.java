package com.lonebytesoft.hamster.accounting.dao;

import com.lonebytesoft.hamster.accounting.model.Category;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class CategoryDaoImpl extends AbstractDao<Category> implements CategoryDao {

    private static final Logger logger = LoggerFactory.getLogger(CategoryDaoImpl.class);

    public CategoryDaoImpl(final SessionFactory sessionFactory) {
        super(sessionFactory, Category.class);
    }

    @Override
    public Map<Long, Category> getAll() {
        return query((criteriaBuilder, criteriaQuery, root) -> {
            criteriaQuery
                    .orderBy(criteriaBuilder.asc(root.get("ordering")));
        }, query -> {
            final List<Category> categories = query.list();
            logger.debug("Fetched {} categories", categories.size());
            return obtainMapById(categories);
        });
    }

    @Override
    public void save(Category category) {
        logger.debug("Saving {}", category);
        saveInTransaction(category);
    }

}
