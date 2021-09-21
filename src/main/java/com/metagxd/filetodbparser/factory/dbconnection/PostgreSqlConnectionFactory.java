package com.metagxd.filetodbparser.factory.dbconnection;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class PostgreSqlConnectionFactory implements DbConnectionFactory {

    private final String url;
    private final String username;
    private final String password;

    public PostgreSqlConnectionFactory(@Value("${database.url}") String url, @Value("${database.username}") String username,
                                       @Value("${database.password}") String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public Connection getConnection() throws SQLException {
        var logger = LoggerFactory.getLogger(PostgreSqlConnectionFactory.class);
        try {
            logger.debug("Getting connection for {}", url);
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException sqlException) {
            throw new SQLException("Can't connect to database!", sqlException);
        }
    }
}
