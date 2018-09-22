package com.lonebytesoft.hamster.accounting.service.currency;

import com.lonebytesoft.hamster.accounting.model.Currency;
import com.lonebytesoft.hamster.accounting.repository.CurrencyRepository;
import com.lonebytesoft.hamster.accounting.service.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final ConfigService configService;
    private final CurrencyRateProvider currencyRateProvider;

    @Autowired
    public CurrencyServiceImpl(final CurrencyRepository currencyRepository,
                               final ConfigService configService,
                               final CurrencyRateProvider currencyRateProvider) {
        this.currencyRepository = currencyRepository;
        this.configService = configService;
        this.currencyRateProvider = currencyRateProvider;
    }

    @Override
    public double getActualCurrencyValue(Currency currency) {
        final Currency base = configService.get().getCurrencyDefault();
        final Double rate = currencyRateProvider.getCurrencyRate(currency.getCode(), base.getCode());
        return rate == null ? currency.getValue() : rate;
    }

    @Override
    public void updateCurrencyValues() {
        currencyRepository.save(StreamSupport.stream(currencyRepository.findAll().spliterator(), false)
                .peek(currency -> currency.setValue(getActualCurrencyValue(currency)))
                .collect(Collectors.toList())
        );
    }

    @Override
    public void updateCurrencyRates() {
        if(currencyRateProvider instanceof CurrencyRateCachingProvider) {
            ((CurrencyRateCachingProvider) currencyRateProvider).invalidateCache();
        }
    }

    @Override
    public double convert(Currency from, Currency to, double amount) {
        if(from.getId() == to.getId()) {
            return amount;
        }

        final double amountBase = amount * from.getValue();
        return amountBase / to.getValue();
    }

}
