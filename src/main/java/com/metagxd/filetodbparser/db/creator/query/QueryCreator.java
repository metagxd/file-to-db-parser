package com.metagxd.filetodbparser.db.creator.query;

public interface QueryCreator {
    String getQuery(String tableName, String... columnNames);
}
