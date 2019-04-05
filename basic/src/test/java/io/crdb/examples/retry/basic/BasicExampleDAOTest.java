package io.crdb.examples.retry.basic;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.startsWith;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BasicExampleDAOTest {

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

        BasicExample basicExample = new BasicExample();
        basicExample.setId(UUID.randomUUID());
        basicExample.setBalance(100);

        new BasicExampleDAO(ds).insert(basicExample);
    }

}