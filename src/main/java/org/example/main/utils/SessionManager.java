package org.example.main.utils;

import org.example.main.controllers.InMemoryDatabase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public class SessionManager {
    private final Connection connection;
    private final InMemoryDatabase inMemoryDatabase;
    private static SessionManager instance;
    private int loggedInUserId = -1;
    public void setLoggedInUserId(int userId) {
        this.loggedInUserId = userId;
    }

    private static final Logger logger = Logger.getLogger(SessionManager.class.getName());

    public SessionManager(InMemoryDatabase inMemoryDatabase) throws SQLException {
        this.inMemoryDatabase = inMemoryDatabase;
        this.connection = inMemoryDatabase.getConnection();
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            try {
                InMemoryDatabase db = new InMemoryDatabase();
                instance = new SessionManager(db);
            } catch (SQLException e) {
                System.err.println("Ошибка при создании SessionManager: " + e.getMessage());
            }
        }
        return instance;
    }

    /**
     * Устанавливает данные авторизованного пользователя.
     */
    public void setLoggedInUser(String username, int userId) {
        try {
            inMemoryDatabase.setLoggedInUser(username, userId);
            logger.info("Пользователь " + username + " успешно авторизован.");
        } catch (SQLException e) {
            logger.severe("Ошибка при установке данных пользователя: " + e.getMessage());
        }
    }
    private boolean isAdmin = false;

    public void setLoggedInUser(String username, int userId, boolean isAdmin) {
        this.loggedInUserId = userId;
        this.isAdmin = isAdmin;
        try {
            inMemoryDatabase.setLoggedInUser(username, userId);
        } catch (SQLException e) {
            logger.severe("Ошибка при установке данных пользователя: " + e.getMessage());
        }
    }

    public boolean isAdmin() {
        return isAdmin;
    }
    /**
     * Аутентификация пользователя.
     */
    public int authenticateUser(String username, String password) {
        String query = "SELECT USER_ID, IS_ADMIN FROM USERS WHERE USERNAME = ? AND PASSWORD_HASH = ?";
        try (var stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            try (var rs = stmt.executeQuery()) {
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

    /**
     * Проверка, авторизован ли пользователь.
     */
    public boolean isLoggedIn() {
        try {
            return inMemoryDatabase.isLoggedIn();
        } catch (SQLException e) {
            logger.severe("Ошибка проверки авторизации: " + e.getMessage());
            return false;
        }
    }

    /**
     * Получение имени авторизованного пользователя.
     */
    public String getLoggedInUsername() {
        try {
            return inMemoryDatabase.getLoggedInUsername();
        } catch (SQLException e) {
            logger.severe("Ошибка получения имени: " + e.getMessage());
            return null;
        }
    }

    /**
     * Получение ID авторизованного пользователя.
     */
    public int getLoggedInUserId() {
        try {
            return inMemoryDatabase.getLoggedInUserId();
        } catch (SQLException e) {
            logger.severe("Ошибка получения ID: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Выход пользователя.
     */
    public void logout() {
        try {
            inMemoryDatabase.logout();
            logger.info("Пользователь вышел из системы.");
        } catch (SQLException e) {
            logger.severe("Ошибка выхода: " + e.getMessage());
        }
    }

    /**
     * Удаляет товар из корзины по ID товара.
     */
    public void removeFromBasket(int adId) {
        try {
            int userId = getLoggedInUserId();
            if (userId == -1) {
                logger.warning("Пользователь не авторизован. Невозможно удалить товар из корзины.");
                return;
            }
            inMemoryDatabase.removeFromBasket(userId, adId);
            logger.info("Товар с ID " + adId + " удален из корзины.");
        } catch (SQLException e) {
            logger.severe("Ошибка при удалении товара из корзины: " + e.getMessage());
        }
    }

    /**
     * Очищает корзину.
     */
    public void clearBasket() {
        try {
            int userId = getLoggedInUserId();
            if (userId == -1) {
                logger.warning("Пользователь не авторизован. Невозможно очистить корзину.");
                return;
            }
            inMemoryDatabase.clearBasket(userId);
            logger.info("Корзина очищена.");
        } catch (SQLException e) {
            logger.severe("Ошибка при очистке корзины: " + e.getMessage());
        }
    }

    /**
     * Возвращает общую стоимость товаров в корзине.
     */
    public double getTotalPrice() {
        try {
            int userId = getLoggedInUserId();
            if (userId == -1) {
                logger.warning("Пользователь не авторизован. Невозможно получить общую стоимость.");
                return 0.0;
            }
            return inMemoryDatabase.getTotalPrice(userId);
        } catch (SQLException e) {
            logger.severe("Ошибка при получении общей стоимости товаров: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Возвращает количество товаров в корзине.
     */
    public int getBasketItemCount() {
        try {
            int userId = getLoggedInUserId();
            if (userId == -1) {
                logger.warning("Пользователь не авторизован. Невозможно получить количество товаров.");
                return 0;
            }
            return inMemoryDatabase.getBasketItemCount(userId);
        } catch (SQLException e) {
            logger.severe("Ошибка при получении количества товаров: " + e.getMessage());
            return 0;
        }
    }
}