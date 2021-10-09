package com.metagxd.filetodbparser;

import com.metagxd.filetodbparser.db.saver.DbSaver;
import com.metagxd.filetodbparser.reader.FileReader;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FileToDbParserApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(FileToDbParserApplication.class);

    @Autowired
    private FileReader dbTransfer;
    @Autowired
    private DbSaver dbSaver;

    public static void main(String[] args) {
        SpringApplication.run(FileToDbParserApplication.class, args);
    }

    @Override
    public void run(String... args) {
        var readerThread = new Thread(() -> dbTransfer.run(), "TransferThread");
        var saveThreads = new ArrayList<Thread>();
        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            saveThreads.add(new Thread(() -> dbSaver.run(), "Transfer Thread " + i));
        }

        readerThread.start();
        saveThreads.forEach(Thread::start);
    }

    @Bean
    public BlockingQueue<String[]> getBlockingQueue(@Value("${transfer.cache.size}") int capacity) {
        return new LinkedBlockingQueue<>(capacity);
    }
}
