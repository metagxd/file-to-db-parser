package com.metagxd.filetodbparser.db.saver;

import com.metagxd.filetodbparser.db.creator.query.QueryCreator;
import com.metagxd.filetodbparser.factory.dbconnection.DbConnectionFactory;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/*create sql query once than use existing*/
@Component
public class DbSaverImpl implements DbSaver {

    private final String tableName;
    private final QueryCreator queryCreator;
    private final List<String> columnNames;
    private final BlockingQueue<String[]> nodeStorage;
    private final DbConnectionFactory connectionFactory;
    private boolean isStopped = false;
    private String query;

    private static final Logger logger = LoggerFactory.getLogger(DbSaverImpl.class);

    public DbSaverImpl(@Value("${database.table.name}") String tableName, QueryCreator queryCreator,
            @Value("#{'${transfer.child.node.names}'.split(',')}") List<String> columnNames, BlockingQueue<String[]> nodeStorage,
            DbConnectionFactory connectionFactory) {
        this.tableName = tableName;
        this.queryCreator = queryCreator;
        this.columnNames = columnNames;
        this.nodeStorage = nodeStorage;
        this.connectionFactory = connectionFactory;
    }

    public void save() {
        if (query == null) {
            query = queryCreator.getQuery(tableName, columnNames.toArray(new String[0]));
        }
        try (var connection = connectionFactory.getConnection();
                var preparedStatement = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            while (!nodeStorage.isEmpty()) {
                int batchSize = 10_000;
                if (nodeStorage.size() < batchSize) {
                    batchSize = nodeStorage.size();
                }
                for (int i = 0; i < batchSize; i++) {
                    String[] poll = nodeStorage.take();
                    for (int j = 0; j < poll.length; j++) {
                        int index = j + 1;
                        preparedStatement.setString(index, getStringOrNull(poll[j]));
                    }
                    preparedStatement.addBatch();

                }
                int size = Arrays.stream(preparedStatement.executeBatch()).sum();
                logger.debug("Executing batch size {}", size);
                connection.commit();
            }
        } catch (SQLException | InterruptedException exception) {
            logger.error("Save error!", exception);
            exception.printStackTrace();
        }
    }

    /*return given string or null if not present or empty*/
    private String getStringOrNull(@Nullable String s) {
        if (s != null) {
            return s.isEmpty() ? null : s;
        }
        return null;
    }

    @Override
    public void run() {
        save();
    }

    public boolean isStopped() {
        return isStopped;
    }

    public void setStopped(boolean stopped) {
        isStopped = stopped;
    }
}
