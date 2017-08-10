package com.lonebytesoft.hamster.accounting;

import com.lonebytesoft.hamster.accounting.model.Account;
import com.lonebytesoft.hamster.accounting.model.Category;
import com.lonebytesoft.hamster.accounting.model.Operation;
import com.lonebytesoft.hamster.accounting.model.Transaction;
import com.lonebytesoft.hamster.accounting.view.AccountView;
import com.lonebytesoft.hamster.accounting.view.CategoryView;
import com.lonebytesoft.hamster.accounting.view.OperationView;
import com.lonebytesoft.hamster.accounting.view.TransactionView;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class ModelViewMapper {

    public AccountView mapAccount(final Account account) {
        final AccountView accountView = new AccountView();

        accountView.setId(account.getId());
        accountView.setName(account.getName());
        accountView.setCurrencySymbol(account.getCurrency().getSymbol());

        return accountView;
    }

    public CategoryView mapCategory(final Category category) {
        final CategoryView categoryView = new CategoryView();

        categoryView.setId(category.getId());
        categoryView.setName(category.getName());

        return categoryView;
    }

    public TransactionView mapTransaction(final Transaction transaction) {
        final TransactionView transactionView = new TransactionView();

        transactionView.setId(transaction.getId());
        transactionView.setTime(transaction.getTime());
        transactionView.setComment(transaction.getComment());
        transactionView.setCategory(transaction.getCategory().getName());

        final Collection<OperationView> operationViews = transaction.getOperations()
                .stream()
                .map(this::mapOperation)
                .collect(Collectors.toList());
        transactionView.setOperations(operationViews);

        return transactionView;
    }

    public OperationView mapOperation(final Operation operation) {
        final OperationView operationView = new OperationView();

        operationView.setId(operation.getAccount().getId());
        operationView.setAmount(operation.getAmount());

        return operationView;
    }

}
