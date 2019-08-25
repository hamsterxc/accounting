package com.lonebytesoft.hamster.accounting.service.currency.provider.ecb;

import com.lonebytesoft.hamster.accounting.model.Currency;
import com.lonebytesoft.hamster.accounting.service.currency.provider.AbstractDailyCurrencyRateProvider;
import com.lonebytesoft.hamster.accounting.service.currency.provider.XmlCurrencyRateProviderUtils;
import com.lonebytesoft.hamster.accounting.service.date.DateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.Unmarshaller;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class EuropeanCentralBankCurrencyRateProviderImpl extends AbstractDailyCurrencyRateProvider {

    private static final Logger logger = LoggerFactory.getLogger(EuropeanCentralBankCurrencyRateProviderImpl.class);

    private static final String URL_RATES = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";

    private static final String CODE_DEFAULT = "EUR";
    private static final double VALUE_DEFAULT = 1.0;

    private final Unmarshaller unmarshaller;
    private final URL urlRates;

    @Autowired
    public EuropeanCentralBankCurrencyRateProviderImpl(final DateService dateService) {
        super(dateService);

        this.unmarshaller = XmlCurrencyRateProviderUtils.buildUnmarshaller(EuropeanCentralBankEnvelope.class);
        this.urlRates = XmlCurrencyRateProviderUtils.buildUrl(
                URL_RATES,
                () -> "Bad configuration: could not parse rates URL '" + URL_RATES + "'"
        );
    }

    @Override
    protected Collection<Currency> requestCurrencies() {
        final EuropeanCentralBankEnvelope response = requestData(urlRates, "Could not obtain rates information");

        final Collection<Currency> currencies = response.getData().getCurrencies().getRates()
                .stream()
                .map(currency -> new Currency(0, currency.getCode(), "", "", VALUE_DEFAULT / Double.parseDouble(currency.getValue())))
                .collect(Collectors.toCollection(HashSet::new));
        currencies.add(new Currency(0, CODE_DEFAULT, "", "", VALUE_DEFAULT));

        return currencies;
    }

    private <T> T requestData(final URL url, final String failureMessage) {
        logger.debug("Requesting {}", url);
        return XmlCurrencyRateProviderUtils.requestData(unmarshaller, url, () -> failureMessage);
    }

}
