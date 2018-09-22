package com.lonebytesoft.hamster.accounting.controller;

import com.lonebytesoft.hamster.accounting.controller.view.ActionResultView;
import com.lonebytesoft.hamster.accounting.controller.view.AggregationInputView;
import com.lonebytesoft.hamster.accounting.controller.view.AggregationItemView;
import com.lonebytesoft.hamster.accounting.controller.view.AggregationView;
import com.lonebytesoft.hamster.accounting.controller.view.OperationView;
import com.lonebytesoft.hamster.accounting.controller.view.TransactionBoundaryView;
import com.lonebytesoft.hamster.accounting.controller.view.TransactionInputView;
import com.lonebytesoft.hamster.accounting.controller.view.TransactionView;
import com.lonebytesoft.hamster.accounting.controller.view.TransactionsView;
import com.lonebytesoft.hamster.accounting.model.Account;
import com.lonebytesoft.hamster.accounting.model.Aggregation;
import com.lonebytesoft.hamster.accounting.model.Category;
import com.lonebytesoft.hamster.accounting.model.Config;
import com.lonebytesoft.hamster.accounting.model.Currency;
import com.lonebytesoft.hamster.accounting.model.Operation;
import com.lonebytesoft.hamster.accounting.model.Transaction;
import com.lonebytesoft.hamster.accounting.repository.AccountRepository;
import com.lonebytesoft.hamster.accounting.repository.CategoryRepository;
import com.lonebytesoft.hamster.accounting.repository.CurrencyRepository;
import com.lonebytesoft.hamster.accounting.repository.TransactionRepository;
import com.lonebytesoft.hamster.accounting.service.EntityAction;
import com.lonebytesoft.hamster.accounting.service.aggregation.AggregationService;
import com.lonebytesoft.hamster.accounting.service.config.ConfigService;
import com.lonebytesoft.hamster.accounting.service.date.DateService;
import com.lonebytesoft.hamster.accounting.service.transaction.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    private static final String OPERATION_AMOUNT_PATTERN_STRING = "((-+)?\\d+(" + DecimalFormatSymbols.getInstance().getDecimalSeparator() + "\\d+)?)";
    private static final Pattern OPERATION_AMOUNT_PATTERN = Pattern.compile(OPERATION_AMOUNT_PATTERN_STRING);
    private static final Pattern OPERATION_AMOUNT_CURRENCY_PATTERN = Pattern.compile(OPERATION_AMOUNT_PATTERN_STRING + "\\s+(\\w+)");

    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final CurrencyRepository currencyRepository;
    private final TransactionRepository transactionRepository;

    private final TransactionService transactionService;
    private final ConfigService configService;
    private final DateService dateService;
    private final AggregationService aggregationService;

    @Autowired
    public TransactionController(
            final AccountRepository accountRepository,
            final CategoryRepository categoryRepository,
            final CurrencyRepository currencyRepository,
            final TransactionRepository transactionRepository,
            final TransactionService transactionService,
            final ConfigService configService,
            final DateService dateService,
            final AggregationService aggregationService
    ) {
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.currencyRepository = currencyRepository;
        this.transactionRepository = transactionRepository;

        this.transactionService = transactionService;
        this.configService = configService;
        this.dateService = dateService;
        this.aggregationService = aggregationService;
    }

    @RequestMapping(method = RequestMethod.POST, path = "/aggregate")
    public AggregationView aggregate(
            @RequestBody final AggregationInputView aggregationInputView
    ) {
        final Collection<AggregationItemView> items = aggregationInputView.getFilters().stream()
                .map(filter -> {
                    final long from = parseTimestampParam(filter.getFrom(), filter.getFromDate(), () -> 0L);
                    final long to = parseTimestampParam(filter.getTo(), filter.getToDate(),
                            () -> dateService.calculateDayStart(System.currentTimeMillis(), 1));

                    final Collection<Transaction> transactions = transactionRepository.findAllBetweenTime(from, to).stream()
                            .filter(transaction -> aggregationInputView.getIncludeHidden() || transaction.getVisible())
                            .collect(Collectors.toList());

                    final Aggregation aggregation;
                    switch(aggregationInputView.getField()) {
                        case ACCOUNT:
                            aggregation = aggregationService.aggregateByAccount(transactions);
                            break;

                        case CATEGORY:
                            aggregation = aggregationService.aggregateByCategory(transactions);
                            break;

                        default:
                            throw new TransactionInputException("Unsupported aggregation: " + aggregationInputView.getField());
                    }

                    return new AggregationItemView(from, to, aggregationInputView.getField(),
                            aggregation.getItems(), aggregation.getTotal());
                })
                .collect(Collectors.toList());

        return new AggregationView(items);
    }

    @RequestMapping(method = RequestMethod.GET, path = "boundary")
    public TransactionBoundaryView getBoundaries() {
        final TransactionBoundaryView transactionBoundaryView = new TransactionBoundaryView();

        final Transaction lower = transactionRepository.findFirstAfter(0);
        if(lower != null) {
            transactionBoundaryView.setLower(lower.getTime());
        }

        final Transaction upper = transactionRepository.findFirstBefore(Long.MAX_VALUE);
        if(upper != null) {
            transactionBoundaryView.setUpper(upper.getTime());
        }

        logger.debug("Served 'boundary' request: lower = '{}', upper = '{}'",
                transactionBoundaryView.getLower() == null ? "<absent>" : new Date(transactionBoundaryView.getLower()),
                transactionBoundaryView.getUpper() == null ? "<absent>" : new Date(transactionBoundaryView.getUpper()));
        return transactionBoundaryView;
    }

    @RequestMapping(method = RequestMethod.GET)
    public TransactionsView getTransactions(
            @RequestParam(name = "from", required = false) final Long from,
            @RequestParam(name = "fromDate", defaultValue = "") final String fromDate,
            @RequestParam(name = "to", required = false) final Long to,
            @RequestParam(name = "toDate", defaultValue = "") final String toDate
    ) {
        final long fromTimestamp = parseTimestampParam(from, fromDate, () -> 0L);
        final long toTimestamp = parseTimestampParam(to, toDate, () -> dateService.calculateDayStart(System.currentTimeMillis(), 1));

        final Config config = configService.get();

        final TransactionsView transactionsView = new TransactionsView();
        transactionsView.setTransactions(
                transactionRepository.findAllBetweenTime(fromTimestamp, toTimestamp)
                        .stream()
                        .map(transaction -> mapTransactionToView(transaction, config))
                        .collect(Collectors.toList())
        );
        transactionsView.setFrom(fromTimestamp);
        transactionsView.setTo(toTimestamp);
        return transactionsView;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
    public TransactionView addTransaction(@RequestBody TransactionInputView transactionInputView) {
        final Transaction transaction = new Transaction();
        transaction.setOperations(new ArrayList<>());
        populateTransactionFromView(transaction, transactionInputView);

        final Transaction added = transactionService.addLastInDay(transaction);
        return mapTransactionToView(added, configService.get());
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/{id}", consumes = "application/json")
    public TransactionView modifyTransaction(@PathVariable final long id,
                                             @RequestBody TransactionInputView transactionInputView) {
        final Transaction transaction = transactionRepository.findOne(id);
        if(transaction == null) {
            throw new TransactionInputException("Could not find transaction, id=" + id);
        }

        final long time = transaction.getTime();
        populateTransactionFromView(transaction, transactionInputView);

        final Transaction modified;
        if(dateService.calculateDayStart(time, 0) == dateService.calculateDayStart(transaction.getTime(), 0)) {
            transaction.setTime(time);
            modified = transactionRepository.save(transaction);
        } else {
            modified = transactionService.addLastInDay(transaction);
        }

        return mapTransactionToView(modified, configService.get());
    }

    @RequestMapping(method = RequestMethod.POST, path = "/{id}/{action}")
    public ActionResultView performTransactionAction(@PathVariable final long id,
                                                     @PathVariable final EntityAction action) {
        final Transaction transaction = transactionRepository.findOne(id);
        if(transaction == null) {
            throw new TransactionInputException("Could not find transaction, id=" + id);
        }

        switch(action) {
            case MOVE_UP:
            case MOVE_DOWN:
                transactionService.performTimeAction(transaction, action);
                break;

            case DELETE:
                transactionRepository.delete(transaction);
                break;
        }

        final ActionResultView actionResultView = new ActionResultView();
        actionResultView.setStatus(ActionResultView.Status.SUCCESS);
        return actionResultView;
    }

    private long parseTimestampParam(final Long param, final String paramDate, final Supplier<Long> defaultValue) {
        if(param != null) {
            return param;
        }

        if((paramDate == null) || paramDate.equals("")) {
            return defaultValue.get();
        }

        final Long parsed = dateService.parse(paramDate);
        if(parsed != null) {
            return parsed;
        }

        return defaultValue.get();
    }

    private void populateTransactionFromView(final Transaction transaction, final TransactionInputView transactionInputView) {
        final Long time = dateService.parse(transactionInputView.getDate());
        if(time == null) {
            throw new TransactionInputException("Could not parse date: '" + transactionInputView.getDate() + "'");
        }
        transaction.setTime(time);

        final Category category = categoryRepository.findOne(transactionInputView.getCategoryId());
        if(category == null) {
            throw new TransactionInputException("Could not find category, id=" + transactionInputView.getCategoryId());
        }
        transaction.setCategory(category);

        transaction.setComment(transactionInputView.getComment());

        transaction.setVisible(true);

        if(transactionInputView.getOperations() != null) {
            final Collection<Operation> operations = transactionInputView.getOperations()
                    .stream()
                    .filter(operationInputView -> !isBlank(operationInputView.getAmount()))
                    .map(operationInputView -> {
                        final Operation operation = new Operation();

                        operation.setTransaction(transaction);

                        final Account account = accountRepository.findOne(operationInputView.getAccountId());
                        if(account == null) {
                            throw new TransactionInputException("Could not find account, id=" + operationInputView.getAccountId());
                        }
                        operation.setAccount(account);

                        final double amount;
                        final Matcher matcherWithCurrency = OPERATION_AMOUNT_CURRENCY_PATTERN.matcher(operationInputView.getAmount());
                        if(matcherWithCurrency.find()) {
                            amount = Double.parseDouble(matcherWithCurrency.group(1));

                            final String currencyCode = matcherWithCurrency.group(4);
                            final Currency currency = currencyRepository.findByCode(currencyCode);
                            if(currency == null) {
                                throw new TransactionInputException("Could not find currency, code=" + currencyCode);
                            }
                            operation.setCurrency(currency);
                        } else {
                            final Matcher matcher = OPERATION_AMOUNT_PATTERN.matcher(operationInputView.getAmount());
                            if(matcher.find()) {
                                amount = Double.parseDouble(matcher.group(1));
                            } else {
                                throw new TransactionInputException("Could not parse amount string: " + operationInputView.getAmount());
                            }
                        }
                        operation.setAmount(amount);

                        operation.setActive(operationInputView.isActive());

                        return operation;
                    })
                    .collect(Collectors.toList());

            // preserving collection possibly tracked by Hibernate
            transaction.getOperations().clear();
            transaction.getOperations().addAll(operations);
        }
    }

    private boolean isBlank(final String s) {
        return (s == null) || (s.trim().equals(""));
    }

    private TransactionView mapTransactionToView(final Transaction transaction, final Config config) {
        final TransactionView transactionView = new TransactionView();
        transactionView.setId(transaction.getId());
        transactionView.setTime(transaction.getTime());
        transactionView.setCategoryId(transaction.getCategory().getId());
        transactionView.setComment(transaction.getComment());
        transactionView.setVisible(transaction.getVisible());
        transactionView.setTotal(transactionService.calculateTotal(transaction));
        transactionView.setOperations(transaction.getOperations()
                .stream()
                .map(operation -> {
                    final OperationView operationView = new OperationView();
                    operationView.setAccountId(operation.getAccount().getId());
                    operationView.setCurrencyId(operation.getCurrency() == null ? null : operation.getCurrency().getId());
                    operationView.setAmount(operation.getAmount());
                    operationView.setActive(operation.isActive());
                    return operationView;
                })
                .collect(Collectors.toList())
        );
        return transactionView;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(TransactionInputException.class)
    public ActionResultView handleTransactionInputException(final TransactionInputException e) {
        final ActionResultView actionResultView = new ActionResultView();
        actionResultView.setStatus(ActionResultView.Status.ERROR);
        actionResultView.setInfo(e.getMessage());
        return actionResultView;
    }

    private class TransactionInputException extends RuntimeException {
        public TransactionInputException(final String message) {
            super(message);
        }
    }

}
