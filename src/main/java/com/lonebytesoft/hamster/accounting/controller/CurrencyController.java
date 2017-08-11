package com.lonebytesoft.hamster.accounting.controller;

import com.lonebytesoft.hamster.accounting.controller.view.CurrencyView;
import com.lonebytesoft.hamster.accounting.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
public class CurrencyController {

    private final CurrencyRepository currencyRepository;

    @Autowired
    public CurrencyController(final CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/currency")
    public Collection<CurrencyView> getCurrencies() {
        return StreamSupport.stream(currencyRepository.findAll().spliterator(), false)
                .map(currency -> {
                    final CurrencyView currencyView = new CurrencyView();
                    currencyView.setId(currency.getId());
                    currencyView.setCode(currency.getCode());
                    currencyView.setName(currency.getName());
                    currencyView.setSymbol(currency.getSymbol());
                    currencyView.setValue(currency.getValue());
                    return currencyView;
                })
                .collect(Collectors.toList());
    }

}