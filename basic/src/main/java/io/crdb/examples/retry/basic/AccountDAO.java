package io.crdb.examples.retry.basic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;

public class AccountDAO {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final int MAX_RETRY_COUNT = 3;
    private static final String SAVEPOINT_NAME = "cockroach_restart";
    private static final String RETRY_SQL_STATE = "40001";

    private DataSource ds;

    public AccountDAO(DataSource ds) {
        this.ds = ds;
    }


    public void update(Account account) {

        try (Connection connection = ds.getConnection()) {

            connection.setAutoCommit(false);

            int retryCount = 0;

            while (retryCount < MAX_RETRY_COUNT) {

                Savepoint sp = connection.setSavepoint(SAVEPOINT_NAME);

                try (PreparedStatement statement = connection.prepareStatement("UPDATE account SET balance = ? WHERE id = ?")) {

                    statement.setInt(1, account.getBalance());
                    statement.setObject(2, account.getId());
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
            log.error(String.format("an unexpected error occurred: %s", e.getMessage()), e);
        }
    }

}
