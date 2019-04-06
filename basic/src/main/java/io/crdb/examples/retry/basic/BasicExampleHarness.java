package io.crdb.examples.retry.basic;

import org.postgresql.ds.PGSimpleDataSource;

import java.sql.SQLException;

class BasicExampleHarness {

    public static void main(String[] args) {

        // Create DataSource
        final PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setServerName("localhost");
        ds.setPortNumber(5432);
        ds.setDatabaseName("examples");
        ds.setUser("root");
        ds.setPassword(null);

        // Create DAO
        final BasicExampleDAO dao = new BasicExampleDAO(ds);

        // Generate Retry
        dao.retryable();

    }
}
