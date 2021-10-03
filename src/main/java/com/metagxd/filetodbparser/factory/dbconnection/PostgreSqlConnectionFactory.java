package com.metagxd.filetodbparser.factory.dbconnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Factory implementation for creating db connections
 */
@Component
public class PostgreSqlConnectionFactory implements DbConnectionFactory {

    private final String url;
    private final String username;
    private final String password;

    private static final Logger logger = LoggerFactory.getLogger(PostgreSqlConnectionFactory.class);

    /**
     * Create {@link DbConnectionFactory} implementation for creating connection for PostreSQL database.
     *
     * @param url      url of database
     * @param username username
     * @param password password to db
     */
    public PostgreSqlConnectionFactory(@Value("${database.url}") String url, @Value("${database.username}") String username,
                                       @Value("${database.password}") String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * @return {@link Connection} to database.
     * @throws SQLException when can't establish connection for any reason.
     */
    @Override
    public Connection getConnection() throws SQLException {
        try {
            logger.debug("Getting connection for {}", url);
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException sqlException) {
            throw new SQLException("Can't connect to database!", sqlException);
        }
    }
}
