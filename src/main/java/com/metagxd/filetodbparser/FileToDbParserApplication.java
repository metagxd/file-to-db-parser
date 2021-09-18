package com.metagxd.filetodbparser;

import com.metagxd.filetodbparser.parsers.XmlDbParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FileToDbParserApplication implements CommandLineRunner {

    @Autowired
    private XmlDbParser xmlDbParser;

    @Value("${parser.file.name}")
    private String filename;

    public static void main(String[] args) {
        SpringApplication.run(FileToDbParserApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {
        long startTime = System.nanoTime();
        xmlDbParser.parseToDb(filename);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000;
        System.out.println(duration + " ms");
    }

}
