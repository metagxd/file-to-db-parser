package com.metagxd.filetodbparser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


//FIXME: bad implementation
public class XmlPopulator {
    //set how many nodes need in xml
    private static final int NUMBER_OF_NODES = 5_568_352;
    private static final String filename = "src/test/java/com/metagxd/filetodbparser/test-data/testData.xml";

    public static void main(String[] args) {

        try {
            Path filePath = Paths.get(filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            // write XML to ByteArrayOutputStream
            writeXml(out);

            // standard way to convert byte array to String
            String xml = out.toString(StandardCharsets.UTF_8);

            String formatXML = formatXML(xml);

            Files.writeString(filePath,
                    formatXML, StandardCharsets.UTF_8);

        } catch (TransformerException | XMLStreamException | IOException e) {
            e.printStackTrace();
        }

    }

    //create formatted xml file with random data
    private static void writeXml(OutputStream out) throws XMLStreamException {

        XMLOutputFactory output = XMLOutputFactory.newInstance();

        XMLStreamWriter writer = output.createXMLStreamWriter(out);

        writer.writeStartDocument("UTF-8", "1.0");

        writer.writeStartElement("companies");
        for (int i = 0; i < NUMBER_OF_NODES; i++) {
            // <company>

            writer.writeStartElement("company");

            writer.writeStartElement("name");
            writer.writeCharacters(getRandomString());
            writer.writeEndElement();

            writer.writeStartElement("city");
            writer.writeCharacters(getRandomString());
            writer.writeEndElement();

            writer.writeStartElement("foundation");
            writer.writeCharacters(String.valueOf(getRandomNumber(1900, 2005)));
            writer.writeEndElement();

            writer.writeEndElement();
            // </company>
        }
        writer.writeEndDocument();
        // </companies>
        writer.flush();
        writer.close();
    }

    //this bottleneck cause of out of mem
    private static String formatXML(String xml) throws TransformerException {

        // write data to xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        // pretty print by indention
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        // add standalone="yes", add line break before the root element
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");

        StreamSource source = new StreamSource(new StringReader(xml));
        StringWriter output = new StringWriter();
        transformer.transform(source, new StreamResult(output));

        return output.toString();

    }

    private static String getRandomString() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    private static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

}