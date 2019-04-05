package io.crdb.examples.retry.basic;

import org.postgresql.ds.PGSimpleDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class BasicExampleHarness {

    public static void main(String[] args) throws SQLException {

        // Create DataSource
        final PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setServerName("localhost");
        ds.setPortNumber(5432);
        ds.setDatabaseName("examples");
        ds.setUser("root");
        ds.setPassword(null);

        // Create Table
        try (Connection connection = ds.getConnection();
             PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS basic_example(id UUID PRIMARY KEY, balance INT)")) {
            statement.executeUpdate();
        }

        // Create DAO
        BasicExampleDAO dao = new BasicExampleDAO(ds);

        // Insert BasicExample
        dao.insert(UUID.randomUUID(), ThreadLocalRandom.current().nextInt(0, 1000));

    }
}
