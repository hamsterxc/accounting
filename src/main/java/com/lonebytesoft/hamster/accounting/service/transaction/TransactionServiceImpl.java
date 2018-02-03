package com.lonebytesoft.hamster.accounting.service.transaction;

import com.lonebytesoft.hamster.accounting.model.Transaction;
import com.lonebytesoft.hamster.accounting.repository.TransactionRepository;
import com.lonebytesoft.hamster.accounting.util.Utils;
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

    @Autowired
    public TransactionServiceImpl(final TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
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

        final long threshold = Utils.calculateDayStart(
                action == TransactionAction.MOVE_EARLIER ? transactionTime : closestTime, 0);
        final long diff = Math.abs(threshold - closestTime);
        if(diff > 1) {
            transaction.setTime(Math.min(threshold, closestTime) + diff / 2);
            logger.debug("Performing time action, move in adjacent days: {}", transaction);
            transactionRepository.save(transaction);
        } else {
            if(action == TransactionAction.MOVE_EARLIER) {
                rebalance(Utils.calculateDayStart(closestTime, 0), threshold);
            } else {
                rebalance(threshold, Utils.calculateDayStart(closestTime, 1));
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
            start = Utils.calculateDayStart(transactionTime, -1);
            end = Utils.calculateDayStart(transactionTime, 0);
        } else {
            start = Utils.calculateDayStart(transactionTime, 1);
            end = Utils.calculateDayStart(transactionTime, 2);
        }

        transaction.setTime(start + (end - start) / 2);
        logger.debug("Performing time action, move lone: {}, {}", transaction);
        transactionRepository.save(transaction);
    }

    private boolean isSameDay(final long first, final long second) {
        final long dayFirst = Utils.calculateDayStart(first);
        final long daySecond = Utils.calculateDayStart(second);
        return dayFirst == daySecond;
    }
    
    private boolean isAdjacentDays(final long first, final long second) {
        final long min = Math.min(first, second);
        final long max = Math.max(first, second);
        return Utils.calculateDayStart(min, 1) == Utils.calculateDayStart(max, 0);
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
        final long time = Utils.calculateDayStart(transaction.getTime(), 1);
        transaction.setTime(time);

        logger.debug("Saving last-in-day transaction {}", transaction);

        final Transaction added = transactionRepository.save(transaction);
        performTimeAction(added, TransactionAction.MOVE_EARLIER);

        return added;
    }

}