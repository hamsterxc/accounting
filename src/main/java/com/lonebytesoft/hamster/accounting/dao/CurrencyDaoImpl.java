package com.lonebytesoft.hamster.accounting.dao;

import com.lonebytesoft.hamster.accounting.model.Currency;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class CurrencyDaoImpl extends AbstractDao<Currency> implements CurrencyDao {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyDaoImpl.class);

    public CurrencyDaoImpl(final SessionFactory sessionFactory) {
        super(sessionFactory, Currency.class);
    }

    @Override
    public Map<Long, Currency> getAll() {
        return query((criteriaBuilder, criteriaQuery, root) -> {
            criteriaQuery
                    .orderBy(criteriaBuilder.asc(root.get("id")));
        }, query -> {
            final List<Currency> currencies = query.list();
            logger.debug("Fetched {} currencies", currencies.size());
            return obtainMapById(currencies);
        });
    }

    @Override
    public void save(Currency currency) {
        logger.debug("Saving {}", currency);
        saveInTransaction(currency);
    }

}
