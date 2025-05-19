package org.example.main.utils;

import org.example.main.controllers.InMemoryDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SessionManager {
    private final Connection connection;
    private final InMemoryDatabase inMemoryDatabase;
    private static SessionManager instance;
    private int loggedInUserId = -1;
    private boolean isAdmin = false;
    private final List<AuthObserver> observers = new ArrayList<>();

    private static final Logger logger = Logger.getLogger(SessionManager.class.getName());

    public SessionManager(InMemoryDatabase inMemoryDatabase) throws SQLException {
        this.inMemoryDatabase = inMemoryDatabase;
        this.connection = inMemoryDatabase.getConnection();
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            try {
                InMemoryDatabase db = InMemoryDatabase.getInstance();
                instance = new SessionManager(db);
            } catch (SQLException e) {
                System.err.println("Ошибка при создании SessionManager: " + e.getMessage());
            }
        }
        return instance;
    }

    public void addObserver(AuthObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(AuthObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        for (AuthObserver observer : observers) {
            observer.onAuthStateChanged();
        }
    }

    public void setLoggedInUser(String username, int userId, boolean isAdmin) {
        try {
            inMemoryDatabase.setLoggedInUser(username, userId);
            this.loggedInUserId = userId;
            this.isAdmin = isAdmin;
            logger.info("Пользователь " + username + " успешно авторизован.");
            notifyObservers();
        } catch (SQLException e) {
            logger.severe("Ошибка при установке данных пользователя: " + e.getMessage());
        }
    }

    public int authenticateUser(String username, String password) {
        String query = "SELECT USER_ID, IS_ADMIN FROM USERS WHERE USERNAME = ? AND PASSWORD_HASH = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password); // Предполагается, что пароль хранится в виде текста; рекомендуется хэширование
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("USER_ID");
                    boolean isAdmin = rs.getBoolean("IS_ADMIN");
                    setLoggedInUser(username, userId, isAdmin);
                    return userId;
                }
            }
        } catch (SQLException e) {
            logger.severe("Ошибка аутентификации: " + e.getMessage());
        }
        return -1;
    }

    public boolean isLoggedIn() {
        try {
            return inMemoryDatabase.isLoggedIn();
        } catch (SQLException e) {
            logger.severe("Ошибка проверки авторизации: " + e.getMessage());
            return false;
        }
    }

    public String getLoggedInUsername() {
        try {
            return inMemoryDatabase.getLoggedInUsername();
        } catch (SQLException e) {
            logger.severe("Ошибка получения имени: " + e.getMessage());
            return null;
        }
    }

    public int getLoggedInUserId() {
        try {
            return inMemoryDatabase.getLoggedInUserId();
        } catch (SQLException e) {
            logger.severe("Ошибка получения ID: " + e.getMessage());
            return -1;
        }
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void logout() {
        try {
            inMemoryDatabase.logout();
            this.loggedInUserId = -1;
            this.isAdmin = false;
            logger.info("Пользователь вышел из системы.");
            notifyObservers();
        } catch (SQLException e) {
            logger.severe("Ошибка выхода: " + e.getMessage());
        }
    }
    public void setLoggedInUser(String testuser, int i) {
    }
}