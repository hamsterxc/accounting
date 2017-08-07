package com.lonebytesoft.hamster.accounting.dao;

import com.lonebytesoft.hamster.accounting.model.Operation;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class OperationDaoImpl extends AbstractDao<Operation> implements OperationDao {

    private static final Logger logger = LoggerFactory.getLogger(OperationDaoImpl.class);

    public OperationDaoImpl(final SessionFactory sessionFactory) {
        super(sessionFactory, Operation.class);
    }

    @Override
    public Collection<Operation> get(long transactionId) {
        return query((criteriaBuilder, criteriaQuery, root) -> {
            criteriaQuery
                    .where(criteriaBuilder.equal(root.get("transactionId"), transactionId));
        }, query -> {
            final Collection<Operation> operations = query.list();
            logger.trace("Fetched {} operations for transaction id={}", operations.size(), transactionId);
            return operations;
        });
    }

    @Override
    public void save(Operation operation) {
        logger.debug("Saving {}", operation);
        saveInTransaction(operation);
    }

    @Override
    public void remove(Operation operation) {
        logger.debug("Deleting {}", operation);
        executeInTransaction(session -> session.delete(operation));
    }

}
