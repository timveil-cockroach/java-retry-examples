package io.crdb.examples.retry.spring.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.UUID;

@Component
class SpringJdbcExampleDAO {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final int MAX_RETRY_COUNT = 3;
    private static final String SAVEPOINT_NAME = "cockroach_restart";
    private static final String RETRY_SQL_STATE = "40001";

    private final DataSource ds;

    @Autowired
    SpringJdbcExampleDAO(DataSource ds) {
        this.ds = ds;
    }

    void insert(UUID id, int balance) {

        try (Connection connection = ds.getConnection()) {

            connection.setAutoCommit(false);

            int retryCount = 0;

            while (retryCount < MAX_RETRY_COUNT) {

                Savepoint sp = connection.setSavepoint(SAVEPOINT_NAME);

                try (PreparedStatement statement = connection.prepareStatement("INSERT INTO spring_jdbc(id,balance) VALUES(?,?)")) {

                    statement.setObject(1, id);
                    statement.setInt(2, balance);
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
            log.error(String.format("an unexpected error occurred during insert: %s", e.getMessage()), e);
        }
    }
}
