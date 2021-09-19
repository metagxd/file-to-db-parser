package com.metagxd.filetodbparser.dbtranser.xml;

import com.metagxd.filetodbparser.db.creator.table.TableCreator;
import com.metagxd.filetodbparser.db.saver.DbSaver;
import com.metagxd.filetodbparser.dbtranser.DbTransfer;
import com.metagxd.filetodbparser.factory.dbconnection.DbConnectionFactory;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static javax.xml.stream.XMLStreamConstants.*;

@Component
public class XMLDbTransfer implements DbTransfer {

    private final DbConnectionFactory connectionFactory;
    private final DbSaver<List<String[]>> dbSaver;
    private final XMLStreamReaderFactory readerFactory;
    private final TableCreator tableCreator;

    @Value("${database.table.name}")
    private String tableName;
    @Value("${transfer.batch.size:#{100}}")
    private int batchSize;

    private final List<String[]> nodeList = new ArrayList<>();

    private static final Logger logger = LoggerFactory.getLogger(XMLDbTransfer.class);

    public XMLDbTransfer(DbConnectionFactory connectionFactory, DbSaver<List<String[]>> dbSaver,
                         XMLStreamReaderFactory readerFactory, TableCreator tableCreator) {
        this.connectionFactory = connectionFactory;
        this.dbSaver = dbSaver;
        this.readerFactory = readerFactory;
        this.tableCreator = tableCreator;
    }

    public void transferToDb(String fileName, String elementName, String... nodeNames) {
        logger.info("Start reading file {}", fileName);
        var path = Paths.get(fileName);
        if (!Files.exists(path)) {
            logger.error("File {} not exist!", fileName);
            return;
        }
        try (readerFactory) {
            var connection = connectionFactory.getConnection();
            tableCreator.createTable(connection, tableName, nodeNames);
            XMLStreamReader reader = readerFactory.getReader(Files.newInputStream(path));
            //create storage for elements
            var nodeData = new String[nodeNames.length];
            //populate object from xml
            while (reader.hasNext()) {       // while not end of XML
                int event = reader.next();   // read next event
                if (event == START_ELEMENT) {
                    String nodeName = reader.getLocalName();
                    if (Arrays.asList(nodeNames).contains(nodeName)) {      //order of nodes is not important
                        saveToNodeData(nodeData, reader, nodeNames, nodeName);
                    }
                }

                //if reach end of node save node values to nodeList and create new nodeData
                if (event == END_ELEMENT && elementName.equals(reader.getLocalName())) {
                    nodeList.add(nodeData);
                    nodeData = new String[nodeNames.length];
                }
                //if batch size reached the limit push to DB, else if end of document reached
                if (nodeList.size() >= batchSize || event == END_DOCUMENT) {
                    dbSaver.save(connection, nodeList);
                    nodeList.clear();
                }
            }
        } catch (XMLStreamException | IOException e) {
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

}
