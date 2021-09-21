package com.metagxd.filetodbparser.db.creator.table;

import java.sql.Connection;
import java.util.List;

public interface TableCreator {

    void createTable(Connection connection,  String tableName, List<String> uniqueColumns, String... columnNames);
}
