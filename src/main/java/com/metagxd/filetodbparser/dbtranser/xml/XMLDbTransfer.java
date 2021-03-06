package com.metagxd.filetodbparser.dbtranser.xml;

import com.metagxd.filetodbparser.db.creator.table.TableCreator;
import com.metagxd.filetodbparser.db.saver.DbSaver;
import com.metagxd.filetodbparser.dbtranser.DbTransfer;
import com.metagxd.filetodbparser.factory.dbconnection.DbConnectionFactory;
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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static javax.xml.stream.XMLStreamConstants.*;

/**
 * This class implements {@link DbTransfer}
 */
@Component
public class XMLDbTransfer implements DbTransfer {

    private final DbConnectionFactory connectionFactory;
    private final DbSaver<List<String[]>> dbSaver;
    private final XMLReaderFactory<XMLStreamReader> readerFactory;
    private final TableCreator tableCreator;

    @Value("${database.table.name}")
    private String tableName;
    @Value("${database.unique.fields:#{null}}")
    private List<String> uniqueColumns;
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

    /**
     * Transfer data from XML file to db
     *
     * @param fileName name of xml file.
     * @param parentNodeName name of parent node that contains child node that will be transfer in db.
     * @param nodeNames name of child node.
     */
    public void transferToDb(String fileName, String parentNodeName, String... nodeNames) {
        var path = Paths.get(fileName);
        if (!Files.exists(path)) {
            logger.error("File {} not exist!", fileName);
            return;
        }
        try (
                readerFactory;
                Connection connection = connectionFactory.getConnection()
        ) {
            tableCreator.createTable(connection, tableName, uniqueColumns, nodeNames);
            XMLStreamReader reader = readerFactory.getReader(Files.newInputStream(path));
            //create storage for elements
            var nodeData = new String[nodeNames.length];
            logger.info("Start reading file {}", fileName);
            //populate object from xml
            while (reader.hasNext()) {       // while not end of XML
                int event = reader.next();   // read next event
                if (event == START_ELEMENT) {
                    String nodeName = reader.getLocalName();
                    if (Arrays.asList(nodeNames).contains(nodeName)) {
                        saveToNodeData(nodeData, reader, nodeNames, nodeName);
                    }
                }

                //if reach end of node save node values to nodeList and create new nodeData
                if (event == END_ELEMENT && parentNodeName.equals(reader.getLocalName())) {
                    nodeList.add(nodeData);
                    nodeData = new String[nodeNames.length];
                }

                //if batch size reached the limit push to DB, else if end of document reached
                if (nodeList.size() >= batchSize || event == END_DOCUMENT && !nodeList.isEmpty()) {
                    dbSaver.save(connection, nodeList);
                    nodeList.clear();
                }
            }
        } catch (XMLStreamException | IOException | SQLException e) {
            logger.error("Transfer error:", e);
        }
    }

    private void saveToNodeData(String[] nodeData, XMLStreamReader reader, String[] nodeNames,
                                String nodeName) throws XMLStreamException {
        for (var i = 0; i < nodeNames.length; i++) {
            if (nodeNames[i].equalsIgnoreCase(nodeName)) {
                nodeData[i] = reader.getElementText();
            }
        }
    }

}
