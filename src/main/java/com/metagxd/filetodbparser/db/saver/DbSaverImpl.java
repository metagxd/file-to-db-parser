package com.metagxd.filetodbparser.db.saver;

import com.metagxd.filetodbparser.db.creator.query.QueryCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/*create sql query once than use existing*/

@Component
public class DbSaverImpl implements DbSaver<List<String[]>> {
    private final QueryCreator queryCreator;
    private final String tableName;
    private final List<String> columnNames;
    private String query;
    private static final Logger logger = LoggerFactory.getLogger(DbSaverImpl.class);

    public DbSaverImpl(QueryCreator queryCreator, @Value("${database.table.name}") String tableName,
                       @Value("#{'${transfer.child.node.names}'.split(',')}") List<String> columnNames) {
        this.queryCreator = queryCreator;
        this.tableName = tableName;
        this.columnNames = columnNames;
    }

    public void save(Connection connection, List<String[]> nodeList) {
        if (query == null) {
            query = queryCreator.getQuery(tableName, columnNames.toArray(new String[0]));
        }
        try (var preparedStatement = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            for (String[] node : nodeList) {
                for (int i = 0; i < node.length; i++) {
                    int index = i + 1;
                    preparedStatement.setString(index, getStringOrNull(node[i]));
                }
                preparedStatement.addBatch();
            }
            logger.debug("Executing batch, size {}", nodeList.size());
            preparedStatement.executeBatch();
            connection.commit();
        } catch (SQLException sqlException) {
            logger.error("Save error!", sqlException);
        }
    }

    private String getStringOrNull(@Nullable String s) {
        if (s != null) {
            return s.isEmpty() ? null : s;
        }
        return null;
    }
}
