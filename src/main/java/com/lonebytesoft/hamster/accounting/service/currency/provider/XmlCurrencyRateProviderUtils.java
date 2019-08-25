package com.lonebytesoft.hamster.accounting.service.currency.provider;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class XmlCurrencyRateProviderUtils {

    public static Unmarshaller buildUnmarshaller(final Class... classes) {
        return buildUnmarshaller(
                classes,
                () -> "Bad configuration: could not create XML unmarshaller for [" +
                        Arrays.stream(classes).map(Class::getSimpleName).collect(Collectors.joining(", ")) + "]"
        );
    }

    public static Unmarshaller buildUnmarshaller(final Class[] classes, final Supplier<String> failureMessage) {
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(classes);
            return jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            throw new IllegalStateException(failureMessage.get(), e);
        }
    }

    public static URL buildUrl(final String url, final Supplier<String> failureMessage) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalStateException(failureMessage.get(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T requestData(final Unmarshaller unmarshaller, final URL url, final Supplier<String> failureMessage) {
        try {
            return (T) unmarshaller.unmarshal(url);
        } catch (JAXBException e) {
            throw new CurrencyRateProviderException(failureMessage.get(), e);
        }
    }

}
