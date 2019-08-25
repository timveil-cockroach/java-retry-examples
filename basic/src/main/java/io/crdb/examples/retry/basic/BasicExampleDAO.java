package io.crdb.examples.retry.basic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class BasicExampleDAO {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final int MAX_RETRY_COUNT = 0;
    private static final String SAVEPOINT_NAME = "cockroach_restart";
    private static final String RETRY_SQL_STATE = "40001";

    private final DataSource ds;

    BasicExampleDAO(DataSource ds) {
        this.ds = ds;
    }

    void forceRetry() {


        UUID id = UUID.randomUUID();
        int balance = 100;

        try (Connection connection = ds.getConnection()) {

            try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS basic_example(id UUID PRIMARY KEY, balance INT)")) {
                statement.executeUpdate();
            }

            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO basic_example(id,balance) VALUES(?,?)")) {
                statement.setObject(1, id);
                statement.setInt(2, balance);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        final ExecutorService executorService = Executors.newFixedThreadPool(2);

        CountDownLatch countDownLatch = new CountDownLatch(2);

        executorService.execute(new Runnable() {
            @Override
            public void run() {

                try (Connection connection = ds.getConnection()) {
                    try {
                        connection.setAutoCommit(false);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    try (PreparedStatement statement = connection.prepareStatement("select * from basic_example where id = ?")) {
                        statement.setObject(1, id);
                        statement.executeQuery();

                        log.debug("select");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    try (PreparedStatement statement = connection.prepareStatement("update basic_example set balance = ? where id = ?")) {
                        statement.setInt(1, 99);
                        statement.setObject(2, id);
                        statement.executeUpdate();

                        log.debug("first update");

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    try {
                        connection.commit();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    try {
                        connection.setAutoCommit(true);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }


                    countDownLatch.countDown();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        executorService.execute(new Runnable() {
            @Override
            public void run() {

                try (Connection connection = ds.getConnection()) {
                    try {
                        connection.setAutoCommit(false);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                    try (PreparedStatement statement = connection.prepareStatement("update basic_example set balance = ? where id = ?")) {
                        statement.setInt(1, 98);
                        statement.setObject(2, id);
                        statement.executeUpdate();

                        log.debug("second update");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    try {
                        connection.commit();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    try {
                        connection.setAutoCommit(true);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    countDownLatch.countDown();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }

        log.debug("finished...{}", id);


    }

    void retryable() {

        try (Connection connection = ds.getConnection()) {

            connection.setAutoCommit(false);

            int retryCount = 0;

            while (MAX_RETRY_COUNT == 0 || retryCount < MAX_RETRY_COUNT) {

                Savepoint sp = connection.setSavepoint(SAVEPOINT_NAME);

                // this method call is only used to test retry logic.  it is not necessary in production code;
                forceRetry(connection);

                try (PreparedStatement statement = connection.prepareStatement("SELECT crdb_internal.force_retry('1s':::INTERVAL)");
                     final ResultSet resultSet = statement.executeQuery()) {

                    if (resultSet.next()) {
                        log.debug("query result = [{}]", resultSet.getString(1));
                    }

                    connection.releaseSavepoint(sp);

                    connection.commit();

                    break;

                } catch (SQLException e) {

                    if (RETRY_SQL_STATE.equals(e.getSQLState())) {
                        log.debug("retryable exception occurred: sql state = [{}], message = [{}], retry counter = {}", e.getSQLState(), e.getMessage(), retryCount);
                        connection.rollback(sp);
                        retryCount++;
                    } else {
                        throw e;
                    }
                }
            }

            connection.setAutoCommit(true);

        } catch (SQLException e) {
            log.error(String.format("an unexpected error occurred during retryable: %s", e.getMessage()), e);
        }
    }

    private void forceRetry(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("select 1")) {
            statement.executeQuery();
        }
    }
}
