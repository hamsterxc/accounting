package com.lonebytesoft.hamster.accounting;

import com.lonebytesoft.hamster.accounting.dao.AccountDao;
import com.lonebytesoft.hamster.accounting.dao.CategoryDao;
import com.lonebytesoft.hamster.accounting.dao.ConfigDao;
import com.lonebytesoft.hamster.accounting.dao.CurrencyDao;
import com.lonebytesoft.hamster.accounting.dao.OperationDao;
import com.lonebytesoft.hamster.accounting.dao.TransactionDao;
import com.lonebytesoft.hamster.accounting.model.Account;
import com.lonebytesoft.hamster.accounting.model.Category;
import com.lonebytesoft.hamster.accounting.model.Config;
import com.lonebytesoft.hamster.accounting.model.Currency;
import com.lonebytesoft.hamster.accounting.model.Operation;
import com.lonebytesoft.hamster.accounting.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TestDataPopulator {

    private static final Logger logger = LoggerFactory.getLogger(TestDataPopulator.class);

    private static final char[] ALPHABET_UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final char[] ALPHABET_LOWERCASE = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    private final AccountDao accountDao;
    private final CategoryDao categoryDao;
    private final CurrencyDao currencyDao;
    private final OperationDao operationDao;
    private final TransactionDao transactionDao;
    private final ConfigDao configDao;

    private final int categoryCount;
    private final int currencyCount;
    private final int accountCount;
    private final int transactionCount;

    public TestDataPopulator(final AccountDao accountDao, final CategoryDao categoryDao, final CurrencyDao currencyDao,
                             final OperationDao operationDao, final TransactionDao transactionDao, final ConfigDao configDao,
                             final int categoryCount, final int currencyCount, final int accountCount, final int transactionCount) {
        this.accountDao = accountDao;
        this.categoryDao = categoryDao;
        this.currencyDao = currencyDao;
        this.operationDao = operationDao;
        this.transactionDao = transactionDao;
        this.configDao = configDao;

        this.categoryCount = categoryCount;
        this.currencyCount = currencyCount;
        this.accountCount = accountCount;
        this.transactionCount = transactionCount;
    }

    public void populate() {
        logger.info("Populating test data");

        final List<Long> categoryIds = populateCategory(categoryCount);
        final List<Long> currencyIds = populateCurrency(currencyCount);
        final List<Long> accountIds = populateAccount(accountCount, currencyIds);

        populateConfig(currencyIds);

        final long to = System.currentTimeMillis();

        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(to);
        calendar.add(Calendar.YEAR, -1);
        final long from = calendar.getTimeInMillis();

        populateTransactionOperation(transactionCount, categoryIds, accountIds, from, to);
    }

    private void populateConfig(final List<Long> currencyIds) {
        final Config config = new Config();

        config.setCurrencyIdDefault(currencyIds.get(0));

        configDao.save(config);
    }

    private List<Long> populateCategory(final int count) {
        final List<Long> ids = new ArrayList<>(count);
        int ordering = 0;
        for(int i = 0; i < count; i++) {
            final Category category = new Category();

            category.setName(generateRandomWords(1, 3,4, 11));

            ordering += random(0, 3);
            category.setOrdering(ordering);

            categoryDao.save(category);
            ids.add(category.getId());
        }
        return ids;
    }

    private List<Long> populateCurrency(final int count) {
        final List<Long> ids = new ArrayList<>(count);
        for(int i = 0; i < count; i++) {
            final Currency currency = new Currency();

            currency.setCode(generateRandomString(ALPHABET_UPPERCASE, 3));
            currency.setName(generateRandomWord(4, 11));
            currency.setSymbol(currency.getCode().substring(0, 2));
            currency.setValue(random(1e-3, 1e4));

            currencyDao.save(currency);
            ids.add(currency.getId());
        }
        return ids;
    }

    private List<Long> populateAccount(final int count, final List<Long> currencyIds) {
        final List<Long> ids = new ArrayList<>(count);
        for(int i = 0; i < count; i++) {
            final Account account = new Account();

            account.setName(generateRandomWords(1, 3, 4, 11));
            account.setCurrencyId(currencyIds.get(random(0, currencyIds.size())));

            accountDao.save(account);
            ids.add(account.getId());
        }
        return ids;
    }

    private void populateTransactionOperation(final int count, final List<Long> categoryIds, final List<Long> accountIds,
                                              final long timeFrom, final long timeTo) {
        for(int i = 0; i < count; i++) {
            final Transaction transaction = new Transaction();

            transaction.setTime(random(timeFrom, timeTo));
            transaction.setCategoryId(categoryIds.get(random(0, categoryIds.size())));
            transaction.setComment(generateRandomWords(1, 6, 4, 16));

            transactionDao.save(transaction);

            final int countOperations = random(1, accountIds.size() + 1);
            final List<Long> freeAccountIds = new ArrayList<>(accountIds);
            for(int j = 0; j < countOperations; j++) {
                final Operation operation = new Operation();

                operation.setTransactionId(transaction.getId());
                operation.setAccountId(freeAccountIds.remove(random(0, freeAccountIds.size())));
                operation.setAmount(random(-1e5, 1e5));

                operationDao.save(operation);
            }
        }
    }

    private String generateRandomString(final char[] alphabet, final int length) {
        final StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < length; i++) {
            stringBuilder.append(alphabet[random(0, alphabet.length)]);
        }
        return stringBuilder.toString();
    }

    private String generateRandomWord(final int minLength, final int maxLength) {
        return generateRandomString(ALPHABET_UPPERCASE, 1)
                + generateRandomString(ALPHABET_LOWERCASE, random(minLength - 1, maxLength - 1));
    }

    private String generateRandomWords(final int minCount, final int maxCount, final int minLength, final int maxLength) {
        final StringBuilder stringBuilder = new StringBuilder();

        final int count = random(minCount, maxCount);
        for(int i = 0; i < count; i++) {
            if(i == 0) {
                stringBuilder.append(generateRandomWord(minLength, maxLength));
            } else {
                stringBuilder
                        .append(' ')
                        .append(generateRandomString(ALPHABET_LOWERCASE, random(minLength, maxLength)));
            }
        }

        return stringBuilder.toString();
    }

    private int random(final int min, final int max) {
        return min + (int) Math.floor(Math.random() * (max - min));
    }

    private long random(final long min, final long max) {
        return min + (long) Math.floor(Math.random() * (max - min));
    }

    private double random(final double min, final double max) {
        return min + Math.random() * (max - min);
    }

}
