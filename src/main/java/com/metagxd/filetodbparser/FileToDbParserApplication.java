package com.metagxd.filetodbparser;

import com.metagxd.filetodbparser.db.saver.DbSaver;
import com.metagxd.filetodbparser.dbtranser.DbTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@SpringBootApplication
public class FileToDbParserApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(FileToDbParserApplication.class);

    @Autowired
    private DbTransfer dbTransfer;
    @Autowired
    private DbSaver dbSaver;

    public static void main(String[] args) {
        SpringApplication.run(FileToDbParserApplication.class, args);
    }


    @Override
    public void run(String... args) {
        StopWatch stopWatch = new StopWatch("Execution time");
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        stopWatch.start("Transfer");
        var readerThread = new Thread(() -> dbTransfer.run(), "TransferThread");
        List<Thread> threadList = new ArrayList<>(Arrays.asList(
                new Thread(() -> dbSaver.run(), "saverThread0"),
                new Thread(() -> dbSaver.run(), "saverThread1"),
                new Thread(() -> dbSaver.run(), "saverThread2"),
                new Thread(() -> dbSaver.run(), "saverThread3")
        ));

        readerThread.start();
        threadList.forEach(Thread::start);
        while (true) {
            if (readerThread.isInterrupted()) {
                stopWatch.stop();
                break;
            }
        }
        logger.info("Execution time {} seconds", stopWatch.getTotalTimeSeconds());
    }


    @Bean(name = "transferBlockingQueue")
    public BlockingQueue<String[]> getBlockingQueue() {
        return new LinkedBlockingQueue<>(400_000);
    }
}
