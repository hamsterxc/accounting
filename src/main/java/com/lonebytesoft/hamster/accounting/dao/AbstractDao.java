package com.lonebytesoft.hamster.accounting.dao;

import com.lonebytesoft.hamster.accounting.model.HasId;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

abstract class AbstractDao<T> {

    private final SessionFactory sessionFactory;
    private final Class<T> clazz;

    protected AbstractDao(final SessionFactory sessionFactory, final Class<T> clazz) {
        this.sessionFactory = sessionFactory;
        this.clazz = clazz;
    }

    protected <R> R query(final TriConsumer<CriteriaBuilder, CriteriaQuery<T>, Root<T>> criteriaQueryProcessor,
                          final Function<Query<T>, R> resultGetter) {
        try(final Session session = sessionFactory.openSession()) {
            final CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            final CriteriaQuery<T> criteria = criteriaBuilder.createQuery(clazz);
            final Root<T> criteriaRoot = criteria.from(clazz);

            criteriaQueryProcessor.accept(criteriaBuilder, criteria, criteriaRoot);

            final Query<T> query = session.createQuery(criteria);
            return resultGetter.apply(query);
        }
    }

    protected Optional<T> obtainSingleResult(final Query<T> query) {
        try {
            final T result = query.getSingleResult();
            return Optional.of(result);
        } catch (NoResultException e) {
            return Optional.empty();
//        } catch (NonUniqueResultException e) {
//            throw new IllegalStateException("Non-unique result obtained", e);
        }
    }

    protected <K, V extends HasId<K>> Map<K, V> obtainMapById(final Collection<V> items) {
        return items
                .stream()
                .collect(Collectors.toMap(
                        HasId::getId,
                        Function.identity(),
                        (u, v) -> {
                            throw new IllegalStateException("Duplicate id: " + u);
                        },
                        LinkedHashMap::new));
    }

    protected void saveInTransaction(final T object) {
        executeInTransaction(session -> session.saveOrUpdate(object));
    }

    protected void executeInTransaction(final Consumer<Session> consumer) {
        try(final Session session = sessionFactory.openSession()) {
            final Transaction transaction = session.beginTransaction();
            consumer.accept(session);
            transaction.commit();
        }
    }

}
