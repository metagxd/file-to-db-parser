package com.metagxd.filetodbparser.repo;

import com.metagxd.filetodbparser.factory.DbConnectionFactory;
import com.metagxd.filetodbparser.model.Company;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Component
public class CompanyDbSaver implements DbSaver<Company> {

    private final DbConnectionFactory dbConnectionFactory;

    public CompanyDbSaver(DbConnectionFactory dbConnectionFactory) {
        this.dbConnectionFactory = dbConnectionFactory;
    }

    public void saveToDb(List<Company> objectList) {
        Connection connection = dbConnectionFactory.getConnection();

        try (var preparedStatement = connection.prepareStatement("INSERT INTO companies (name, city ,foundation) VALUES (?,?,?) ON CONFLICT DO NOTHING")) {
            connection.setAutoCommit(false);
            for (Company company : objectList) {
                preparedStatement.setString(1, company.getName());
                preparedStatement.setString(2, company.getCity());
                preparedStatement.setInt(3, company.getFoundation());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            connection.commit();
            connection.close();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }
}
