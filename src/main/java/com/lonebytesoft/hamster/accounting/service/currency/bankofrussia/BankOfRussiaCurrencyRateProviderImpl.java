package com.lonebytesoft.hamster.accounting.service.currency.bankofrussia;

import com.lonebytesoft.hamster.accounting.service.currency.CurrencyRateCachingProvider;
import com.lonebytesoft.hamster.accounting.service.currency.CurrencyRateProvider;
import com.lonebytesoft.hamster.accounting.service.date.DateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class BankOfRussiaCurrencyRateProviderImpl implements CurrencyRateProvider, CurrencyRateCachingProvider {

    private static final Logger logger = LoggerFactory.getLogger(BankOfRussiaCurrencyRateProviderImpl.class);

    private static final String API_URL = "http://www.cbr.ru/scripts/XML_daily.asp";

    private static final String ISO_CODE_DEFAULT = "RUB";
    private static final double RATE_DEFAULT = 1.0;

    private static final ThreadLocal<NumberFormat> VALUE_FORMAT =
            ThreadLocal.withInitial(() -> NumberFormat.getInstance(new Locale("ru", "RU")));

    private final Unmarshaller unmarshaller;
    private final URL apiUrl;
    private final DateService dateService;

    private final Map<Long, Map<String, Double>> dailyRates = new ConcurrentHashMap<>();

    @Autowired
    public BankOfRussiaCurrencyRateProviderImpl(final DateService dateService) {
        this.dateService = dateService;

        try {
            apiUrl = new URL(API_URL);
        } catch (MalformedURLException e) {
            logger.error("Bad configuration: could not parse API URL '" + API_URL + "'");
            throw new IllegalStateException(e);
        }

        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(Rates.class);
            unmarshaller = jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            logger.error("Bad configuration: could not create XML unmarshaller");
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Double getCurrencyRate(String isoCodeSubject, String isoCodeBase) {
        final long dayStart = dateService.calculateDayStart(System.currentTimeMillis(), 0);

        try {
            final Map<String, Double> rates = dailyRates.computeIfAbsent(dayStart, time -> {
                final Date timeToLog = new Date(time);
                logger.debug("Getting currency rates on {}", timeToLog);

                final Rates ratesResponse;
                try {
                    ratesResponse = (Rates) unmarshaller.unmarshal(apiUrl);
                } catch (JAXBException e) {
                    throw new BankOfRussiaException("Could not obtain rates information", e);
                }
                logger.trace("Got currency rates on {}: {}", timeToLog, ratesResponse);

                return ratesResponse.getRates()
                        .stream()
                        .collect(Collectors.toMap(Rate::getIsoCode, rate -> {
                            final NumberFormat valueFormat = VALUE_FORMAT.get();
                            final double value;
                            final double multiplier;
                            try {
                                 value = valueFormat.parse(rate.getValue()).doubleValue();
                                 multiplier = valueFormat.parse(rate.getMultiplier()).doubleValue();
                            } catch (ParseException e) {
                                throw new BankOfRussiaException("Could not parse rates information", e);
                            }

                            if(value <= 0) {
                                throw new BankOfRussiaException("Illegal currency rate value: " + value);
                            }
                            if(multiplier <= 0) {
                                throw new BankOfRussiaException("Illegal currency rate multiplier: " + multiplier);
                            }

                            return value / multiplier;
                        }));
            });

            final double rateSubject = getRate(rates, isoCodeSubject);
            final double rateBase = getRate(rates, isoCodeBase);
            return rateSubject / rateBase;
        } catch (BankOfRussiaException e) {
            if(e.getCause() == null) {
                logger.warn(e.getMessage());
            } else {
                logger.warn(e.getMessage(), e.getCause());
            }
            return null;
        }
    }

    private double getRate(final Map<String, Double> rates, final String isoCode) {
        return ISO_CODE_DEFAULT.equals(isoCode)
                ? RATE_DEFAULT
                : Optional.ofNullable(rates.get(isoCode)).orElseThrow(() -> new BankOfRussiaException("Unknown currency ISO code: " + isoCode));
    }

    @Override
    public void invalidateCache() {
        final long dayStart = dateService.calculateDayStart(System.currentTimeMillis(), 0);
        dailyRates.remove(dayStart);
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
