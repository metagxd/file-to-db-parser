package com.metagxd.filetodbparser;

import com.metagxd.filetodbparser.dbtranser.xml.XMLDbTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.List;

@SpringBootApplication
@EnableAsync
public class FileToDbParserApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(FileToDbParserApplication.class);

    @Autowired
    XMLDbTransfer xmlParser;
    @Value("${transfer.file.name}")
    private String fileName;
    @Value("#{'${transfer.node.names}'.split(',')}")
    private List<String> nodeNames;
    @Value("${transfer.element.name}")
    private String elementName;

    public static void main(String[] args) {
        SpringApplication.run(FileToDbParserApplication.class, args);
    }


    @Override
    public void run(String... args) {
        long startTime = System.nanoTime();
        xmlParser.transferToDb(fileName, elementName, nodeNames.toArray(new String[0]));
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000;
        logger.info("{} ms", duration);
    }

}
