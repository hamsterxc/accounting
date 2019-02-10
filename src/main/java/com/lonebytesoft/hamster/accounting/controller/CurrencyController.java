package com.lonebytesoft.hamster.accounting.controller;

import com.lonebytesoft.hamster.accounting.controller.view.converter.ModelViewConverter;
import com.lonebytesoft.hamster.accounting.controller.view.input.CurrencyInputView;
import com.lonebytesoft.hamster.accounting.controller.view.output.ActionResultView;
import com.lonebytesoft.hamster.accounting.controller.view.output.ActionStatus;
import com.lonebytesoft.hamster.accounting.controller.view.output.CurrenciesView;
import com.lonebytesoft.hamster.accounting.controller.view.output.CurrencyView;
import com.lonebytesoft.hamster.accounting.model.Account;
import com.lonebytesoft.hamster.accounting.model.Config;
import com.lonebytesoft.hamster.accounting.model.Currency;
import com.lonebytesoft.hamster.accounting.model.Operation;
import com.lonebytesoft.hamster.accounting.model.Transaction;
import com.lonebytesoft.hamster.accounting.repository.AccountRepository;
import com.lonebytesoft.hamster.accounting.repository.CurrencyRepository;
import com.lonebytesoft.hamster.accounting.repository.OperationRepository;
import com.lonebytesoft.hamster.accounting.service.config.ConfigService;
import com.lonebytesoft.hamster.accounting.service.currency.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/currency")
public class CurrencyController {

    private final CurrencyRepository currencyRepository;
    private final AccountRepository accountRepository;
    private final OperationRepository operationRepository;
    private final CurrencyService currencyService;
    private final ConfigService configService;
    private final ModelViewConverter<Currency, CurrencyInputView, CurrencyView> viewConverter;

    @Autowired
    public CurrencyController(
            final CurrencyRepository currencyRepository,
            final AccountRepository accountRepository,
            final OperationRepository operationRepository,
            final CurrencyService currencyService,
            final ConfigService configService,
            final ModelViewConverter<Currency, CurrencyInputView, CurrencyView> viewConverter
    ) {
        this.currencyRepository = currencyRepository;
        this.accountRepository = accountRepository;
        this.operationRepository = operationRepository;
        this.currencyService = currencyService;
        this.configService = configService;
        this.viewConverter = viewConverter;
    }

    @RequestMapping(method = RequestMethod.GET)
    public CurrenciesView getCurrencies() {
        currencyService.updateCurrencyValues();
        return new CurrenciesView(
                StreamSupport.stream(currencyRepository.findAll().spliterator(), false)
                        .map(viewConverter::convertToOutput)
                        .collect(Collectors.toList())
        );
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
    public CurrencyView addCurrency(
            @RequestBody final CurrencyInputView currencyInputView
    ) {
        Currency currency = viewConverter.populateFromInput(new Currency(), currencyInputView);
        currency = saveAndUpdate(currency, currencyInputView.isDefault());

        return viewConverter.convertToOutput(currency);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/{id}", consumes = "application/json")
    public CurrencyView modifyCurrency(
            @PathVariable final long id,
            @RequestBody final CurrencyInputView currencyInputView
    ) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Could not find currency, id=" + id));

        currency = viewConverter.populateFromInput(currency, currencyInputView);
        currency = saveAndUpdate(currency, currencyInputView.isDefault());

        return viewConverter.convertToOutput(currency);
    }

    private Currency saveAndUpdate(final Currency currency, final boolean isDefault) {
        Currency entity = currencyRepository.save(currency);

        currencyService.updateCurrencyRates();
        currencyService.updateCurrencyValues();
        final long currencyId = entity.getId();
        entity = currencyRepository.findById(currencyId)
                .orElseThrow(() -> new IllegalStateException("Could not find currency, id=" + currencyId));

        if(isDefault) {
            final Config config = configService.get();
            config.setCurrencyDefault(entity);
            configService.save(config);
        }

        return entity;
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/{id}")
    public ActionResultView deleteCurrency(
            @PathVariable final long id
    ) {
        final Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Could not find currency, id=" + id));

        if(currency.getId() == configService.get().getCurrencyDefault().getId()) {
            throw new IllegalArgumentException("Cannot delete default currency");
        }

        final Collection<Account> accounts = accountRepository.findByCurrency(currency);
        if(!accounts.isEmpty()) {
            throw new IllegalArgumentException(
                    "Currency is used by the following accounts: " + Account.toUserString(accounts)
            );
        }

        final Collection<Operation> operations = operationRepository.findByCurrency(currency);
        if(!operations.isEmpty()) {
            throw new IllegalArgumentException(
                    "Currency is used by some operations in the following transactions: " + Transaction.toUserString(
                            operations.stream()
                                    .map(Operation::getTransaction)
                                    .distinct()
                                    .collect(Collectors.toList())
                    )
            );
        }

        currencyRepository.delete(currency);

        return new ActionResultView(ActionStatus.SUCCESS, "");
    }

}
