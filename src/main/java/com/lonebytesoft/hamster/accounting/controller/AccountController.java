package com.lonebytesoft.hamster.accounting.controller;

import com.lonebytesoft.hamster.accounting.controller.view.converter.ModelViewConverter;
import com.lonebytesoft.hamster.accounting.controller.view.input.AccountInputView;
import com.lonebytesoft.hamster.accounting.controller.view.output.AccountView;
import com.lonebytesoft.hamster.accounting.controller.view.output.AccountsView;
import com.lonebytesoft.hamster.accounting.controller.view.output.ActionResultView;
import com.lonebytesoft.hamster.accounting.controller.view.output.ActionStatus;
import com.lonebytesoft.hamster.accounting.model.Account;
import com.lonebytesoft.hamster.accounting.model.Operation;
import com.lonebytesoft.hamster.accounting.model.OrderedUtils;
import com.lonebytesoft.hamster.accounting.model.Transaction;
import com.lonebytesoft.hamster.accounting.repository.AccountRepository;
import com.lonebytesoft.hamster.accounting.repository.OperationRepository;
import com.lonebytesoft.hamster.accounting.service.EntityAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/account")
public class AccountController {

    private final AccountRepository accountRepository;
    private final OperationRepository operationRepository;
    private final ModelViewConverter<Account, AccountInputView, AccountView> viewConverter;

    @Autowired
    public AccountController(
            final AccountRepository accountRepository,
            final OperationRepository operationRepository,
            final ModelViewConverter<Account, AccountInputView, AccountView> viewConverter
    ) {
        this.accountRepository = accountRepository;
        this.operationRepository = operationRepository;
        this.viewConverter = viewConverter;
    }

    @RequestMapping(method = RequestMethod.GET)
    public AccountsView getAccounts() {
        return new AccountsView(
                StreamSupport.stream(accountRepository.findAll().spliterator(), false)
                        .map(viewConverter::convertToOutput)
                        .collect(Collectors.toList())
        );
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
    public AccountView addAccount(
            @RequestBody final AccountInputView accountInputView
    ) {
        Account account = viewConverter.populateFromInput(new Account(), accountInputView);
        account.setOrdering(OrderedUtils.getMaxOrder(accountRepository.findAll()) + 1);

        account = accountRepository.save(account);
        return viewConverter.convertToOutput(account);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/{id}", consumes = "application/json")
    public AccountView modifyAccount(
            @PathVariable final long id,
            @RequestBody final AccountInputView accountInputView
    ) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Could not find account, id=" + id));

        account = viewConverter.populateFromInput(account, accountInputView);
        account = accountRepository.save(account);
        return viewConverter.convertToOutput(account);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/{id}/{action}")
    public ActionResultView performAccountAction(
            @PathVariable final long id,
            @PathVariable final EntityAction action
    ) {
        final Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Could not find account, id=" + id));

        switch(action) {
            case MOVE_UP:
                final Account firstBefore = accountRepository.findFirstBefore(account.getOrdering());
                OrderedUtils.swapOrder(account, firstBefore);
                accountRepository.saveAll(
                        Stream.of(account, firstBefore)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())
                );
                break;

            case MOVE_DOWN:
                final Account firstAfter = accountRepository.findFirstAfter(account.getOrdering());
                OrderedUtils.swapOrder(account, firstAfter);
                accountRepository.saveAll(
                        Stream.of(account, firstAfter)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())
                );
                break;

            case DELETE:
                deleteAccount(account);
                break;

            default:
                throw new UnsupportedOperationException(action.getParamValue());
        }

        return new ActionResultView(ActionStatus.SUCCESS, "");
    }

    private void deleteAccount(final Account account) {
        final Collection<Operation> operations = operationRepository.findByAccount(account);
        if(!operations.isEmpty()) {
            throw new IllegalArgumentException(
                    "Account has some operations in the following transactions: " + Transaction.toUserString(
                            operations.stream()
                                    .map(Operation::getTransaction)
                                    .distinct()
                                    .collect(Collectors.toList())
                    )
            );
        }

        accountRepository.delete(account);
    }

}
