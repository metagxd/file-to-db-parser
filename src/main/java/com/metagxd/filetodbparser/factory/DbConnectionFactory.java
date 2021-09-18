package com.metagxd.filetodbparser.factory;

import java.sql.Connection;

public interface DbConnectionFactory {
    Connection getConnection();
}
