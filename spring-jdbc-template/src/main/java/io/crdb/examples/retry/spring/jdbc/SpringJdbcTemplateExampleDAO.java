package io.crdb.examples.retry.spring.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
class SpringJdbcTemplateExampleDAO {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public SpringJdbcTemplateExampleDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    void retryable() {

        jdbcTemplate.query("select 1", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {

            }
        });

        jdbcTemplate.query("SELECT crdb_internal.force_retry('2m':::INTERVAL)", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    log.debug("query result [{}]", rs.getString(1));
                }
            }
        });

    }
}
