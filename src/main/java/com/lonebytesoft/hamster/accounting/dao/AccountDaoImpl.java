package com.lonebytesoft.hamster.accounting.dao;

import com.lonebytesoft.hamster.accounting.model.Account;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class AccountDaoImpl extends AbstractDao<Account> implements AccountDao {

    private static final Logger logger = LoggerFactory.getLogger(AccountDaoImpl.class);

    public AccountDaoImpl(final SessionFactory sessionFactory) {
        super(sessionFactory, Account.class);
    }

    @Override
    public Map<Long, Account> getAll() {
        return query((criteriaBuilder, criteriaQuery, root) -> {
            criteriaQuery
                    .orderBy(criteriaBuilder.asc(root.get("id")));
        }, query -> {
            final List<Account> accounts = query.list();
            logger.debug("Fetched {} accounts", accounts.size());
            return obtainMapById(accounts);
        });
    }

    @Override
    public void save(Account account) {
        logger.debug("Saving {}", account);
        saveInTransaction(account);
    }

}
