package io.crdb.examples.retry.spring.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.UUID;

@Component
class SpringJdbcTemplateExampleDAO {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public SpringJdbcTemplateExampleDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    void insert(UUID id, int balance) {

        jdbcTemplate.execute("INSERT INTO spring_jdbc_template(id,balance) VALUES(?,?)", new PreparedStatementCallback<Integer>() {
            @Override
            public Integer doInPreparedStatement(PreparedStatement preparedStatement) throws SQLException, DataAccessException {
                preparedStatement.setObject(1, id);
                preparedStatement.setInt(2, balance);
                return preparedStatement.executeUpdate();
            }
        });

    }
}
