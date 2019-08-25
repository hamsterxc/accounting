package com.lonebytesoft.hamster.accounting.service.currency.provider;

import com.lonebytesoft.hamster.accounting.model.Currency;
import com.lonebytesoft.hamster.accounting.service.date.DateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractDailyCurrencyRateProvider implements CurrencyRateProvider {

    private static final Logger logger = LoggerFactory.getLogger(AbstractDailyCurrencyRateProvider.class);

    private final DateService dateService;

    private final Map<Long, Collection<Currency>> cache = new ConcurrentHashMap<>();

    protected AbstractDailyCurrencyRateProvider(final DateService dateService) {
        this.dateService = dateService;
    }

    @Override
    public Collection<Currency> getCurrencies(boolean useCache) {
        try {
            if (!useCache) {
                cache.clear();
            }

            return cache.computeIfAbsent(
                    dateService.calculateDayStart(System.currentTimeMillis(), 0),
                    time -> requestCurrencies()
            );
        } catch (CurrencyRateProviderException e) {
            if(e.getCause() == null) {
                logger.warn(e.getMessage());
            } else {
                logger.warn(e.getMessage(), e.getCause());
            }
            return Collections.emptyList();
        }
    }

    protected abstract Collection<Currency> requestCurrencies();

}
