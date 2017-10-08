package com.lonebytesoft.hamster.accounting.service;

import com.lonebytesoft.hamster.accounting.model.Account;
import com.lonebytesoft.hamster.accounting.model.Category;
import com.lonebytesoft.hamster.accounting.model.Currency;
import com.lonebytesoft.hamster.accounting.model.Operation;
import com.lonebytesoft.hamster.accounting.model.Transaction;
import com.lonebytesoft.hamster.accounting.repository.AccountRepository;
import com.lonebytesoft.hamster.accounting.repository.CategoryRepository;
import com.lonebytesoft.hamster.accounting.repository.CurrencyRepository;
import com.lonebytesoft.hamster.accounting.repository.TransactionRepository;
import com.lonebytesoft.hamster.accounting.service.transaction.TransactionAction;
import com.lonebytesoft.hamster.accounting.service.transaction.TransactionService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/spring-test.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TransactionServiceImplTest {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImplTest.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionService transactionService;

    private int unique = 0;
    private List<Long> ids = new ArrayList<>();
    private Map<Long, Long> times = new HashMap<>();
    private Map<Long, Category> categories = new HashMap<>();
    private Map<Long, String> comments = new HashMap<>();
    private Map<Long, Collection<Operation>> operations = new HashMap<>();

    @Before
    public void before() {
        long time = getBaseTime(); // June 15th, 10:00 AM
        createTransaction(time, "first", 2);

        time = addTime(time, 0, 8); // June 15th, 6:00 PM
        createTransaction(time, "second second", 3);

        time = addTime(time, 1, -2); // June 16th, 4:00 PM
        createTransaction(time, "third", 1);

        time = addTime(time, 2, 4); // June 18th, 8:00 PM
        createTransaction(time, "fourth fourth fourth", 2);
    }

    // June 15th, 10:00 AM
    private long getBaseTime() {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(2000, Calendar.JUNE, 15, 10, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private Category createCategory() {
        final Category category = new Category();
        category.setName(String.valueOf(unique++));
        category.setOrdering(unique++);
        return categoryRepository.save(category);
    }

    private Currency createCurrency() {
        final Currency currency = new Currency();
        currency.setCode(String.valueOf(unique++));
        currency.setName(String.valueOf(unique++));
        currency.setSymbol(String.valueOf(unique++));
        currency.setValue(unique++);
        return currencyRepository.save(currency);
    }

    private Account createAccount() {
        final Account account = new Account();
        account.setName(String.valueOf(unique++));
        account.setCurrency(createCurrency());
        return accountRepository.save(account);
    }

    private Collection<Operation> createOperations(final Transaction transaction, final int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> {
                    final Operation operation = new Operation();
                    operation.setTransaction(transaction);
                    operation.setAccount(createAccount());
                    operation.setAmount((index + 1.0) * 10.0);
                    return operation;
                })
                .collect(Collectors.toList());
    }

    private Transaction constructTransaction(final long time, final String comment, final int operationsCount) {
        final Transaction transaction = new Transaction();
        transaction.setTime(time);
        transaction.setCategory(createCategory());
        transaction.setComment(comment);
        transaction.setVisible(true);
        transaction.setOperations(createOperations(transaction, operationsCount));
        return transaction;
    }

    private void updateTestData(final Transaction transaction) {
        final long id = transaction.getId();
        ids.add(id);
        times.put(id, transaction.getTime());
        categories.put(id, transaction.getCategory());
        comments.put(id, transaction.getComment());
        operations.put(id, transaction.getOperations());
    }

    private void createTransaction(final long time, final String comment, final int operationsCount) {
        final Transaction transaction = constructTransaction(time, comment, operationsCount);
        final Transaction added = transactionRepository.save(transaction);
        updateTestData(added);
    }

    private void createTransactionLastInDay(final long time, final String comment, final int operationsCount) {
        final Transaction transaction = constructTransaction(time, comment, operationsCount);
        final Transaction added = transactionService.addLastInDay(transaction);
        updateTestData(added);
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

        assertTotalCount(4);

        long time = times.get(ids.get(0));
        time = addTime(time, -1, 2); // June 14th, 12:00 PM
        assertTransactionData(0, time);

        assertTransactionData(1, times.get(ids.get(1)));
        assertTransactionData(2, times.get(ids.get(2)));
        assertTransactionData(3, times.get(ids.get(3)));
    }

    @Test
    public void testMoveEarlierSameDayClosest() {
        performTimeAction(ids.get(1), true);

        assertTotalCount(4);

        assertTransactionData(0, times.get(ids.get(1)));
        assertTransactionData(1, times.get(ids.get(0)));

        assertTransactionData(2, times.get(ids.get(2)));
        assertTransactionData(3, times.get(ids.get(3)));
    }

    @Test
    public void testMoveEarlierAdjacentDayClosest() {
        performTimeAction(ids.get(2), true);

        assertTotalCount(4);

        assertTransactionData(0, times.get(ids.get(0)));
        assertTransactionData(1, times.get(ids.get(1)));

        long time = times.get(ids.get(2));
        time = addTime(time, 0, -19); // June 15th, 9 PM
        assertTransactionData(2, time);

        assertTransactionData(3, times.get(ids.get(3)));
    }

    @Test
    public void testMoveEarlierFarClosest() {
        performTimeAction(ids.get(3), true);

        assertTotalCount(4);

        assertTransactionData(0, times.get(ids.get(0)));
        assertTransactionData(1, times.get(ids.get(1)));
        assertTransactionData(2, times.get(ids.get(2)));

        long time = times.get(ids.get(3));
        time = addTime(time, -1, -8); // June 17th, 12 PM
        assertTransactionData(3, time);
    }

    @Test
    public void testMoveLaterNoClosest() {
        performTimeAction(ids.get(3), false);

        assertTotalCount(4);

        assertTransactionData(0, times.get(ids.get(0)));
        assertTransactionData(1, times.get(ids.get(1)));
        assertTransactionData(2, times.get(ids.get(2)));

        long time = times.get(ids.get(3));
        time = addTime(time, 0, 16); // June 19th, 12:00 PM
        assertTransactionData(3, time);
    }

    @Test
    public void testMoveLaterSameDayClosest() {
        performTimeAction(ids.get(0), false);

        assertTotalCount(4);

        assertTransactionData(0, times.get(ids.get(1)));
        assertTransactionData(1, times.get(ids.get(0)));

        assertTransactionData(2, times.get(ids.get(2)));
        assertTransactionData(3, times.get(ids.get(3)));
    }

    @Test
    public void testMoveLaterAdjacentDayClosest() {
        performTimeAction(ids.get(1), false);

        assertTotalCount(4);

        assertTransactionData(0, times.get(ids.get(0)));

        long time = times.get(ids.get(1));
        time = addTime(time, 0, 14); // June 16th, 8 AM
        assertTransactionData(1, time);

        assertTransactionData(2, times.get(ids.get(2)));
        assertTransactionData(3, times.get(ids.get(3)));
    }

    @Test
    public void testMoveLaterFarClosest() {
        performTimeAction(ids.get(2), false);

        assertTotalCount(4);

        assertTransactionData(0, times.get(ids.get(0)));
        assertTransactionData(1, times.get(ids.get(1)));

        long time = times.get(ids.get(2));
        time = addTime(time, 0, 20); // June 17th, 12 PM
        assertTransactionData(2, time);

        assertTransactionData(3, times.get(ids.get(3)));
    }

    @Test
    public void testAddLastInDayLoneNoNext() {
        final long time = addTime(getBaseTime(), 4, 0); // June 19th, 10:00 AM
        final long timeExpected = addTime(time, 0, 2); // June 19th, 12:00 PM
        testAddLastInDay(time, timeExpected);
    }

    @Test
    public void testAddLastInDayLoneNextPresent() {
        final long time = addTime(getBaseTime(), 2, 0); // June 17th, 10:00 AM
        final long timeExpected = addTime(time, 0, 2); // June 17th, 12:00 PM
        testAddLastInDay(time, timeExpected);
    }

    @Test
    public void testAddLastInDayNotAloneNoNext() {
        final long time = addTime(getBaseTime(), 1, 0); // June 16th, 10:00 AM
        final long timeExpected = addTime(time, 0, 10); // June 16th, 8:00 PM
        testAddLastInDay(time, timeExpected);
    }

    @Test
    public void testAddLastInDayNotAloneNextPresent() {
        final long time = getBaseTime(); // June 15th, 10:00 AM
        final long timeExpected = addTime(time, 0, 11); // June 15th, 9:00 PM
        testAddLastInDay(time, timeExpected);
    }

    private void testAddLastInDay(final long time, final long timeExpected) {
        createTransactionLastInDay(time, "last-in-day comment", 1);

        assertTotalCount(5);

        assertTransactionData(0, times.get(ids.get(0)));
        assertTransactionData(1, times.get(ids.get(1)));
        assertTransactionData(2, times.get(ids.get(2)));
        assertTransactionData(3, times.get(ids.get(3)));
        assertTransactionData(4, timeExpected);
    }

    private Transaction getTransaction(final long id) {
        final Transaction transaction = transactionRepository.findOne(id);
        Assert.assertNotNull(transaction);
        return transaction;
    }

    private void performTimeAction(final long id, final boolean moveEarlier) {
        final Transaction transaction = getTransaction(id);
        final TransactionAction action = moveEarlier ? TransactionAction.MOVE_EARLIER : TransactionAction.MOVE_LATER;
        transactionService.performTimeAction(transaction, action);
    }

    private void assertTotalCount(final int expected) {
        int count = 0;
        for(final Transaction transaction : transactionRepository.findAll()) {
            count++;
        }

        Assert.assertEquals(expected, count);
    }

    private void assertTransactionData(final int index, final long time) {
        final long id = ids.get(index);
        final Transaction transaction = getTransaction(id);

        Assert.assertEquals(id, transaction.getId());

        logger.debug("Comparing dates: expected {}, actual {}", new Date(time), new Date(transaction.getTime()));
        Assert.assertEquals(time, transaction.getTime());

        Assert.assertEquals(categories.get(id).getId(), transaction.getCategory().getId());
        Assert.assertEquals(comments.get(id), transaction.getComment());

        final Collection<Operation> operations = transaction.getOperations();
        Assert.assertEquals(this.operations.get(id).size(), operations.size());
        operations.forEach(operation -> Assert.assertEquals(id, operation.getTransaction().getId()));

        final Function<Operation, Long> accountIdExtractor = operation -> operation.getAccount().getId();
        Assert.assertEquals(
                collectOperationValues(this.operations.get(id), accountIdExtractor),
                collectOperationValues(operations, accountIdExtractor)
        );

        final Function<Operation, Double> amountExtractor = Operation::getAmount;
        Assert.assertEquals(
                collectOperationValues(this.operations.get(id), amountExtractor),
                collectOperationValues(operations, amountExtractor)
        );
    }

    private <T> Collection<T> collectOperationValues(final Collection<Operation> operations,
                                                     final Function<Operation, T> converter) {
        return operations
                .stream()
                .map(converter)
                .collect(Collectors.toList());
    }

}
