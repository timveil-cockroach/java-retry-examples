package io.crdb.examples.retry.spring.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@Profile("!test")
public class SpringJdbcApplicationRunner implements ApplicationRunner {

    private final SpringJdbcExampleDAO dao;

    @Autowired
    public SpringJdbcApplicationRunner(SpringJdbcExampleDAO dao) {
        this.dao = dao;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        dao.retryable();
    }
}
