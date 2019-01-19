package com.lonebytesoft.hamster.accounting.service.currency;

import com.lonebytesoft.hamster.accounting.model.Currency;
import com.lonebytesoft.hamster.accounting.repository.CurrencyRepository;
import com.lonebytesoft.hamster.accounting.service.config.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class CurrencyServiceImpl implements CurrencyService {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyServiceImpl.class);

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
    public void updateCurrencyValues() {
        final Map<String, Currency> currencies = getKnownCurrencies()
                .stream()
                .collect(Collectors.toMap(Currency::getCode, Function.identity()));
        currencyRepository.saveAll(StreamSupport.stream(currencyRepository.findAll().spliterator(), false)
                .peek(currency -> {
                    final String code = currency.getCode();
                    final Currency reference = currencies.get(code);
                    if (reference != null) {
                        currency.setValue(reference.getValue());
                    } else {
                        logger.warn("Currency not known: " + code);
                    }
                })
                .collect(Collectors.toList())
        );
    }

    @Override
    public void updateCurrencyRates() {
        currencyRateProvider.getCurrencies(false);
    }

    @Override
    public double convert(Currency from, Currency to, double amount) {
        if(from.getId() == to.getId()) {
            return amount;
        }

        final double amountBase = amount * from.getValue();
        return amountBase / to.getValue();
    }

    @Override
    public Collection<Currency> getKnownCurrencies() {
        final Collection<Currency> currencies = currencyRateProvider.getCurrencies(true);

        final String baseCode = configService.get().getCurrencyDefault().getCode();
        final double baseValue = currencies
                .stream()
                .filter(currency -> currency.getCode().equalsIgnoreCase(baseCode))
                .findFirst()
                .map(Currency::getValue)
                .orElseGet(() -> {
                    logger.warn("Base currency not known: " + baseCode);
                    return 1.0;
                });

        return currencies
                .stream()
                .map(currency -> new Currency(
                        0,
                        currency.getCode(),
                        currency.getName().isEmpty() ? currency.getCode() : currency.getName(),
                        currency.getSymbol().isEmpty() ? currency.getCode() : currency.getSymbol(),
                        currency.getValue() / baseValue
                ))
                .collect(Collectors.toList());
    }

}
