package org.example.main.controllers;

import org.example.main.models.Ad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryDatabaseTest {
    private InMemoryDatabase db;

    @BeforeEach
    void setUp() throws SQLException {
        db = new InMemoryDatabase();
        db.createTables();
        db.populateCategories();
        db.populateCities();
    }

    @Test
    void testAddUser() throws SQLException {
        int userId = db.addUser("testuser", "test@example.com", "password", "Test", "User", "1234567890");
        assertTrue(userId > 0, "User ID should be greater than 0");

        // Проверяем, что пользователь добавлен
        boolean isUsernameTaken = db.isUsernameTaken("testuser");
        assertTrue(isUsernameTaken, "Username should be taken");
    }

    @Test
    void testAuthenticateUser() throws SQLException {
        db.addUser("testuser", "test@example.com", "password", "Test", "User", "1234567890");
        int userId = db.authenticateUser("testuser", "password");
        assertTrue(userId > 0, "Authentication should succeed");

        int invalidUserId = db.authenticateUser("testuser", "wrongpassword");
        assertEquals(-1, invalidUserId, "Authentication should fail with wrong password");
    }

    @Test
    void testGetFilteredAds() throws SQLException {
        // Добавляем тестовое объявление
        db.createAd(1, 1, "Test Ad", "Description", 100.0, "Москва", null);

        List<Ad> ads = db.getFilteredAds(2, "Электроника", 50.0, 150.0);
        assertFalse(ads.isEmpty(), "Filtered ads should not be empty");
        assertEquals("Test Ad", ads.get(0).getTitle(), "Ad title should match");
    }
}