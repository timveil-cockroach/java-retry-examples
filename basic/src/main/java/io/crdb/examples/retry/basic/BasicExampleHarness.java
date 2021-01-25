package io.crdb.examples.retry.basic;

import org.postgresql.ds.PGSimpleDataSource;

class BasicExampleHarness {

    public static void main(String[] args) {

        // Create DataSource
        final PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setServerNames(new String[]{"localhost"});
        ds.setPortNumbers(new int[]{26257});
        ds.setDatabaseName("java_retry_example");
        ds.setApplicationName("basic_example");
        ds.setUser("root");
        ds.setPassword(null);

        // Create DAO
        final BasicExampleDAO dao = new BasicExampleDAO(ds);

        dao.createTable();

        // Generate Retry
        dao.retryable();

    }
}
