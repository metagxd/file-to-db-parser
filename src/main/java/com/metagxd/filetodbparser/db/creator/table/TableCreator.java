package com.metagxd.filetodbparser.db.creator.table;

import java.sql.Connection;

public interface TableCreator {

    void createTable(Connection connection, String tableName, String... columnNames);
}
