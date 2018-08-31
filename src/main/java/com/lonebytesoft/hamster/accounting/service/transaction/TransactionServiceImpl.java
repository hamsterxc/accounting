package com.lonebytesoft.hamster.accounting.service.transaction;

import com.lonebytesoft.hamster.accounting.model.Transaction;
import com.lonebytesoft.hamster.accounting.repository.TransactionRepository;
import com.lonebytesoft.hamster.accounting.service.EntityAction;
import com.lonebytesoft.hamster.accounting.service.date.DateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final TransactionRepository transactionRepository;
    private final DateService dateService;

    @Autowired
    public TransactionServiceImpl(final TransactionRepository transactionRepository,
                                  final DateService dateService) {
        this.transactionRepository = transactionRepository;
        this.dateService = dateService;
    }

    @Override
    public void performTimeAction(Transaction transaction, EntityAction action) {
        assertTransactionTimeAction(action);

        final Transaction closest = action == EntityAction.MOVE_UP
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

    private void assertTransactionTimeAction(final EntityAction action) {
        if((action != EntityAction.MOVE_UP) && (action != EntityAction.MOVE_DOWN)) {
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
                                    final EntityAction action) {
        assertTransactionTimeAction(action);

        final long transactionTime = transaction.getTime();
        final long closestTime = closest.getTime();

        final long threshold = dateService.calculateDayStart(
                action == EntityAction.MOVE_UP ? transactionTime : closestTime, 0);
        final long diff = Math.abs(threshold - closestTime);
        if(diff > 1) {
            transaction.setTime(Math.min(threshold, closestTime) + diff / 2);
            logger.debug("Performing time action, move in adjacent days: {}", transaction);
            transactionRepository.save(transaction);
        } else {
            if(action == EntityAction.MOVE_UP) {
                rebalance(dateService.calculateDayStart(closestTime, 0), threshold);
            } else {
                rebalance(threshold, dateService.calculateDayStart(closestTime, 1));
            }
            performTimeAction(transaction, action);
        }
    }

    private void moveLone(final Transaction transaction, final EntityAction action) {
        assertTransactionTimeAction(action);

        final long transactionTime = transaction.getTime();

        final long start;
        final long end;
        if(action == EntityAction.MOVE_UP) {
            start = dateService.calculateDayStart(transactionTime, -1);
            end = dateService.calculateDayStart(transactionTime, 0);
        } else {
            start = dateService.calculateDayStart(transactionTime, 1);
            end = dateService.calculateDayStart(transactionTime, 2);
        }

        transaction.setTime(start + (end - start) / 2);
        logger.debug("Performing time action, move lone: {}, {}", transaction);
        transactionRepository.save(transaction);
    }

    private boolean isSameDay(final long first, final long second) {
        final long dayFirst = dateService.calculateDayStart(first, 0);
        final long daySecond = dateService.calculateDayStart(second, 0);
        return dayFirst == daySecond;
    }
    
    private boolean isAdjacentDays(final long first, final long second) {
        final long min = Math.min(first, second);
        final long max = Math.max(first, second);
        return dateService.calculateDayStart(min, 1) == dateService.calculateDayStart(max, 0);
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
    public Transaction addLastInDay(Transaction transaction) {
        final long time = dateService.calculateDayStart(transaction.getTime(), 1);
        transaction.setTime(time);

        logger.debug("Saving last-in-day transaction {}", transaction);

        final Transaction added = transactionRepository.save(transaction);
        performTimeAction(added, EntityAction.MOVE_UP);

        return added;
    }

}
