package com.lonebytesoft.hamster.accounting.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

public class XmlManager {

    private static final Logger logger = LoggerFactory.getLogger(XmlManager.class);

    private final String xslPath;

    public XmlManager(final String xslPath) {
        if(xslPath.endsWith("/")) {
            this.xslPath = xslPath;
        } else {
            this.xslPath = xslPath + '/';
        }
    }

    /**
     * Returns an xml for given view
     * @param clazz view class, should be marshalable
     * @param object view
     * @param xslFilename XSL file name to use
     * @return transformed xml string
     * @throws RuntimeException if either JAXB or XSLT exception encountered
     */
    public <T> String transform(final Class<T> clazz, final T object, final String xslFilename) {
        final String marshaled;
        try {
            marshaled = marshal(clazz, object);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }

        logger.trace(marshaled);

        final String result;
        try {
            result = transform(xslFilename, marshaled);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }

        logger.trace(result);

        return result;
    }

    private <T> String marshal(final Class<T> clazz, final T object) throws JAXBException {
        final Writer writer = new StringWriter();

        final JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        final Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(object, writer);

        return writer.toString();
    }

    private String transform(final String xslFilename, final String xml) throws TransformerException {
        final Source sourceXsl = new StreamSource(XmlManager.class.getClassLoader().getResourceAsStream(xslFilename));

        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setURIResolver((href, base) -> {
            final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(xslPath + href);
            return new StreamSource(inputStream);
        });
        final Transformer transformer = transformerFactory.newTransformer(sourceXsl);

        final Source sourceXml = new StreamSource(new StringReader(xml));
        final Writer resultXmlWriter = new StringWriter();
        final Result resultXml = new StreamResult(resultXmlWriter);
        transformer.transform(sourceXml, resultXml);

        return resultXmlWriter.toString();
    }

}
