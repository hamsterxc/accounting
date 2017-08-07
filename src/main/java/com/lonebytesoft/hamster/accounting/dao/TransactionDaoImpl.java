package com.lonebytesoft.hamster.accounting.dao;

import com.lonebytesoft.hamster.accounting.model.Transaction;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class TransactionDaoImpl extends AbstractDao<Transaction> implements TransactionDao {

    private static final Logger logger = LoggerFactory.getLogger(TransactionDaoImpl.class);

    public TransactionDaoImpl(final SessionFactory sessionFactory) {
        super(sessionFactory, Transaction.class);
    }

    @Override
    public List<Transaction> get(long from, long to) {
        return query((criteriaBuilder, criteriaQuery, root) -> {
            criteriaQuery
                    .where(criteriaBuilder.and(
                            criteriaBuilder.greaterThanOrEqualTo(root.get("time"), from),
                            criteriaBuilder.lessThan(root.get("time"), to)
                    ))
                    .orderBy(criteriaBuilder.asc(root.get("time")));
        }, query -> {
            final List<Transaction> transactions = query.list();
            logger.debug("Fetched {} transactions between {} and {}", transactions.size(), from, to);
            return transactions;
        });
    }

    @Override
    public Optional<Transaction> get(long id) {
        return query((criteriaBuilder, criteriaQuery, root) -> {
            criteriaQuery
                    .where(criteriaBuilder.equal(root.get("id"), id));
        }, query -> {
            final Optional<Transaction> result = obtainSingleResult(query);
            if(result.isPresent()) {
                logger.debug("Fetched transaction {} by id", result.get());
            } else {
                logger.debug("No transaction id {}", id);
            }
            return result;
        });
    }

    @Override
    public List<Transaction> getAll() {
        return query((criteriaBuilder, criteriaQuery, root) -> {
            criteriaQuery
                    .orderBy(criteriaBuilder.asc(root.get("time")));
        }, query -> {
            final List<Transaction> transactions = query.list();
            logger.debug("Fetched {} transactions", transactions.size());
            return transactions;
        });
    }

    @Override
    public Optional<Transaction> getClosestBefore(Transaction transaction) {
        return query((criteriaBuilder, criteriaQuery, root) -> {
            criteriaQuery
                    .where(criteriaBuilder.lessThan(root.get("time"), transaction.getTime()))
                    .orderBy(criteriaBuilder.desc(root.get("time")));
        }, query -> {
            query.setMaxResults(1);
            final Optional<Transaction> result = obtainSingleResult(query);
            if(result.isPresent()) {
                logger.debug("Fetched transaction {}, closest before {}", result.get(), transaction);
            } else {
                logger.debug("No transactions before {}", transaction);
            }
            return result;
        });
    }

    @Override
    public Optional<Transaction> getClosestAfter(Transaction transaction) {
        return query((criteriaBuilder, criteriaQuery, root) -> {
            criteriaQuery
                    .where(criteriaBuilder.greaterThan(root.get("time"), transaction.getTime()))
                    .orderBy(criteriaBuilder.asc(root.get("time")));
        }, query -> {
            query.setMaxResults(1);
            final Optional<Transaction> result = obtainSingleResult(query);
            if(result.isPresent()) {
                logger.debug("Fetched transaction {}, closest after {}", result.get(), transaction);
            } else {
                logger.debug("No transactions after {}", transaction);
            }
            return result;
        });
    }

    @Override
    public void save(Transaction transaction) {
        logger.debug("Saving {}", transaction);
        saveInTransaction(transaction);
    }

    @Override
    public void remove(Transaction transaction) {
        logger.debug("Deleting {}", transaction);
        executeInTransaction(session -> session.delete(transaction));
    }

}
