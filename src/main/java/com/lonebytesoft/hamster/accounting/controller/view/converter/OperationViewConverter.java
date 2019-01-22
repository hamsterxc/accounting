package com.lonebytesoft.hamster.accounting.controller.view.converter;

import com.lonebytesoft.hamster.accounting.controller.exception.TransactionInputException;
import com.lonebytesoft.hamster.accounting.controller.view.input.OperationInputView;
import com.lonebytesoft.hamster.accounting.controller.view.output.OperationView;
import com.lonebytesoft.hamster.accounting.model.Account;
import com.lonebytesoft.hamster.accounting.model.Currency;
import com.lonebytesoft.hamster.accounting.model.Operation;
import com.lonebytesoft.hamster.accounting.repository.AccountRepository;
import com.lonebytesoft.hamster.accounting.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormatSymbols;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class OperationViewConverter implements ModelViewConverter<Operation, OperationInputView, OperationView> {

    private static final String OPERATION_AMOUNT_PATTERN_STRING = "((-+)?\\d+(" + DecimalFormatSymbols.getInstance().getDecimalSeparator() + "\\d+)?)";
    private static final Pattern OPERATION_AMOUNT_PATTERN = Pattern.compile(OPERATION_AMOUNT_PATTERN_STRING);
    private static final Pattern OPERATION_AMOUNT_CURRENCY_PATTERN = Pattern.compile(OPERATION_AMOUNT_PATTERN_STRING + "\\s+(\\w+)");

    private AccountRepository accountRepository;
    private CurrencyRepository currencyRepository;

    @Autowired
    public OperationViewConverter(
            final AccountRepository accountRepository,
            final CurrencyRepository currencyRepository
    ) {
        this.accountRepository = accountRepository;
        this.currencyRepository = currencyRepository;
    }

    @Override
    public Operation populateFromInput(Operation base, OperationInputView input) {
        final Account account = accountRepository.findById(input.getAccountId())
                .orElseThrow(() -> new TransactionInputException("Could not find account, id=" + input.getAccountId()));
        base.setAccount(account);

        final double amount;
        final Matcher matcherWithCurrency = OPERATION_AMOUNT_CURRENCY_PATTERN.matcher(input.getAmount());
        if(matcherWithCurrency.find()) {
            amount = Double.parseDouble(matcherWithCurrency.group(1));

            final String currencyCode = matcherWithCurrency.group(4);
            final Currency currency = currencyRepository.findByCode(currencyCode)
                    .orElseThrow(() -> new TransactionInputException("Could not find currency, code=" + currencyCode));
            base.setCurrency(currency);
        } else {
            final Matcher matcher = OPERATION_AMOUNT_PATTERN.matcher(input.getAmount());
            if(matcher.find()) {
                amount = Double.parseDouble(matcher.group(1));
            } else {
                throw new TransactionInputException("Could not parse amount string: " + input.getAmount());
            }
        }
        base.setAmount(amount);

        base.setActive(input.isActive());

        return base;
    }

    @Override
    public OperationView convertToOutput(Operation model) {
        return new OperationView(
                model.getAccount().getId(),
                model.getCurrency() == null ? null : model.getCurrency().getId(),
                model.getAmount(),
                model.isActive()
        );
    }

}
