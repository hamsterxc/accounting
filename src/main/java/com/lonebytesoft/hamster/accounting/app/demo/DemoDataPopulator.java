package com.lonebytesoft.hamster.accounting.app.demo;

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
import com.lonebytesoft.hamster.accounting.service.config.ConfigService;
import com.lonebytesoft.hamster.accounting.service.currency.CurrencyService;
import com.lonebytesoft.hamster.accounting.service.date.DateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

@Component
@Profile("demo")
public class DemoDataPopulator {

    private static final Logger logger = LoggerFactory.getLogger(DemoDataPopulator.class);

    private static final String WORDS_RESOURCE = "dictionary_en.txt";

    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final CurrencyRepository currencyRepository;
    private final TransactionRepository transactionRepository;

    private final CurrencyService currencyService;
    private final DateService dateService;
    private final ConfigService configService;

    private final DemoProperties properties;

    private final List<String> words;

    @Autowired
    public DemoDataPopulator(
            final AccountRepository accountRepository,
            final CategoryRepository categoryRepository,
            final CurrencyRepository currencyRepository,
            final TransactionRepository transactionRepository,
            final CurrencyService currencyService,
            final DateService dateService,
            final ConfigService configService,
            final DemoProperties demoProperties
    ) {
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.currencyRepository = currencyRepository;
        this.transactionRepository = transactionRepository;

        this.currencyService = currencyService;
        this.dateService = dateService;
        this.configService = configService;

        this.properties = demoProperties;

        // https://svnweb.freebsd.org/base/release/12.0.0/share/dict/web2?revision=341707&view=co
        this.words = readLines(WORDS_RESOURCE);
    }

