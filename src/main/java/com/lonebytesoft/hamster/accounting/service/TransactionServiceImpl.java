package com.lonebytesoft.hamster.accounting.service;

import com.lonebytesoft.hamster.accounting.model.Account;
import com.lonebytesoft.hamster.accounting.model.Category;
import com.lonebytesoft.hamster.accounting.model.Operation;
import com.lonebytesoft.hamster.accounting.model.Transaction;
import com.lonebytesoft.hamster.accounting.repository.AccountRepository;
import com.lonebytesoft.hamster.accounting.repository.TransactionRepository;
import com.lonebytesoft.hamster.accounting.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public TransactionServiceImpl(final TransactionRepository transactionRepository, final AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public void performTimeAction(Transaction transaction, TransactionAction action) {
        assertTransactionTimeAction(action);

        final Transaction closest = action == TransactionAction.MOVE_EARLIER
                ? transactionRepository.findFirstBefore(transaction.getTime())
                : transactionRepository.findFirstAfter(transaction.getTime());
        logger.debug("Performing time action {}: {}, closest {}", action, transaction, closest);
        if(closest != null) {
            final long transactionTime = transaction.getTime();
            final long closestTime = closest.getTime();
            if(isSameDay(transactionTime, closestTime)) {
                swapTime(transaction, closest);
            } else if(isAdjacentDays(transactionTime, closestTime)) {
                moveInAdjacentDays(transaction, closest, action);
            } else {
                moveLone(transaction, action);
            }
        } else {
            moveLone(transaction, action);
        }
    }

    private void assertTransactionTimeAction(final TransactionAction action) {
        if((action != TransactionAction.MOVE_EARLIER) && (action != TransactionAction.MOVE_LATER)) {
            throw new IllegalArgumentException("Unexpected transaction time action: " + action);
        }
    }

    private void swapTime(final Transaction transaction, final Transaction closest) {
        final long transactionTime = transaction.getTime();
        final long closestTime = closest.getTime();

        closest.setTime(transactionTime);
        transaction.setTime(closestTime);

        logger.debug("Performing time action, swap time: {}, {}", transaction, closest);

        transactionRepository.save(transaction);
        transactionRepository.save(closest);
    }

    private void moveInAdjacentDays(final Transaction transaction, final Transaction closest,
                                    final TransactionAction action) {
        assertTransactionTimeAction(action);

        final long transactionTime = transaction.getTime();
        final long closestTime = closest.getTime();

        final long threshold = calculateDayStart(
                action == TransactionAction.MOVE_EARLIER ? transactionTime : closestTime, 0);
        final long diff = Math.abs(threshold - closestTime);
        if(diff > 1) {
            transaction.setTime(Math.min(threshold, closestTime) + diff / 2);
            logger.debug("Performing time action, move in adjacent days: {}", transaction);
            transactionRepository.save(transaction);
        } else {
            if(action == TransactionAction.MOVE_EARLIER) {
                rebalance(calculateDayStart(closestTime, 0), threshold);
            } else {
                rebalance(threshold, calculateDayStart(closestTime, 1));
            }
            performTimeAction(transaction, action);
        }
    }

    private void moveLone(final Transaction transaction, final TransactionAction action) {
        assertTransactionTimeAction(action);

        final long transactionTime = transaction.getTime();

        final long start;
        final long end;
        if(action == TransactionAction.MOVE_EARLIER) {
            start = calculateDayStart(transactionTime, -1);
            end = calculateDayStart(transactionTime, 0);
        } else {
            start = calculateDayStart(transactionTime, 1);
            end = calculateDayStart(transactionTime, 2);
        }

        transaction.setTime(start + (end - start) / 2);
        logger.debug("Performing time action, move lone: {}, {}", transaction);
        transactionRepository.save(transaction);
    }

    private boolean isSameDay(final long first, final long second) {
        final long dayFirst = calculateDayStart(first, 0);
        final long daySecond = calculateDayStart(second, 0);
        return dayFirst == daySecond;
    }
    
    private boolean isAdjacentDays(final long first, final long second) {
        final long min = Math.min(first, second);
        final long max = Math.max(first, second);
        return calculateDayStart(min, 1) == calculateDayStart(max, 0);
    }

    private long calculateDayStart(final long time, final int daysDelta) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        Utils.setCalendarDayStart(calendar);
        calendar.add(Calendar.DAY_OF_MONTH, daysDelta);
        return calendar.getTimeInMillis();
    }

    private void rebalance(final long from, final long to) {
        logger.debug("Rebalancing transactions in '{}' - '{}'", new Date(from), new Date(to));
        final List<Transaction> transactions = transactionRepository.findAllBetweenTime(from, to);
        final int count = transactions.size();
        final long delta = (to - from) / count;
        for(int i = 0; i < count; i++) {
            final Transaction transaction = transactions.get(i);
            transaction.setTime(from + i * delta);
            transactionRepository.save(transaction);
        }
    }

    @Override
    public Transaction add(long time, Category category, String comment, boolean visible, Map<Long, Double> operations) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        Utils.setCalendarDayStart(calendar);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        final Transaction transaction = addAtExactTime(calendar.getTimeInMillis(), category, comment, visible, operations);
        performTimeAction(transaction, TransactionAction.MOVE_EARLIER);

        return transaction;
    }

    @Override
    public Transaction addAtExactTime(long time, Category category, String comment, boolean visible,
                                      Map<Long, Double> operations) {
        final Transaction transaction = new Transaction();
        transaction.setTime(time);
        transaction.setCategory(category);
        transaction.setComment(comment);
        transaction.setVisible(visible);

        transaction.setOperations(operations
                .entrySet()
                .stream()
                .map(entry -> {
                    final Operation operation = new Operation();
                    operation.setTransaction(transaction);
                    operation.setAmount(entry.getValue());

                    final Account account = new Account();
                    account.setId(entry.getKey());
                    operation.setAccount(account);

                    return operation;
                })
                .collect(Collectors.toList()));

        final Transaction saved = transactionRepository.save(transaction);

        // todo: hack: deeply nested entities are not lazily loaded
        for(final Operation operation : saved.getOperations()) {
            operation.setAccount(accountRepository.findOne(operation.getAccount().getId()));
        }

        logger.debug("Saved transaction {}", saved);
        return saved;
    }

    @Override
    public void remove(Transaction transaction) {
        logger.debug("Deleting transaction {}", transaction);
        transactionRepository.delete(transaction);
    }

}
