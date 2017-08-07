package com.lonebytesoft.hamster.accounting.service;

import com.lonebytesoft.hamster.accounting.dao.OperationDao;
import com.lonebytesoft.hamster.accounting.dao.OperationDaoImpl;
import com.lonebytesoft.hamster.accounting.dao.TransactionDao;
import com.lonebytesoft.hamster.accounting.dao.TransactionDaoImpl;
import com.lonebytesoft.hamster.accounting.model.Operation;
import com.lonebytesoft.hamster.accounting.model.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TransactionServiceImplTest {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImplTest.class);

    private static final String HIBERNATE_CONFIG = "hibernate-test.cfg.xml";

    private SessionFactory sessionFactory;
    private TransactionDao transactionDao;
    private OperationDao operationDao;
    private TransactionService transactionService;

    private List<Long> ids = new ArrayList<>();
    private Map<Long, Long> times = new HashMap<>();

    @Before
    public void before() {
        final Configuration configuration = new Configuration();
        configuration.configure(HIBERNATE_CONFIG);
        sessionFactory = configuration.buildSessionFactory();

        transactionDao = new TransactionDaoImpl(sessionFactory);
        operationDao = new OperationDaoImpl(sessionFactory);
        transactionService = new TransactionServiceImpl(transactionDao, operationDao);

        final Calendar calendar = Calendar.getInstance();
        calendar.set(2000, Calendar.JUNE, 15, 10, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long time = calendar.getTimeInMillis(); // June 15th, 10:00 AM
        long id = transactionService.addAtExactTime(time, 1, "first", buildOperationsMap(1, 20, 3, 40));
        ids.add(id);
        times.put(id, time);

        time = addTime(time, 0, 8); // June 15th, 6:00 PM
        id = transactionService.addAtExactTime(time, 2, "second second", buildOperationsMap(3, 41, 5, 60, 7, 80));
        ids.add(id);
        times.put(id, time);

        time = addTime(time, 1, -2); // June 16th, 4:00 PM
        id = transactionService.addAtExactTime(time, 3, "third", buildOperationsMap(7, 81));
        ids.add(id);
        times.put(id, time);

        time = addTime(time, 2, 4); // June 18th, 8:00 PM
        id = transactionService.addAtExactTime(time, 4, "fourth fourth fourth", buildOperationsMap(7, 82, 9, 100));
        ids.add(id);
        times.put(id, time);
    }

    private Map<Long, Double> buildOperationsMap(final double... values) {
        final Map<Long, Double> operationsMap = new HashMap<>();
        for(int i = 0; i < values.length / 2; i++) {
            operationsMap.put((long) values[i * 2], values[i * 2 + 1]);
        }
        return operationsMap;
    }

    private long addTime(final long time, final int days, final int hours) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        calendar.add(Calendar.HOUR_OF_DAY, hours);
        return calendar.getTimeInMillis();
    }

    @Test
    public void testMoveEarlierNoClosest() {
        performTimeAction(ids.get(0), true);

        Assert.assertEquals(4, transactionDao.getAll().size());

        long time = times.get(ids.get(0));
        time = addTime(time, -1, 2); // June 14th, 12:00 PM
        assertTransactionData(ids.get(0), time, 1, "first", 1, 20, 3, 40);

        assertTransactionData(ids.get(1), times.get(ids.get(1)), 2, "second second", 3, 41, 5, 60, 7, 80);
        assertTransactionData(ids.get(2), times.get(ids.get(2)), 3, "third", 7, 81);
        assertTransactionData(ids.get(3), times.get(ids.get(3)), 4, "fourth fourth fourth", 7, 82, 9, 100);
    }

    @Test
    public void testMoveEarlierSameDayClosest() {
        performTimeAction(ids.get(1), true);

        Assert.assertEquals(4, transactionDao.getAll().size());

        assertTransactionData(ids.get(0), times.get(ids.get(1)), 1, "first", 1, 20, 3, 40);
        assertTransactionData(ids.get(1), times.get(ids.get(0)), 2, "second second", 3, 41, 5, 60, 7, 80);

        assertTransactionData(ids.get(2), times.get(ids.get(2)), 3, "third", 7, 81);
        assertTransactionData(ids.get(3), times.get(ids.get(3)), 4, "fourth fourth fourth", 7, 82, 9, 100);
    }

    @Test
    public void testMoveEarlierAdjacentDayClosest() {
        performTimeAction(ids.get(2), true);

        Assert.assertEquals(4, transactionDao.getAll().size());

        assertTransactionData(ids.get(0), times.get(ids.get(0)), 1, "first", 1, 20, 3, 40);
        assertTransactionData(ids.get(1), times.get(ids.get(1)), 2, "second second", 3, 41, 5, 60, 7, 80);

        long time = times.get(ids.get(2));
        time = addTime(time, 0, -19); // June 15th, 9 PM
        assertTransactionData(ids.get(2), time, 3, "third", 7, 81);

        assertTransactionData(ids.get(3), times.get(ids.get(3)), 4, "fourth fourth fourth", 7, 82, 9, 100);
    }

    @Test
    public void testMoveEarlierFarClosest() {
        performTimeAction(ids.get(3), true);

        Assert.assertEquals(4, transactionDao.getAll().size());

        assertTransactionData(ids.get(0), times.get(ids.get(0)), 1, "first", 1, 20, 3, 40);
        assertTransactionData(ids.get(1), times.get(ids.get(1)), 2, "second second", 3, 41, 5, 60, 7, 80);
        assertTransactionData(ids.get(2), times.get(ids.get(2)), 3, "third", 7, 81);

        long time = times.get(ids.get(3));
        time = addTime(time, -1, -8); // June 17th, 12 PM
        assertTransactionData(ids.get(3), time, 4, "fourth fourth fourth", 7, 82, 9, 100);
    }

    @Test
    public void testMoveLaterNoClosest() {
        performTimeAction(ids.get(3), false);

        Assert.assertEquals(4, transactionDao.getAll().size());

        assertTransactionData(ids.get(0), times.get(ids.get(0)), 1, "first", 1, 20, 3, 40);
        assertTransactionData(ids.get(1), times.get(ids.get(1)), 2, "second second", 3, 41, 5, 60, 7, 80);
        assertTransactionData(ids.get(2), times.get(ids.get(2)), 3, "third", 7, 81);

        long time = times.get(ids.get(3));
        time = addTime(time, 0, 16); // June 19th, 12:00 PM
        assertTransactionData(ids.get(3), time, 4, "fourth fourth fourth", 7, 82, 9, 100);
    }

    @Test
    public void testMoveLaterSameDayClosest() {
        performTimeAction(ids.get(0), false);

        Assert.assertEquals(4, transactionDao.getAll().size());

        assertTransactionData(ids.get(0), times.get(ids.get(1)), 1, "first", 1, 20, 3, 40);
        assertTransactionData(ids.get(1), times.get(ids.get(0)), 2, "second second", 3, 41, 5, 60, 7, 80);

        assertTransactionData(ids.get(2), times.get(ids.get(2)), 3, "third", 7, 81);
        assertTransactionData(ids.get(3), times.get(ids.get(3)), 4, "fourth fourth fourth", 7, 82, 9, 100);
    }

    @Test
    public void testMoveLaterAdjacentDayClosest() {
        performTimeAction(ids.get(1), false);

        Assert.assertEquals(4, transactionDao.getAll().size());

        assertTransactionData(ids.get(0), times.get(ids.get(0)), 1, "first", 1, 20, 3, 40);

        long time = times.get(ids.get(1));
        time = addTime(time, 0, 14); // June 16th, 8 AM
        assertTransactionData(ids.get(1), time, 2, "second second", 3, 41, 5, 60, 7, 80);

        assertTransactionData(ids.get(2), times.get(ids.get(2)), 3, "third", 7, 81);
        assertTransactionData(ids.get(3), times.get(ids.get(3)), 4, "fourth fourth fourth", 7, 82, 9, 100);
    }

    @Test
    public void testMoveLaterFarClosest() {
        performTimeAction(ids.get(2), false);

        Assert.assertEquals(4, transactionDao.getAll().size());

        assertTransactionData(ids.get(0), times.get(ids.get(0)), 1, "first", 1, 20, 3, 40);
        assertTransactionData(ids.get(1), times.get(ids.get(1)), 2, "second second", 3, 41, 5, 60, 7, 80);

        long time = times.get(ids.get(2));
        time = addTime(time, 0, 20); // June 17th, 12 PM
        assertTransactionData(ids.get(2), time, 3, "third", 7, 81);

        assertTransactionData(ids.get(3), times.get(ids.get(3)), 4, "fourth fourth fourth", 7, 82, 9, 100);
    }

    private Transaction getTransaction(final long id) {
        final Optional<Transaction> transactionOptional = transactionDao.get(id);
        Assert.assertTrue(transactionOptional.isPresent());
        return transactionOptional.get();
    }

    private void performTimeAction(final long id, final boolean moveEarlier) {
        final Transaction transaction = getTransaction(id);
        final TransactionAction action = moveEarlier ? TransactionAction.MOVE_EARLIER : TransactionAction.MOVE_LATER;
        transactionService.performTimeAction(transaction, action);
    }

    private void assertTransactionData(final long id, final long time, final long categoryId, final String comment,
                                       final double... operationValues) {
        final Transaction transaction = getTransaction(id);

        Assert.assertEquals(id, (long) transaction.getId());

        logger.debug("Comparing dates: expected {}, actual {}", new Date(time), new Date(transaction.getTime()));
        Assert.assertEquals(time, transaction.getTime());

        Assert.assertEquals(categoryId, transaction.getCategoryId());
        Assert.assertEquals(comment, transaction.getComment());

        final Collection<Operation> operations = operationDao.get(id);
        operations.forEach(operation -> Assert.assertEquals(id, operation.getTransactionId()));
        final Map<Long, Double> values = operations.stream()
                .collect(Collectors.toMap(
                        Operation::getAccountId,
                        Operation::getAmount
                ));
        for(int i = 0; i < operationValues.length / 2; i++) {
            final long accountId = (long) operationValues[i * 2];
            final double amount = operationValues[i * 2 + 1];

            Assert.assertTrue(values.containsKey(accountId));
            Assert.assertEquals((Double) amount, values.get(accountId));

            values.remove(accountId);
        }
    }

    @After
    public void after() {
        sessionFactory.close();
    }

}
