package com.lonebytesoft.hamster.accounting.controller;

import com.lonebytesoft.hamster.accounting.controller.view.AccountView;
import com.lonebytesoft.hamster.accounting.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
public class AccountController {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountController(final AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/account")
    public Collection<AccountView> getAccounts() {
        return StreamSupport.stream(accountRepository.findAll().spliterator(), false)
                .map(account -> {
                    final AccountView accountView = new AccountView();
                    accountView.setId(account.getId());
                    accountView.setName(account.getName());
                    accountView.setCurrencyId(account.getCurrency().getId());
                    return accountView;
                })
                .collect(Collectors.toList());
    }

}
