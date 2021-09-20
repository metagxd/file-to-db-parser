package com.metagxd.filetodbparser;

import com.metagxd.filetodbparser.dbtranser.DbTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StopWatch;

import java.util.List;

@SpringBootApplication
public class FileToDbParserApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(FileToDbParserApplication.class);

    @Autowired
    private DbTransfer xmlParser;
    @Value("${transfer.file.name}")
    private String fileName;
    @Value("#{'${transfer.child.node.names}'.split(',')}")
    private List<String> nodeNames;
    @Value("${transfer.parent.node.name}")
    private String parentNodeName;

    public static void main(String[] args) {
        SpringApplication.run(FileToDbParserApplication.class, args);
    }


    @Override
    public void run(String... args) {
        StopWatch stopWatch = new StopWatch("Execution time");
        stopWatch.start("Transfer");
        xmlParser.transferToDb(fileName, parentNodeName, nodeNames.toArray(new String[0]));
        stopWatch.stop();
        logger.info("Execution time {} seconds", stopWatch.getTotalTimeSeconds());
    }

}
