package com.lonebytesoft.hamster.accounting.service.currency.bankofrussia;

import com.lonebytesoft.hamster.accounting.model.Currency;
import com.lonebytesoft.hamster.accounting.service.currency.CurrencyRateProvider;
import com.lonebytesoft.hamster.accounting.service.date.DateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class BankOfRussiaCurrencyRateProviderImpl implements CurrencyRateProvider {

    private static final Logger logger = LoggerFactory.getLogger(BankOfRussiaCurrencyRateProviderImpl.class);

    private static final String URL_RATES = "https://www.cbr.ru/scripts/XML_daily.asp";
    private static final String URL_CURRENCIES = "https://www.cbr.ru/scripts/XML_valFull.asp";

    private static final String CODE_DEFAULT = "RUB";
    private static final double VALUE_DEFAULT = 1.0;
    private static final String NAME_RU_DEFAULT = "Российский рубль";
    private static final String NAME_EN_DEFAULT = "Russian rouble";

    private static final ThreadLocal<NumberFormat> VALUE_FORMAT =
            ThreadLocal.withInitial(() -> NumberFormat.getInstance(new Locale("ru", "RU")));

    private final DateService dateService;
    private final Unmarshaller unmarshaller;

    private final URL urlRates;
    private final URL urlCurrencies;

    private final BankOfRussiaCurrencyNameLanguage currencyNameLanguage;

    private final Map<Long, Collection<Currency>> currenciesCache = new ConcurrentHashMap<>();

    @Autowired
    public BankOfRussiaCurrencyRateProviderImpl(
            final DateService dateService,
            @Value("${accounting.currency.bankofrussia.language:ru}") final BankOfRussiaCurrencyNameLanguage currencyNameLanguage
    ) {
        this.dateService = dateService;
        this.unmarshaller = buildUnmarshaller();

        this.urlRates = buildUrl(URL_RATES, "Bad configuration: could not parse rates URL '" + URL_RATES + "'");
        this.urlCurrencies = buildUrl(URL_CURRENCIES, "Bad configuration: could not parse currencies URL '" + URL_CURRENCIES + "'");

        this.currencyNameLanguage = currencyNameLanguage;
    }

    private Unmarshaller buildUnmarshaller() {
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(
                    BankOfRussiaRates.class,
                    BankOfRussiaCurrencies.class
            );
            return jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            throw new IllegalStateException("Bad configuration: could not create XML unmarshaller", e);
        }
    }

    private URL buildUrl(final String url, String failureMessage) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalStateException(failureMessage, e);
        }
    }

    @Override
    public Collection<Currency> getCurrencies(boolean useCache) {
        try {
            if (!useCache) {
                currenciesCache.clear();
            }

            return currenciesCache.computeIfAbsent(
                    dateService.calculateDayStart(System.currentTimeMillis(), 0),
                    time -> requestCurrencies()
            );
        } catch (BankOfRussiaException e) {
            if(e.getCause() == null) {
                logger.warn(e.getMessage());
            } else {
                logger.warn(e.getMessage(), e.getCause());
            }
            return Collections.emptyList();
        }
    }

    private Collection<Currency> requestCurrencies() {
        final BankOfRussiaRates ratesResponse = requestData(urlRates, "Could not obtain rates information");
        final BankOfRussiaCurrencies currenciesResponse = requestData(urlCurrencies, "Could not obtain currencies information");

        final Collection<Currency> currencies = ratesResponse.getRates()
                .stream()
                .map(rate -> {
                    final String code = rate.getIsoCode();
                    if ((code == null) || code.isEmpty()) {
                        throw new BankOfRussiaException("Illegal currency ISO code in " + rate);
                    }

                    return new Currency(
                            0,
                            code,
                            emptyIfNull(calculateName(code, currenciesResponse.getCurrencies(), currencyNameLanguage)),
                            "",
                            calculateValue(rate)
                    );
                })
                .collect(Collectors.toSet());

        currencies.add(new Currency(
                0,
                CODE_DEFAULT,
                emptyIfNull(chooseName(currencyNameLanguage, NAME_RU_DEFAULT, NAME_EN_DEFAULT)),
                "",
                VALUE_DEFAULT
        ));

        return currencies;
    }

    @SuppressWarnings("unchecked")
    private <T> T requestData(final URL url, final String failureMessage) {
        logger.debug("Requesting {}", url);
        try {
            return (T) unmarshaller.unmarshal(url);
        } catch (JAXBException e) {
            throw new BankOfRussiaException(failureMessage, e);
        }
    }

    private double calculateValue(final BankOfRussiaRate rate) {
        final NumberFormat valueFormat = VALUE_FORMAT.get();
        final double value;
        final double multiplier;
        try {
            value = valueFormat.parse(rate.getValue()).doubleValue();
            multiplier = valueFormat.parse(rate.getMultiplier()).doubleValue();
        } catch (ParseException e) {
            throw new BankOfRussiaException("Could not parse rates information in " + rate, e);
        }

        if(value <= 0) {
            throw new BankOfRussiaException("Illegal currency rate value in " + rate);
        }
        if(multiplier <= 0) {
            throw new BankOfRussiaException("Illegal currency rate multiplier in " + rate);
        }

        return value / multiplier;
    }

    private String calculateName(
            final String code,
            final Collection<BankOfRussiaCurrency> currencies,
            final BankOfRussiaCurrencyNameLanguage language
    ) {
        final BankOfRussiaCurrency currency = currencies
                .stream()
                .filter(item -> code.equalsIgnoreCase(item.getIsoCode()))
                .findFirst()
                .orElseThrow(() -> new BankOfRussiaException("Could not find currency information for '" + code + "'"));
        return chooseName(language, currency.getNameRu(), currency.getNameEn());
    }

    private String chooseName(final BankOfRussiaCurrencyNameLanguage language, final String nameRu, final String nameEn) {
        switch (language) {
            case RU: return nameRu;
            case EN: return nameEn;
            default: throw new IllegalStateException("Unsupported language: " + language);
        }
    }

    private String emptyIfNull(final String s) {
        return s == null ? "" : s;
    }

    private static class BankOfRussiaException extends RuntimeException {
        public BankOfRussiaException(String message) {
            super(message);
        }

        public BankOfRussiaException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
