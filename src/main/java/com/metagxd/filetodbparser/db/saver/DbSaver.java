package com.metagxd.filetodbparser.db.saver;

import java.sql.Connection;

public interface DbSaver<T> {
    void save(Connection connection, T objectList);
}
