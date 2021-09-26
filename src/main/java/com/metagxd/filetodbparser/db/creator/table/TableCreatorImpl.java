package com.metagxd.filetodbparser.db.creator.table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Component
public class TableCreatorImpl implements TableCreator {

    private static final Logger logger = LoggerFactory.getLogger(TableCreatorImpl.class);

    @Override
    public void createTable(Connection connection, String tableName, List<String> uniqueColumns, String... columnNames) {
        logger.debug("Creating query for table {}", tableName);
        var sql = new StringBuilder();
        sql.append("CREATE SEQUENCE IF NOT EXISTS global_seq START WITH 1; ");
        sql.append("CREATE TABLE IF NOT EXISTS ")
                .append(tableName)
                .append(" (id INTEGER PRIMARY KEY DEFAULT nextval('global_seq'),");
        for (int i = 0; i < columnNames.length - 1; i++) {
            sql.append(columnNames[i]).append(" VARCHAR,");
        }
        sql.append(columnNames[columnNames.length - 1]) //last item w/o ','
                .append(" VARCHAR);");

        if (uniqueColumns != null) {
            sql.append(" CREATE UNIQUE INDEX ")
                    .append("unique_idx")
                    .append(" ON ")
                    .append(tableName)
                    .append('(');
            for (int i = 0; i < uniqueColumns.size() - 1; i++) {
                sql.append(uniqueColumns.get(i)).append(',');
            }
            sql.append(uniqueColumns.get(uniqueColumns.size() - 1)).append(");");
        }
        logger.debug("Created query: {}", sql);
        try (var preparedStatement = connection.prepareStatement(sql.toString())) {
            preparedStatement.execute();
        } catch (SQLException sqlException) {
            if (sqlException.getSQLState().equals("42P07")) { //WARN if table exist
                logger.warn("Table {} already exist.", tableName);
            } else {
                logger.error("Create table error!", sqlException);
            }
        }
    }

    public void createTable(Connection connection, String tableName, String... columnNames) {
        this.createTable(connection, tableName, null, columnNames);
    }
}