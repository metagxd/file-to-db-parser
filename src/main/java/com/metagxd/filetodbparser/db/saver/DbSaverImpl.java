package com.metagxd.filetodbparser.db.saver;

import com.metagxd.filetodbparser.db.creator.query.QueryCreator;
import com.metagxd.filetodbparser.exception.SavingException;
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
    private final int defaultBatchSize;
    private String query;

    private static final Logger logger = LoggerFactory.getLogger(DbSaverImpl.class);

    public DbSaverImpl(@Value("${database.table.name}") String tableName, QueryCreator queryCreator,
            @Value("#{'${transfer.child.node.names}'.split(',')}") List<String> columnNames, BlockingQueue<String[]> nodeStorage,
            DbConnectionFactory connectionFactory, @Value("${transfer.batch.size}") int batchSize) {
        this.tableName = tableName;
        this.queryCreator = queryCreator;
        this.columnNames = columnNames;
        this.nodeStorage = nodeStorage;
        this.connectionFactory = connectionFactory;
        this.defaultBatchSize = batchSize;
    }

    @Override
    public void save() throws SavingException {
        if (query == null) {
            query = queryCreator.getQuery(tableName, columnNames.toArray(new String[0]));
        }
        try (var connection = connectionFactory.getConnection();
                var preparedStatement = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            while (!nodeStorage.isEmpty()) {
                for (int i = 0; i < defaultBatchSize; i++) {
                    String[] node;
                    
                    synchronized (this) {
                        if (!nodeStorage.isEmpty()) {
                            node = nodeStorage.take();
                        } else {
                            break;
                        }
                    }
                    for (int j = 0; j < node.length; j++) {
                        int index = j + 1;
                        preparedStatement.setString(index, getStringOrNull(node[j]));
                    }
                    preparedStatement.addBatch();
                }
                int size = Arrays.stream(preparedStatement.executeBatch()).sum();
                logger.debug("Executing batch size {}", size);
                connection.commit();
            }
        } catch (SQLException | InterruptedException exception) {
            logger.error("Save error!", exception);
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new SavingException(exception);
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
}
