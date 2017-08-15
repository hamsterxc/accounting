package com.lonebytesoft.hamster.accounting.controller;

import com.lonebytesoft.hamster.accounting.controller.view.ActionResultView;
import com.lonebytesoft.hamster.accounting.controller.view.OperationView;
import com.lonebytesoft.hamster.accounting.controller.view.SummaryView;
import com.lonebytesoft.hamster.accounting.controller.view.TransactionBoundaryView;
import com.lonebytesoft.hamster.accounting.controller.view.TransactionInputView;
import com.lonebytesoft.hamster.accounting.controller.view.TransactionView;
import com.lonebytesoft.hamster.accounting.controller.view.TransactionsView;
import com.lonebytesoft.hamster.accounting.model.Account;
import com.lonebytesoft.hamster.accounting.model.Category;
import com.lonebytesoft.hamster.accounting.model.Config;
import com.lonebytesoft.hamster.accounting.model.Currency;
import com.lonebytesoft.hamster.accounting.model.Transaction;
import com.lonebytesoft.hamster.accounting.repository.AccountRepository;
import com.lonebytesoft.hamster.accounting.repository.CategoryRepository;
import com.lonebytesoft.hamster.accounting.repository.TransactionRepository;
import com.lonebytesoft.hamster.accounting.service.ConfigService;
import com.lonebytesoft.hamster.accounting.service.TransactionAction;
import com.lonebytesoft.hamster.accounting.service.TransactionService;
import com.lonebytesoft.hamster.accounting.util.DateParser;
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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    private static final List<DateParser> DATE_FORMATS = Arrays.asList(
            new DateParser("dd.MM.yyyy", "%s." + Calendar.getInstance().get(Calendar.YEAR)), // dd.MM
            new DateParser("dd.MM.yy"),
            new DateParser("dd.MM.yyyy")
    );

    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    private final TransactionService transactionService;
    private final ConfigService configService;

    @Autowired
    public TransactionController(final AccountRepository accountRepository, final CategoryRepository categoryRepository,
                                 final TransactionRepository transactionRepository,
                                 final TransactionService transactionService, final ConfigService configService) {
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;

        this.transactionService = transactionService;
        this.configService = configService;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/runningtotal")
    public SummaryView getRunningTotal(@RequestParam(name = "from", defaultValue = "0") final Long from,
                                       @RequestParam(name = "to", required = false) final Long paramTo) {
        final long to = calculateUpperBound(paramTo);

        final Map<Long, Double> accountsRunningTotal = new HashMap<>();
        final Config config = configService.get();
        transactionRepository.findAllBetweenTime(from, to)
                .forEach(transaction -> {
                    transaction.getOperations()
                            .forEach(operation -> {
                                final Account account = operation.getAccount();
                                accountsRunningTotal.compute(account.getId(),
                                        (id, amount) -> amount == null ? operation.getAmount() : amount + operation.getAmount());
                            });
                    final double total = calculateTotal(transaction, config);
                    accountsRunningTotal.compute(null, (id, amount) -> amount == null ? total : amount + total);
                });

        final Double total = accountsRunningTotal.remove(null);
        return mapSummaryToView(from, to, accountsRunningTotal, total == null ? 0 : total);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/summary")
    public SummaryView getSummary(@RequestParam(name = "from", defaultValue = "0") final Long from,
                                  @RequestParam(name = "to", required = false) final Long paramTo) {
        final long to = calculateUpperBound(paramTo);

        final Map<Long, Double> items = new HashMap<>();
        final Config config = configService.get();
        transactionRepository.findAllBetweenTime(from, to)
                .forEach(transaction -> {
                    final double total = calculateTotal(transaction, config);
                    items.compute(transaction.getCategory().getId(), (id, amount) -> amount == null ? total : amount + total);
                    items.compute(null, (id, amount) -> amount == null ? total : amount + total);
                });

        final Double total = items.remove(null);
        logger.debug("Served 'summary' request: from = '{}', to = '{}', items = {}, total = {}",
                new Date(from), new Date(to), items, total);
        return mapSummaryToView(from, to, items, total == null ? 0 : total);
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
    public TransactionsView getTransactions(@RequestParam(name = "from", defaultValue = "0") final Long from,
                                            @RequestParam(name = "to", required = false) final Long paramTo) {
        final long to = calculateUpperBound(paramTo);

        final TransactionsView transactionsView = new TransactionsView();

        final Config config = configService.get();
        transactionsView.setTransactions(transactionRepository.findAllBetweenTime(from, to)
                .stream()
                .map(transaction -> mapTransactionToView(transaction, config))
                .collect(Collectors.toList()));

        transactionsView.setFrom(from);
        transactionsView.setTo(to);
        return transactionsView;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
    public TransactionView addTransaction(@RequestBody TransactionInputView transactionInputView) {
        final Long time = parseDate(transactionInputView.getDate());
        if(time == null) {
            throw new TransactionInputException("Could not parse date: '" + transactionInputView.getDate() + "'");
        }

        final Category category = categoryRepository.findOne(transactionInputView.getCategoryId());
        if(category == null) {
            throw new TransactionInputException("Could not find category, id=" + transactionInputView.getCategoryId());
        }

        final Map<Long, Double> operations = new HashMap<>();
        if(transactionInputView.getOperations() != null) {
            transactionInputView.getOperations()
                    .forEach(operationView -> {
                        final Account account = accountRepository.findOne(operationView.getId());
                        if(account == null) {
                            throw new TransactionInputException("Could not find account, id=" + operationView.getId());
                        }
                        operations.put(account.getId(), operationView.getAmount());
                    });
        }

        final Transaction transaction = transactionService.add(time, category, transactionInputView.getComment(), operations);
        return mapTransactionToView(transaction, configService.get());
    }

    @RequestMapping(method = RequestMethod.POST, path = "/{id}/{action}")
    public ActionResultView performTransactionAction(@PathVariable final long id,
                                                     @PathVariable final TransactionAction action) {
        final Transaction transaction = transactionRepository.findOne(id);
        if(transaction == null) {
            throw new TransactionInputException("Could not find transaction, id=" + id);
        }

        switch(action) {
            case MOVE_EARLIER:
            case MOVE_LATER:
                transactionService.performTimeAction(transaction, action);
                break;

            case DELETE:
                transactionService.remove(transaction);
                break;
        }

        final ActionResultView actionResultView = new ActionResultView();
        actionResultView.setStatus(ActionResultView.Status.SUCCESS);
        return actionResultView;
    }

    private long calculateUpperBound(final Long to) {
        return to == null ? System.currentTimeMillis() : to;
    }

    private double convert(final Currency from, final Currency to, final double amount) {
        final double amountBase = amount * from.getValue();
        return amountBase / to.getValue();
    }

    private Long parseDate(final String date) {
        return DATE_FORMATS
                .stream()
                .map(dateParser -> dateParser.parse(date))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private double calculateTotal(final Transaction transaction, final Config config) {
        return transaction.getOperations()
                .stream()
                .mapToDouble(operation ->
                        convert(operation.getAccount().getCurrency(), config.getCurrencyDefault(), operation.getAmount()))
                .sum();
    }

    private TransactionView mapTransactionToView(final Transaction transaction, final Config config) {
        final TransactionView transactionView = new TransactionView();
        transactionView.setId(transaction.getId());
        transactionView.setTime(transaction.getTime());
        transactionView.setCategoryId(transaction.getCategory().getId());
        transactionView.setComment(transaction.getComment());
        transactionView.setTotal(calculateTotal(transaction, config));
        transactionView.setOperations(transaction.getOperations()
                .stream()
                .map(operation -> {
                    final OperationView operationView = new OperationView();
                    operationView.setId(operation.getAccount().getId());
                    operationView.setAmount(operation.getAmount());
                    return operationView;
                })
                .collect(Collectors.toList())
        );
        return transactionView;
    }

    private SummaryView mapSummaryToView(final long from, final long to, final Map<Long, Double> items, final double total) {
        final SummaryView summaryView = new SummaryView();
        summaryView.setItems(items.entrySet()
                .stream()
                .map(entry -> {
                    final OperationView operationView = new OperationView();
                    operationView.setId(entry.getKey());
                    operationView.setAmount(entry.getValue());
                    return operationView;
                })
                .collect(Collectors.toList()));
        summaryView.setTotal(total);
        summaryView.setFrom(from);
        summaryView.setTo(to);
        return summaryView;
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
