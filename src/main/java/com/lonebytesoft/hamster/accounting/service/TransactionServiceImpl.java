package com.lonebytesoft.hamster.accounting.service;

import com.lonebytesoft.hamster.accounting.dao.OperationDao;
import com.lonebytesoft.hamster.accounting.dao.TransactionDao;
import com.lonebytesoft.hamster.accounting.model.Operation;
import com.lonebytesoft.hamster.accounting.model.Transaction;
import com.lonebytesoft.hamster.accounting.util.Utils;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TransactionServiceImpl implements TransactionService {

    private final TransactionDao transactionDao;
    private final OperationDao operationDao;

    public TransactionServiceImpl(final TransactionDao transactionDao, final OperationDao operationDao) {
        this.transactionDao = transactionDao;
        this.operationDao = operationDao;
    }

    @Override
    public void performTimeAction(Transaction transaction, TransactionAction action) {
        assertTransactionTimeAction(action);

        final Optional<Transaction> closestOptional = action == TransactionAction.MOVE_EARLIER
                ? transactionDao.getClosestBefore(transaction)
                : transactionDao.getClosestAfter(transaction);
        if(closestOptional.isPresent()) {
            final Transaction closest = closestOptional.get();
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

        transactionDao.save(transaction);
        transactionDao.save(closest);
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
            transactionDao.save(transaction);
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
        transactionDao.save(transaction);
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
        final List<Transaction> transactions = transactionDao.get(from, to);
        final int count = transactions.size();
        final long delta = (to - from) / count;
        for(int i = 0; i < count; i++) {
            final Transaction transaction = transactions.get(i);
            transaction.setTime(from + i * delta);
            transactionDao.save(transaction);
        }
    }

    @Override
    public long add(long time, long categoryId, String comment, Map<Long, Double> operations) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        Utils.setCalendarDayStart(calendar);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        final long id = addAtExactTime(calendar.getTimeInMillis(), categoryId, comment, operations);

        final Transaction transaction = transactionDao.get(id)
                .orElseThrow(() -> new IllegalStateException("Unable to add transaction"));
        performTimeAction(transaction, TransactionAction.MOVE_EARLIER);

        return id;
    }

    @Override
    public long addAtExactTime(long time, long categoryId, String comment, Map<Long, Double> operations) {
        final Transaction transaction = new Transaction();
        transaction.setTime(time);
        transaction.setCategoryId(categoryId);
        transaction.setComment(comment);
        transactionDao.save(transaction);

        final long transactionId = transaction.getId();
        operations.forEach((accountId, amount) -> {
            final Operation operation = new Operation();
            operation.setTransactionId(transactionId);
            operation.setAccountId(accountId);
            operation.setAmount(amount);
            operationDao.save(operation);
        });

        return transaction.getId();
    }

    @Override
    public void remove(Transaction transaction) {
        operationDao.get(transaction.getId())
                .forEach(operationDao::remove);
        transactionDao.remove(transaction);
    }

}
