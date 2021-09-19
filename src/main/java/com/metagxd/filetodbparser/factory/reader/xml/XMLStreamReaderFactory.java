package com.metagxd.filetodbparser.factory.reader.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

@Component
public class XMLStreamReaderFactory implements XMLReaderFactory<XMLStreamReader>, AutoCloseable {
    private static final XMLInputFactory FACTORY = XMLInputFactory.newInstance();
    private static final Logger logger = LoggerFactory.getLogger(XMLStreamReaderFactory.class);
    private XMLStreamReader reader;


    public XMLStreamReader getReader(InputStream inputStream) throws XMLStreamException {
        return FACTORY.createXMLStreamReader(inputStream);
    }

    @Override
    public void close() {
        if (reader != null) {
            try {
                reader.close();
            } catch (XMLStreamException e) {
                logger.error("Error occurred", e);
            }
        }
    }
}