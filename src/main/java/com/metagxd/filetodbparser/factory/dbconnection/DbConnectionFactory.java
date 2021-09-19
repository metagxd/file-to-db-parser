package com.metagxd.filetodbparser.factory.dbconnection;

import java.sql.Connection;

public interface DbConnectionFactory {
    Connection getConnection();
}
