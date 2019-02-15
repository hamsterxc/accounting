package com.lonebytesoft.hamster.accounting.controller;

import com.lonebytesoft.hamster.accounting.controller.exception.TransactionNotFoundException;
import com.lonebytesoft.hamster.accounting.controller.exception.UnsupportedActionException;
import com.lonebytesoft.hamster.accounting.controller.exception.UnsupportedAggregationException;
import com.lonebytesoft.hamster.accounting.controller.view.converter.ModelViewConverter;
import com.lonebytesoft.hamster.accounting.controller.view.input.AggregationInputView;
import com.lonebytesoft.hamster.accounting.controller.view.input.TransactionInputView;
import com.lonebytesoft.hamster.accounting.controller.view.output.AggregationItemView;
import com.lonebytesoft.hamster.accounting.controller.view.output.AggregationView;
import com.lonebytesoft.hamster.accounting.controller.view.output.TransactionBoundaryView;
import com.lonebytesoft.hamster.accounting.controller.view.output.TransactionView;
import com.lonebytesoft.hamster.accounting.controller.view.output.TransactionsView;
import com.lonebytesoft.hamster.accounting.model.Aggregation;
import com.lonebytesoft.hamster.accounting.model.Transaction;
import com.lonebytesoft.hamster.accounting.repository.TransactionRepository;
import com.lonebytesoft.hamster.accounting.service.EntityAction;
import com.lonebytesoft.hamster.accounting.service.aggregation.AggregationService;
import com.lonebytesoft.hamster.accounting.service.date.DateService;
import com.lonebytesoft.hamster.accounting.service.transaction.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionRepository transactionRepository;

    private final TransactionService transactionService;
    private final DateService dateService;
    private final AggregationService aggregationService;

    private final ModelViewConverter<Transaction, TransactionInputView, TransactionView> transactionConverter;

    @Autowired
    public TransactionController(
            final TransactionRepository transactionRepository,
            final TransactionService transactionService,
            final DateService dateService,
            final AggregationService aggregationService,
            final ModelViewConverter<Transaction, TransactionInputView, TransactionView> transactionConverter
    ) {
        this.transactionRepository = transactionRepository;

        this.transactionService = transactionService;
        this.dateService = dateService;
        this.aggregationService = aggregationService;

        this.transactionConverter = transactionConverter;
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
                            throw new UnsupportedAggregationException(aggregationInputView.getField().name());
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
                new Date(transactionBoundaryView.getLower()),
                new Date(transactionBoundaryView.getUpper()));
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

        return new TransactionsView(
                fromTimestamp,
                toTimestamp,
                transactionRepository.findAllBetweenTime(fromTimestamp, toTimestamp)
                        .stream()
                        .map(transactionConverter::convertToOutput)
                        .collect(Collectors.toList())
        );
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
    public TransactionView addTransaction(@RequestBody TransactionInputView transactionInputView) {
        final Transaction transaction = new Transaction();
        transaction.setOperations(new ArrayList<>());
        transactionConverter.populateFromInput(transaction, transactionInputView);

        final Transaction added = transactionService.addLastInDay(transaction);
        return transactionConverter.convertToOutput(added);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/{id}", consumes = "application/json")
    public TransactionView modifyTransaction(@PathVariable final long id,
                                             @RequestBody TransactionInputView transactionInputView) {
        final Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        final long time = transaction.getTime();
        transactionConverter.populateFromInput(transaction, transactionInputView);

        final Transaction modified;
        if(dateService.calculateDayStart(time, 0) == dateService.calculateDayStart(transaction.getTime(), 0)) {
            transaction.setTime(time);
            modified = transactionRepository.save(transaction);
        } else {
            modified = transactionService.addLastInDay(transaction);
        }

        return transactionConverter.convertToOutput(modified);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/{id}/{action}")
    public void performTransactionAction(
            @PathVariable final long id,
            @PathVariable final EntityAction action
    ) {
        final Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        switch(action) {
            case MOVE_UP:
            case MOVE_DOWN:
                transactionService.performTimeAction(transaction, action);
                break;

            default:
                throw new UnsupportedActionException(action.getParamValue());
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/{id}")
    public void deleteTransaction(
            @PathVariable final long id
    ) {
        final Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));
        transactionRepository.delete(transaction);
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

}
