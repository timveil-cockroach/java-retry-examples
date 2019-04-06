package io.crdb.examples.retry.spring.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Profile("!test")
public class SpringJdbcApplicationRunner implements ApplicationRunner {

    private final SpringJdbcExampleDAO dao;
    private final DataSource dataSource;

    @Autowired
    public SpringJdbcApplicationRunner(SpringJdbcExampleDAO dao, DataSource dataSource) {
        this.dao = dao;
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        // Create Table
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS spring_jdbc(id UUID PRIMARY KEY, balance INT)")) {
            statement.executeUpdate();
        }

        // Insert into Table
        dao.insert(UUID.randomUUID(), ThreadLocalRandom.current().nextInt(0, 1000));
    }
}
