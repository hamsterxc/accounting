package com.lonebytesoft.hamster.accounting.controller;

import com.lonebytesoft.hamster.accounting.controller.view.OperationView;
import com.lonebytesoft.hamster.accounting.controller.view.RunningTotalView;
import com.lonebytesoft.hamster.accounting.controller.view.TransactionView;
import com.lonebytesoft.hamster.accounting.model.Account;
import com.lonebytesoft.hamster.accounting.model.Config;
import com.lonebytesoft.hamster.accounting.model.Currency;
import com.lonebytesoft.hamster.accounting.repository.AccountRepository;
import com.lonebytesoft.hamster.accounting.repository.TransactionRepository;
import com.lonebytesoft.hamster.accounting.service.ConfigService;
import com.lonebytesoft.hamster.accounting.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
public class TransactionController {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ConfigService configService;

    @Autowired
    public TransactionController(final AccountRepository accountRepository, final TransactionRepository transactionRepository,
                                 final ConfigService configService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.configService = configService;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/transaction/runningtotal")
    public RunningTotalView getRunningTotal(@RequestParam(name = "to", required = false) final Long to) {
        final Map<Long, Double> accountsRunningTotal = StreamSupport.stream(accountRepository.findAll().spliterator(), false)
                .collect(Collectors.toMap(
                        Account::getId,
                        account -> 0.0,
                        Utils.obtainNoDuplicatesFunction(),
                        HashMap::new // allowing null key for total
                ));
        accountsRunningTotal.put(null, 0.0);

        final Config config = configService.get();
        transactionRepository.findAllBetweenTime(0, calculateUpperBound(to))
                .forEach(transaction -> {
                    final double total = transaction.getOperations()
                            .stream()
                            .mapToDouble(operation -> {
                                final Account account = operation.getAccount();
                                accountsRunningTotal.compute(account.getId(), (id, amount) -> amount + operation.getAmount());
                                return convert(account.getCurrency(), config.getCurrencyDefault(), operation.getAmount());
                            })
                            .sum();
                    accountsRunningTotal.compute(null, (id, amount) -> amount + total);
                });

        final RunningTotalView runningTotalView = new RunningTotalView();
        runningTotalView.setItems(accountsRunningTotal
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey() != null)
                .map(entry -> {
                    final OperationView operationView = new OperationView();
                    operationView.setAccountId(entry.getKey());
                    operationView.setAmount(entry.getValue());
                    return operationView;
                })
                .collect(Collectors.toList())
        );
        runningTotalView.setTotal(accountsRunningTotal.get(null));
        return runningTotalView;
    }

    private double convert(final Currency from, final Currency to, final double amount) {
        final double amountBase = amount * from.getValue();
        return amountBase / to.getValue();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/transaction")
    public List<TransactionView> getTransactions(@RequestParam(name = "from", defaultValue = "0") final Long from,
                                                 @RequestParam(name = "to", required = false) final Long to) {
        return transactionRepository.findAllBetweenTime(from, calculateUpperBound(to))
                .stream()
                .map(transaction -> {
                    final TransactionView transactionView = new TransactionView();
                    transactionView.setId(transaction.getId());
                    transactionView.setTime(transaction.getTime());
                    transactionView.setCategoryId(transaction.getCategory().getId());
                    transactionView.setComment(transaction.getComment());
                    transactionView.setOperations(transaction.getOperations()
                            .stream()
                            .map(operation -> {
                                final OperationView operationView = new OperationView();
                                operationView.setAccountId(operation.getAccount().getId());
                                operationView.setAmount(operation.getAmount());
                                return operationView;
                            })
                            .collect(Collectors.toList())
                    );
                    return transactionView;
                })
                .collect(Collectors.toList());
    }

    private long calculateUpperBound(final Long to) {
        return to == null ? System.currentTimeMillis() : to;
    }

}
