package com.lonebytesoft.hamster.accounting.controller.view.converter;

import com.lonebytesoft.hamster.accounting.controller.view.input.AccountInputView;
import com.lonebytesoft.hamster.accounting.controller.view.output.AccountView;
import com.lonebytesoft.hamster.accounting.model.Account;
import com.lonebytesoft.hamster.accounting.model.Currency;
import com.lonebytesoft.hamster.accounting.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccountViewConverter implements ModelViewConverter<Account, AccountInputView, AccountView> {

    private final CurrencyRepository currencyRepository;

    @Autowired
    public AccountViewConverter(final CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    @Override
    public Account populateFromInput(Account base, AccountInputView input) {
        base.setName(input.getName());
        base.setVisible(input.getVisible());

        final Currency currency = currencyRepository.findById(input.getCurrencyId())
                .orElseThrow(() -> new IllegalArgumentException("Could not find currency, id=" + input.getCurrencyId()));
        base.setCurrency(currency);

        return base;
    }

    @Override
    public AccountView convertToOutput(Account model) {
        return new AccountView(
                model.getId(),
                model.getName(),
                model.getCurrency().getId(),
                model.getOrdering(),
                model.getVisible()
        );
    }

}