    private List<String> readLines(final String name) {
        try (
                final InputStream wordsStream = getClass().getClassLoader().getResourceAsStream(name)
        ) {
            if (wordsStream != null) {
                try (
                        final InputStreamReader wordsReader = new InputStreamReader(wordsStream);
                        final BufferedReader words = new BufferedReader(wordsReader)
                ) {
                    return words.lines().collect(Collectors.toList());
                }
            } else {
                throw new IllegalStateException("Resource not found: " + name);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read resource: " + name);
        }
    }

    @PostConstruct
    public void populate() {
        logger.info("Populating test data");

        final List<Category> categories = populateCategory(properties.getCount().getCategory());
        final List<Currency> currencies = populateCurrency(properties.getCount().getCurrency());
        final List<Account> accounts = populateAccount(properties.getCount().getAccount(), currencies);
        populateConfig(currencies);

        final long now = System.currentTimeMillis();
        final long from = addPeriod(now, fixPeriod(properties.getTransaction().getDateRangeBack(), true));
        final long to = addPeriod(now, fixPeriod(properties.getTransaction().getDateRangeForward(), false));

        transactionRepository.deleteAll();
        IntStream.range(0, properties.getCount().getTransaction())
                .forEach(i -> populateTransactionWithOperations(categories, currencies, accounts, from, to));
    }

    private String fixPeriod(final String period, final boolean isBack) {
        String result = period.toUpperCase();
        result = result.startsWith("P") ? result : "P" + result;
        if (isBack) {
            return result.startsWith("-") ? result : "-" + result;
        } else {
            return result.startsWith("-") ? result.substring(1) : result;
        }
    }

    private long addPeriod(final long base, final String offset) {
        final Period period = Period.parse(offset);
        final Calendar calendar = dateService.obtainCalendar();
        calendar.setTimeInMillis(base);
        calendar.add(Calendar.YEAR, period.getYears());
        calendar.add(Calendar.MONTH, period.getMonths());
        calendar.add(Calendar.DAY_OF_YEAR, period.getDays());
        return calendar.getTimeInMillis();
    }

    private void populateConfig(final List<Currency> currencies) {
        if (currencies.size() > 0) {
            final Config config = new Config(
                    currencies.get(random(0, currencies.size()))
            );
            configService.save(config);
        }
    }

    private List<Category> populateCategory(final int count) {
        final List<Long> ordering = randomOrdering(count);
        final List<Category> categories = IntStream.range(0, count)
                .mapToObj(i -> new Category(
                        0,
                        generateRandomWords(
                                properties.getWords().getCategoryNameMin(),
                                properties.getWords().getCategoryNameMax()
                        ),
                        ordering.get(i),
                        random(properties.getProbability().getCategoryVisible())
                ))
                .collect(Collectors.toList());

        categoryRepository.deleteAll();
        return StreamSupport.stream(categoryRepository.saveAll(categories).spliterator(), false)
                .collect(Collectors.toList());
    }

    private List<Currency> populateCurrency(final int count) {
        final List<Currency> knownCurrencies = new ArrayList<>(currencyService.getKnownCurrencies());
        Collections.shuffle(knownCurrencies);
        final Iterable<Currency> currencies = currencyRepository.saveAll(knownCurrencies.subList(0, count));
        return StreamSupport.stream(currencies.spliterator(), false)
                .collect(Collectors.toList());
    }

    private List<Account> populateAccount(final int count, final List<Currency> currencies) {
        final List<Long> ordering = randomOrdering(count);
        final List<Account> accounts = IntStream.range(0, count)
                .mapToObj(i -> new Account(
                        0,
                        generateRandomWords(
                                properties.getWords().getAccountNameMin(),
                                properties.getWords().getAccountNameMax()
                        ),
                        currencies.get(random(0, currencies.size())),
                        ordering.get(i),
                        random(properties.getProbability().getAccountVisible())
                ))
                .collect(Collectors.toList());

        accountRepository.deleteAll();
        return StreamSupport.stream(accountRepository.saveAll(accounts).spliterator(), false)
                .collect(Collectors.toList());
    }

    private void populateTransactionWithOperations(
            final List<Category> categories,
            final List<Currency> currencies,
            final List<Account> accounts,
            final long timeFrom,
            final long timeTo
    ) {
        final Transaction transaction = new Transaction(
                0,
                random(timeFrom, timeTo),
                categories.get(random(0, categories.size())),
                generateRandomWords(
                        properties.getWords().getTransactionCommentMin(),
                        properties.getWords().getTransactionCommentMax()
                ),
                random(properties.getProbability().getTransactionVisible())
        );

        final int operationsCount = random(
                properties.getTransaction().getOperationCountMin(),
                properties.getTransaction().getOperationCountMax()
        );
        transaction.setOperations(IntStream.range(0, operationsCount)
                .mapToObj(index -> {
                    final Operation operation = new Operation(
                            0,
                            accounts.get(random(0, accounts.size())),
                            null,
                            random(
                                    properties.getTransaction().getOperationAmountMin(),
                                    properties.getTransaction().getOperationAmountMax()
                            ),
                            random(properties.getProbability().getOperationActive())
                    );

                    operation.setTransaction(transaction);
                    if (random(properties.getProbability().getOperationDifferentCurrency())) {
                        operation.setCurrency(currencies.get(random(0, currencies.size())));
                    }

                    return operation;
                })
                .collect(Collectors.toList())
        );

        transactionRepository.save(transaction);
    }

    private String generateRandomWords(final int minCount, final int maxCount) {
        final int wordsCount = words.size();
        final String sentence = IntStream.range(0, random(minCount, maxCount))
                .map(i -> random(0, wordsCount))
                .mapToObj(words::get)
                .collect(Collectors.joining(" "))
                .toLowerCase();
        return sentence.substring(0, 1).toUpperCase() + sentence.substring(1);
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

    private boolean random(final double probability) {
        return Math.random() < probability;
    }

    private List<Long> randomOrdering(final int count) {
        final List<Long> ordering = LongStream.range(0, count)
                .boxed()
                .collect(Collectors.toList());
        Collections.shuffle(ordering);
        return ordering;
    }

}
