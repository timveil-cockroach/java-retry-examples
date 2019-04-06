package io.crdb.examples.retry.spring.jdbc;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@ActiveProfiles("test")
public class SpringJdbcTemplateApplicationTests {

    @InjectMocks
    private SpringJdbcTemplateExampleDAO dao;

    @Mock
    private DataSource ds;

    @Mock
    private Connection c;

    @Mock
    private PreparedStatement stmt;

    @Mock
    private Savepoint sp;

    @Before
    public void setUp() throws Exception {
        assertNotNull(ds);
        when(ds.getConnection()).thenReturn(c);
    }

    @Test
    public void insert() throws SQLException {

        when(c.prepareStatement(startsWith("INSERT"))).thenReturn(stmt);
        when(c.setSavepoint(anyString())).thenReturn(sp);
        when(stmt.executeUpdate()).thenThrow(new SQLException("mock retry", "40001", 99, null)).thenReturn(1);

        dao.insert(UUID.randomUUID(), 100);
    }

}
