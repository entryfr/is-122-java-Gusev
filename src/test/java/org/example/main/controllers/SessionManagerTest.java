package org.example.main.controllers;

import org.example.main.controllers.InMemoryDatabase;
import org.example.main.utils.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionManagerTest {
    private SessionManager sessionManager;
    private InMemoryDatabase db;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        db = mock(InMemoryDatabase.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        // Настраиваем поведение getConnection
        when(db.getConnection()).thenReturn(connection);
        // Настраиваем prepareStatement
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        // Настраиваем executeQuery
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        sessionManager = new SessionManager(db);
    }

    @Test
    void testAuthenticateUserSuccess() throws SQLException {

        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("USER_ID")).thenReturn(1);
        when(resultSet.getBoolean("IS_ADMIN")).thenReturn(false);

        when(db.isLoggedIn()).thenReturn(true);
        when(db.getLoggedInUsername()).thenReturn("testuser");
        when(db.getLoggedInUserId()).thenReturn(1);

        int userId = sessionManager.authenticateUser("testuser", "password");
        assertEquals(1, userId, "User ID should be 1");
        assertTrue(sessionManager.isLoggedIn(), "User should be logged in");
        assertEquals("testuser", sessionManager.getLoggedInUsername(), "Username should match");

        verify(preparedStatement).setString(1, "testuser");
        verify(preparedStatement).setString(2, "password");
    }

    @Test
    void testLogout() throws SQLException {
        sessionManager.setLoggedInUser("testuser", 1);
        when(db.isLoggedIn()).thenReturn(false);

        sessionManager.logout();
        assertFalse(sessionManager.isLoggedIn(), "User should be logged out");
    }
}