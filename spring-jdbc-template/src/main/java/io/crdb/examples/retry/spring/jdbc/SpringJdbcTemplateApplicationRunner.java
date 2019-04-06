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

    public SpringJdbcTemplateApplicationRunner(SpringJdbcTemplateExampleDAO dao) {
        this.dao = dao;
    }

    @Override
    public void run(ApplicationArguments args) {
        dao.retryable();
    }
}
