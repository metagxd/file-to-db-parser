package com.metagxd.filetodbparser.factory.dbconnection;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class PostgreSqlConnectionFactory implements DbConnectionFactory {

    @Value("${database.url}")
    private String url;
    @Value("${database.username}")
    private String username;
    @Value("${database.password}")
    private String password;

    @Override
    public Connection getConnection() {
        var logger = LoggerFactory.getLogger(PostgreSqlConnectionFactory.class);
        try {
            logger.debug("Getting connection for {}", url);
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException sqlException) {
            logger.error("Can't get connection ", sqlException);
        }
        return null;
    }
}
