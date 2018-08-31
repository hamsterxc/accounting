package com.lonebytesoft.hamster.accounting.controller.view.converter;

import com.lonebytesoft.hamster.accounting.controller.view.CurrencyInputView;
import com.lonebytesoft.hamster.accounting.controller.view.CurrencyView;
import com.lonebytesoft.hamster.accounting.model.Currency;
import com.lonebytesoft.hamster.accounting.service.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CurrencyViewConverter implements ModelViewConverter<Currency, CurrencyInputView, CurrencyView> {

    private final ConfigService configService;

    @Autowired
    public CurrencyViewConverter(final ConfigService configService) {
        this.configService = configService;
    }

    @Override
    public Currency populateFromInput(Currency base, CurrencyInputView input) {
        base.setCode(input.getCode());
        base.setName(input.getName());
        base.setSymbol(input.getSymbol());
        base.setValue(input.getValue());
        return base;
    }

    @Override
    public CurrencyView convertToOutput(Currency model) {
        final CurrencyView output = new CurrencyView();
        output.setId(model.getId());
        output.setCode(model.getCode());
        output.setName(model.getName());
        output.setSymbol(model.getSymbol());
        output.setValue(model.getValue());
        output.setDefault(model.getId() == configService.get().getCurrencyDefault().getId());
        return output;
    }

}
