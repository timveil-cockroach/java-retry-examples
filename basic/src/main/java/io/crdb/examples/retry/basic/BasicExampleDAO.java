package io.crdb.examples.retry.basic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;

public class BasicExampleDAO {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final int MAX_RETRY_COUNT = 3;
    private static final String SAVEPOINT_NAME = "cockroach_restart";
    private static final String RETRY_SQL_STATE = "40001";

    private DataSource ds;

    public BasicExampleDAO(DataSource ds) {
        this.ds = ds;
    }


    public void update(BasicExample basicExample) {

        try (Connection connection = ds.getConnection()) {

            connection.setAutoCommit(false);

            int retryCount = 0;

            while (retryCount < MAX_RETRY_COUNT) {

                Savepoint sp = connection.setSavepoint(SAVEPOINT_NAME);

                try (PreparedStatement statement = connection.prepareStatement("UPDATE basic_example SET balance = ? WHERE id = ?")) {

                    statement.setInt(1, basicExample.getBalance());
                    statement.setObject(2, basicExample.getId());
                    statement.executeUpdate();

                    connection.releaseSavepoint(sp);

                    connection.commit();

                    break;

                } catch (SQLException e) {

                    if (RETRY_SQL_STATE.equals(e.getSQLState())) {
                        connection.rollback(sp);
                        retryCount++;
                    } else {
                        throw e;
                    }

                }

            }

            connection.setAutoCommit(true);

        } catch (SQLException e) {
            log.error(String.format("an unexpected error occurred during update: %s", e.getMessage()), e);
        }
    }


    public void create(BasicExample basicExample) {

        try (Connection connection = ds.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO basic_example(id,balance) VALUES(?,?)")) {

            statement.setObject(1, basicExample.getId());
            statement.setInt(2, basicExample.getBalance());
            statement.executeUpdate();

        } catch (SQLException e) {
            log.error(String.format("an unexpected error occurred during create: %s", e.getMessage()), e);
        }
    }

}
