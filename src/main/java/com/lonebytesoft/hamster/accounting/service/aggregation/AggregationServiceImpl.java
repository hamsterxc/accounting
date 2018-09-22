package com.lonebytesoft.hamster.accounting.service.aggregation;

import com.lonebytesoft.hamster.accounting.model.Account;
import com.lonebytesoft.hamster.accounting.model.Aggregation;
import com.lonebytesoft.hamster.accounting.model.Currency;
import com.lonebytesoft.hamster.accounting.model.Operation;
import com.lonebytesoft.hamster.accounting.model.Transaction;
import com.lonebytesoft.hamster.accounting.service.config.ConfigService;
import com.lonebytesoft.hamster.accounting.service.currency.CurrencyService;
import com.lonebytesoft.hamster.accounting.service.transaction.TransactionService;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AggregationServiceImpl implements AggregationService {

    private final TransactionService transactionService;
    private final CurrencyService currencyService;
    private final ConfigService configService;

    public AggregationServiceImpl(
            final TransactionService transactionService,
            final CurrencyService currencyService,
            final ConfigService configService
    ) {
        this.transactionService = transactionService;
        this.currencyService = currencyService;
        this.configService = configService;
    }

    @Override
    public Aggregation aggregateByAccount(Collection<Transaction> transactions) {
        final Map<Account, Double> itemsByAccount = transactions.stream()
                .flatMap(transaction -> transaction.getOperations().stream())
                .filter(Operation::isActive)
                .collect(Collectors.groupingBy(Operation::getAccount))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        aggregationEntry -> aggregationEntry.getValue().stream()
                                .mapToDouble(operation ->
                                        transactionService.calculateTotal(operation, operation.getAccount().getCurrency()))
                                .sum()
                ));

        final Currency currencyDefault = configService.get().getCurrencyDefault();
        final double total = itemsByAccount.entrySet().stream()
                .mapToDouble(aggregationEntry ->
                        currencyService.convert(aggregationEntry.getKey().getCurrency(), currencyDefault, aggregationEntry.getValue()))
                .sum();

        final Map<Long, Double> items = itemsByAccount.entrySet().stream()
                .collect(Collectors.toMap(
                        aggregationEntry -> aggregationEntry.getKey().getId(),
                        Map.Entry::getValue
                ));
        return new Aggregation(items, total);
    }

    @Override
    public Aggregation aggregateByCategory(Collection<Transaction> transactions) {
        final Map<Long, Double> items = transactions.stream()
                .collect(Collectors.groupingBy(transaction -> transaction.getCategory().getId()))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        aggregationEntry -> aggregationEntry.getValue().stream()
                                .mapToDouble(transactionService::calculateTotal)
                                .sum()
                ));

        final double total = items.entrySet().stream()
                .mapToDouble(Map.Entry::getValue)
                .sum();

        return new Aggregation(items, total);
    }

}
