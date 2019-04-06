package io.crdb.examples.retry.spring.jdbc;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Profile("!test")
public class SpringJdbcTemplateApplicationRunner implements ApplicationRunner {

    private final SpringJdbcTemplateExampleDAO dao;
    private final JdbcTemplate jdbcTemplate;

    public SpringJdbcTemplateApplicationRunner(SpringJdbcTemplateExampleDAO dao, JdbcTemplate jdbcTemplate) {
        this.dao = dao;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {

        // Create Table
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS spring_jdbc_template(id UUID PRIMARY KEY, balance INT)");

        // Insert into Table
        dao.insert(UUID.randomUUID(), ThreadLocalRandom.current().nextInt(0, 1000));
    }
}
