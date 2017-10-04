package com.lonebytesoft.hamster.accounting;

import com.lonebytesoft.hamster.accounting.model.Account;
import com.lonebytesoft.hamster.accounting.model.Category;
import com.lonebytesoft.hamster.accounting.model.Config;
import com.lonebytesoft.hamster.accounting.model.Currency;
import com.lonebytesoft.hamster.accounting.model.Operation;
import com.lonebytesoft.hamster.accounting.model.Transaction;
import com.lonebytesoft.hamster.accounting.repository.AccountRepository;
import com.lonebytesoft.hamster.accounting.repository.CategoryRepository;
import com.lonebytesoft.hamster.accounting.repository.CurrencyRepository;
import com.lonebytesoft.hamster.accounting.repository.TransactionRepository;
import com.lonebytesoft.hamster.accounting.service.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@Profile("test")
public class TestDataPopulator {

    private static final Logger logger = LoggerFactory.getLogger(TestDataPopulator.class);

    private static final char[] ALPHABET_UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final char[] ALPHABET_LOWERCASE = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final CurrencyRepository currencyRepository;
    private final TransactionRepository transactionRepository;

    private final ConfigService configService;

    private final int categoryCount;
    private final int currencyCount;
    private final int accountCount;
    private final int transactionCount;

    @Autowired
    public TestDataPopulator(final AccountRepository accountRepository, final CategoryRepository categoryRepository,
                             final CurrencyRepository currencyRepository, final TransactionRepository transactionRepository,
                             final ConfigService configService,
                             @Value("${accounting.test.populator.count.category}") final int categoryCount,
                             @Value("${accounting.test.populator.count.currency}") final int currencyCount,
                             @Value("${accounting.test.populator.count.account}") final int accountCount,
                             @Value("${accounting.test.populator.count.transaction}") final int transactionCount) {
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.currencyRepository = currencyRepository;
        this.transactionRepository = transactionRepository;

        this.configService = configService;

        this.categoryCount = categoryCount;
        this.currencyCount = currencyCount;
        this.accountCount = accountCount;
        this.transactionCount = transactionCount;
    }

    @PostConstruct
    public void populate() {
        logger.info("Populating test data");

        final List<Category> categories = populateCategory(categoryCount);
        final List<Currency> currencies = populateCurrency(currencyCount);
        final List<Account> accounts = populateAccount(accountCount, currencies);

        populateConfig(currencies);

        final long to = System.currentTimeMillis();

        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(to);
        calendar.add(Calendar.YEAR, -1);
        final long from = calendar.getTimeInMillis();

        for(int i = 0; i < transactionCount; i++) {
            populateTransactionOperation(categories, accounts, from, to);
        }
    }

    private void populateConfig(final List<Currency> currencies) {
        final Config config = new Config();

        config.setCurrencyDefault(currencies.get(0));

        configService.save(config);
    }

    private List<Category> populateCategory(final int count) {
        final List<Category> categories = new ArrayList<>(count);
        int ordering = 0;
        for(int i = 0; i < count; i++) {
            final Category category = new Category();

            category.setName(generateRandomWords(1, 3, 4, 11));
            category.setVisible(Math.random() > 1.5 / count);

            ordering += random(-1, 2);
            category.setOrdering(ordering);

            categoryRepository.save(category);
            categories.add(category);
        }
        return categories;
    }

    private List<Currency> populateCurrency(final int count) {
        final List<Currency> currencies = new ArrayList<>(count);
        for(int i = 0; i < count; i++) {
            final Currency currency = new Currency();

            currency.setCode(generateRandomString(ALPHABET_UPPERCASE, 3));
            currency.setName(generateRandomWord(4, 11));
            currency.setSymbol(currency.getCode().substring(0, 2));
            currency.setValue(random(1e-3, 1e4));

            currencyRepository.save(currency);
            currencies.add(currency);
        }
        return currencies;
    }

    private List<Account> populateAccount(final int count, final List<Currency> currencies) {
        final List<Account> accounts = new ArrayList<>(count);
        for(int i = 0; i < count; i++) {
            final Account account = new Account();

            account.setName(generateRandomWords(1, 3, 4, 11));
            account.setCurrency(currencies.get(random(0, currencies.size())));
            account.setVisible(Math.random() > 1.5 / count);

            accountRepository.save(account);
            accounts.add(account);
        }
        return accounts;
    }

    private void populateTransactionOperation(final List<Category> categories, final List<Account> accounts,
                                              final long timeFrom, final long timeTo) {
        final Transaction transaction = new Transaction();
        transaction.setTime(random(timeFrom, timeTo));
        transaction.setCategory(categories.get(random(0, categories.size())));
        transaction.setComment(generateRandomWords(1, 6, 4, 16));
        transaction.setVisible(Math.random() > 0.1);

        final List<Account> freeAccounts = new ArrayList<>(accounts);
        transaction.setOperations(IntStream.range(0, random(1, accounts.size() + 1))
                .mapToObj(index -> {
                    final Operation operation = new Operation();
                    operation.setTransaction(transaction);
                    operation.setAccount(freeAccounts.remove(random(0, freeAccounts.size())));
                    operation.setAmount(random(-1e5, 1e5));
                    return operation;
                })
                .collect(Collectors.toList())
        );

        transactionRepository.save(transaction);
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
