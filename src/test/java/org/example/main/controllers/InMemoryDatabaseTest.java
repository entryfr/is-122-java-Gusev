package org.example.main.controllers;

import org.junit.*;
import java.sql.SQLException;
import java.util.List;

public class InMemoryDatabaseTest {

    private InMemoryDatabase db;

    @Before
    public void setUp() {
        db = InMemoryDatabase.getInstance();
    }

    @Test
    public void testAddAndLoadCategory() throws SQLException {
        db.addCategory("Тестовая категория");
        List categories = db.loadCategories();
        Assert.assertTrue(categories.contains("Тестовая категория"));
    }

    @Test
    public void testAddAndGetCity() throws SQLException {
        db.addCity("Тестовый город");
        List cities = db.getCities();
        Assert.assertTrue(cities.contains("Тестовый город"));
    }

    @Test
    public void testAddUserAndAuthenticate() throws SQLException {
        int userId = db.addUser("testuser", "test@example.com", "pass", "Имя", "Фамилия", "123456");
        Assert.assertTrue(userId > 0);
        int authId = db.authenticateUser("testuser", "pass");
        Assert.assertEquals(userId, authId);
    }

    @Test
    public void testUsernameUniqueness() throws SQLException {
        db.addUser("uniqueuser", "unique@example.com", "pass", "Имя", "Фамилия", "123456");
        Assert.assertTrue(db.isUsernameTaken("uniqueuser"));
        Assert.assertFalse(db.isUsernameTaken("anotheruser"));
    }

}
