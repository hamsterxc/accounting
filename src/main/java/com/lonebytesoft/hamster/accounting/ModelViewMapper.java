package com.lonebytesoft.hamster.accounting;

import com.lonebytesoft.hamster.accounting.model.Account;
import com.lonebytesoft.hamster.accounting.model.Category;
import com.lonebytesoft.hamster.accounting.model.Currency;
import com.lonebytesoft.hamster.accounting.model.Operation;
import com.lonebytesoft.hamster.accounting.model.Transaction;
import com.lonebytesoft.hamster.accounting.util.Utils;
import com.lonebytesoft.hamster.accounting.view.AccountView;
import com.lonebytesoft.hamster.accounting.view.CategoryView;
import com.lonebytesoft.hamster.accounting.view.OperationView;
import com.lonebytesoft.hamster.accounting.view.TransactionView;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class ModelViewMapper {

    public AccountView mapAccount(final Account account, final Map<Long, Currency> currencies) {
        final AccountView accountView = new AccountView();

        accountView.setId(account.getId());
        accountView.setName(account.getName());

        final Currency currency = currencies.computeIfAbsent(
                account.getCurrencyId(), Utils.obtainNotFoundFunction("currency"));
        accountView.setCurrencySymbol(currency.getSymbol());

        return accountView;
    }

    public CategoryView mapCategory(final Category category) {
        final CategoryView categoryView = new CategoryView();

        categoryView.setId(category.getId());
        categoryView.setName(category.getName());

        return categoryView;
    }

    public TransactionView mapTransaction(final Transaction transaction, final Collection<Operation> operations,
                                          final Map<Long, Account> accounts, final Map<Long, Category> categories) {
        final TransactionView transactionView = new TransactionView();

        transactionView.setId(transaction.getId());
        transactionView.setTime(transaction.getTime());
        transactionView.setComment(transaction.getComment());

        final Category category = categories.computeIfAbsent(
                transaction.getCategoryId(), Utils.obtainNotFoundFunction("category"));
        transactionView.setCategory(category.getName());

        final Collection<OperationView> operationViews = operations
                .stream()
                .map(operation -> mapOperation(operation, accounts))
                .collect(Collectors.toList());
        transactionView.setOperations(operationViews);

        return transactionView;
    }

    public OperationView mapOperation(final Operation operation, final Map<Long, Account> accounts) {
        final OperationView operationView = new OperationView();

        final Account account = accounts.computeIfAbsent(
                operation.getAccountId(), Utils.obtainNotFoundFunction("account"));
        operationView.setId(account.getId());

        operationView.setAmount(operation.getAmount());

        return operationView;
    }

}
