package com.metagxd.filetodbparser.parsers;

import com.metagxd.filetodbparser.model.Company;
import com.metagxd.filetodbparser.repo.CompanyDbSaver;
import com.metagxd.filetodbparser.util.StaxStreamProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

@Component
public class XmlDbParser implements DbParser {

    private final Logger logger = LoggerFactory.getLogger(XmlDbParser.class);

    private static final int BATCH_SIZE = 20_000;
    private List<Company> companies = new ArrayList<>();
    private XMLStreamReader reader;

    private final CompanyDbSaver companyDbSaver;

    public XmlDbParser(CompanyDbSaver companyDbSaver) {
        this.companyDbSaver = companyDbSaver;
    }

    //create list of object from xml file and write it to db
    public void parseToDb(String fileName) {

        logger.info("Start parsing file {}", fileName);

        Path path = Paths.get(fileName);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("File not exist!");
        }
        try (StaxStreamProcessor processor = new StaxStreamProcessor(Files.newInputStream(path))) {
            reader = processor.getReader();
            //create object
            Company company = new Company();

            //populate object from xml field by field
            while (reader.hasNext()) {       // while not end of XML
                int event = reader.next();   // read next event
                if (event == START_ELEMENT &&
                        "name".equals(reader.getLocalName())) {
                    company.setName(reader.getElementText());
                }
                if (event == START_ELEMENT &&
                        "city".equals(reader.getLocalName())) {
                    company.setCity(reader.getElementText());
                }
                if (event == START_ELEMENT &&
                        "foundation".equals(reader.getLocalName())) {
                    company.setFoundation(Integer.parseInt(reader.getElementText()));
                    companies.add(company);
                    company = new Company();
                }

                //if batch size reached the limit push to DB, else if end of document reached
                if (companies.size() >= BATCH_SIZE || event == END_DOCUMENT && !companies.isEmpty()) {
                    companyDbSaver.saveToDb(companies);
                    companies = new ArrayList<>();
                }
            }
        } catch (XMLStreamException | IOException e) {
            e.printStackTrace();
        }
    }
}
