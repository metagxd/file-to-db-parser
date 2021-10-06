package com.metagxd.filetodbparser.dbtranser.xml;

import com.metagxd.filetodbparser.dbtranser.DbTransfer;
import com.metagxd.filetodbparser.factory.reader.xml.XMLReaderFactory;
import com.metagxd.filetodbparser.factory.reader.xml.XMLStreamReaderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

@Component
public class XMLDbTransfer implements DbTransfer {

    private final XMLReaderFactory<XMLStreamReader> readerFactory;
    private final BlockingQueue<String[]> nodeStorage;
    private final String fileName;
    private final String parentNodeName;
    private final String[] nodeNames;

    private static final Logger logger = LoggerFactory.getLogger(XMLDbTransfer.class);

    public XMLDbTransfer(XMLStreamReaderFactory readerFactory, BlockingQueue<String[]> nodeStorage,
                         @Value("${transfer.file.name}") String fileName,
                         @Value("${transfer.parent.node.name}") String parentNodeName,
                         @Value("${transfer.child.node.names}") String... nodeNames) {
        this.readerFactory = readerFactory;
        this.nodeStorage = nodeStorage;
        this.fileName = fileName;
        this.parentNodeName = parentNodeName;
        this.nodeNames = nodeNames;
    }

    public void transferToDb() {
        var path = Paths.get(fileName);
        if (!Files.exists(path)) {
            logger.error("File {} not exist!", fileName);
            return;
        }
        try (readerFactory) {
            XMLStreamReader reader = readerFactory.getReader(Files.newInputStream(path));
            //create storage for elements
            var nodeData = new String[nodeNames.length];
            logger.info("Start reading file {}", fileName);
            //populate object from xml
            while (reader.hasNext()) {       // while not end of XML
                int event = reader.next();   // read next event
                if (event == START_ELEMENT) {
                    String nodeName = reader.getLocalName();
                    if (Arrays.asList(nodeNames).contains(nodeName)) {      //order of nodes is not important
                        saveToNodeData(nodeData, reader, nodeNames, nodeName);
                    }
                }

                //if reach end of node save node values to nodeStorage and create new nodeData
                if (event == END_ELEMENT && parentNodeName.equals(reader.getLocalName())) {
                    nodeStorage.put(nodeData);
                    nodeData = new String[nodeNames.length];
                }
            }
            Thread.currentThread().interrupt();
        } catch (XMLStreamException | IOException | InterruptedException e) {
            logger.error("Transfer error:", e);
        }
    }

    //save node value in right order
    private void saveToNodeData(String[] nodeData, XMLStreamReader reader, String[] nodeNames,
                                String nodeName) throws XMLStreamException {
        for (var i = 0; i < nodeNames.length; i++) {
            if (nodeNames[i].equalsIgnoreCase(nodeName)) {
                nodeData[i] = reader.getElementText();
            }
        }
    }

    @Override
    public void run() {
        transferToDb();
    }
}
