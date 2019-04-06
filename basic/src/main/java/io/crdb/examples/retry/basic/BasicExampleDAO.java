package io.crdb.examples.retry.basic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;

class BasicExampleDAO {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final int MAX_RETRY_COUNT = 0;
    private static final String SAVEPOINT_NAME = "cockroach_restart";
    private static final String RETRY_SQL_STATE = "40001";

    private final DataSource ds;

    BasicExampleDAO(DataSource ds) {
        this.ds = ds;
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
        try (PreparedStatement statement = connection.prepareStatement("select 1")){
            statement.executeQuery();
        }
    }
}
