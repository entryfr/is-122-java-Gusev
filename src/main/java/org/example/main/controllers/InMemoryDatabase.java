package org.example.main.controllers;

import org.example.main.models.Ad;
import org.example.main.models.Chat;
import org.example.main.models.Message;
import org.example.main.models.User;
import org.example.main.utils.SessionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InMemoryDatabase {
    protected Connection connection;
    private static InMemoryDatabase instance;
    public InMemoryDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite::memory:");
            System.out.println("In-memory database created.");
            createTables();
            populateCategories();
            populateCities();
        } catch (SQLException e) {
            System.err.println("Error creating in-memory database: " + e.getMessage());
        }
    }
    public static synchronized InMemoryDatabase getInstance() {
        if (instance == null) {
            instance = new InMemoryDatabase();
        }
        return instance;
    }
    public void createTables() throws SQLException {
        createTableAds();
        createTableAllCity();
        createTableCategories();
        createTableChats();
        createTableMessages();
        createTablePurchases();
        createTableSales();
        createTableSessions();
        createTableUsers();
        createTableUserBasket();
    }

    void createTableAds() {
        String sql = """
            CREATE TABLE IF NOT EXISTS ADS (
                AD_ID INTEGER NOT NULL,
                USER_ID INTEGER NOT NULL,
                CATEGORY_ID INTEGER NOT NULL,
                TITLE TEXT NOT NULL,
                DESCRIPTION TEXT,
                PRICE REAL,
                LOCATION TEXT,
                PUBLICATION_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                EXPIRATION_DATE TIMESTAMP,
                STATUS TEXT DEFAULT 'active',
                IMAGE_PATH BLOB,
                PRIMARY KEY (AD_ID),
                FOREIGN KEY (USER_ID) REFERENCES USERS (USER_ID),
                FOREIGN KEY (CATEGORY_ID) REFERENCES CATEGORIES (CATEGORY_ID)
            );
        """;
        executeUpdate(sql);
        System.out.println("Table ADS created.");
    }
    void createTableAllCity() {
        String sql = """
            CREATE TABLE IF NOT EXISTS ALLCITY (
                REGION TEXT,
                MUN_DISTRICT TEXT,
                CITY TEXT
            );
        """;
        executeUpdate(sql);
        System.out.println("Table ALLCITY created.");
    }
    public void populateCities() throws SQLException {
        String query = "INSERT OR IGNORE INTO ALLCITY (REGION, MUN_DISTRICT, CITY) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            // Добавляем популярные города России
            insertCity(stmt, "Москва", "Центральный федеральный округ", "Москва");
            insertCity(stmt, "Санкт-Петербург", "Северо-Западный федеральный округ", "Санкт-Петербург");
            insertCity(stmt, "Новосибирск", "Сибирский федеральный округ", "Новосибирск");
            insertCity(stmt, "Екатеринбург", "Уральский федеральный округ", "Екатеринбург");
            insertCity(stmt, "Казань", "Приволжский федеральный округ", "Казань");
            insertCity(stmt, "Нижний Новгород", "Приволжский федеральный округ", "Нижний Новгород");
            insertCity(stmt, "Челябинск", "Уральский федеральный округ", "Челябинск");
            insertCity(stmt, "Самара", "Приволжский федеральный округ", "Самара");
            insertCity(stmt, "Омск", "Сибирский федеральный округ", "Омск");
            insertCity(stmt, "Ростов-на-Дону", "Южный федеральный округ", "Ростов-на-Дону");
            insertCity(stmt, "Уфа", "Приволжский федеральный округ", "Уфа");
            insertCity(stmt, "Красноярск", "Сибирский федеральный округ", "Красноярск");
            insertCity(stmt, "Пермь", "Приволжский федеральный округ", "Пермь");
            insertCity(stmt, "Воронеж", "Центральный федеральный округ", "Воронеж");
            insertCity(stmt, "Волгоград", "Южный федеральный округ", "Волгоград");
            insertCity(stmt, "Краснодар", "Южный федеральный округ", "Краснодар");
            insertCity(stmt, "Саратов", "Приволжский федеральный округ", "Саратов");
            insertCity(stmt, "Тюмень", "Уральский федеральный округ", "Тюмень");
            insertCity(stmt, "Тольятти", "Приволжский федеральный округ", "Тольятти");
            insertCity(stmt, "Ижевск", "Приволжский федеральный округ", "Ижевск");
            insertCity(stmt, "Барнаул", "Сибирский федеральный округ", "Барнаул");
            insertCity(stmt, "Ульяновск", "Приволжский федеральный округ", "Ульяновск");
            insertCity(stmt, "Иркутск", "Сибирский федеральный округ", "Иркутск");
            insertCity(stmt, "Хабаровск", "Дальневосточный федеральный округ", "Хабаровск");
            insertCity(stmt, "Ярославль", "Центральный федеральный округ", "Ярославль");
            insertCity(stmt, "Владивосток", "Дальневосточный федеральный округ", "Владивосток");
            insertCity(stmt, "Махачкала", "Северо-Кавказский федеральный округ", "Махачкала");
            insertCity(stmt, "Томск", "Сибирский федеральный округ", "Томск");
            insertCity(stmt, "Оренбург", "Приволжский федеральный округ", "Оренбург");
            insertCity(stmt, "Кемерово", "Сибирский федеральный округ", "Кемерово");
            insertCity(stmt, "Новокузнецк", "Сибирский федеральный округ", "Новокузнецк");
            insertCity(stmt, "Рязань", "Центральный федеральный округ", "Рязань");
            insertCity(stmt, "Астрахань", "Южный федеральный округ", "Астрахань");
            insertCity(stmt, "Набережные Челны", "Приволжский федеральный округ", "Набережные Челны");
            insertCity(stmt, "Пенза", "Приволжский федеральный округ", "Пенза");
            insertCity(stmt, "Липецк", "Центральный федеральный округ", "Липецк");
            insertCity(stmt, "Киров", "Приволжский федеральный округ", "Киров");
            insertCity(stmt, "Чебоксары", "Приволжский федеральный округ", "Чебоксары");
            insertCity(stmt, "Тула", "Центральный федеральный округ", "Тула");
            insertCity(stmt, "Калининград", "Северо-Западный федеральный округ", "Калининград");
            insertCity(stmt, "Балашиха", "Центральный федеральный округ", "Балашиха");
            insertCity(stmt, "Курск", "Центральный федеральный округ", "Курск");
            insertCity(stmt, "Севастополь", "Южный федеральный округ", "Севастополь");
            insertCity(stmt, "Сочи", "Южный федеральный округ", "Сочи");
            insertCity(stmt, "Ставрополь", "Северо-Кавказский федеральный округ", "Ставрополь");
            insertCity(stmt, "Улан-Удэ", "Дальневосточный федеральный округ", "Улан-Удэ");
            insertCity(stmt, "Магнитогорск", "Уральский федеральный округ", "Магнитогорск");
            insertCity(stmt, "Тверь", "Центральный федеральный округ", "Тверь");
            insertCity(stmt, "Иваново", "Центральный федеральный округ", "Иваново");
            insertCity(stmt, "Брянск", "Центральный федеральный округ", "Брянск");
            stmt.executeBatch();
            System.out.println("Таблица городов успешно заполнена.");
        }
    }

    private void insertCity(PreparedStatement stmt, String city, String region, String munDistrict) throws SQLException {
        stmt.setString(1, region);
        stmt.setString(2, munDistrict);
        stmt.setString(3, city);
        stmt.addBatch();
    }
    // Метод для получения списка городов
    public List<String> getCities() throws SQLException {
        List<String> cities = new ArrayList<>();
        String query = "SELECT DISTINCT CITY FROM ALLCITY ORDER BY CITY";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cities.add(rs.getString("CITY"));
            }
        }
        return cities;
    }
    void createTableCategories() {
        String sql = """
            CREATE TABLE IF NOT EXISTS CATEGORIES (
                CATEGORY_ID INTEGER NOT NULL,
                CATEGORY_NAME TEXT NOT NULL,
                PARENT_CATEGORY_ID INTEGER,
                PRIMARY KEY (CATEGORY_ID),
                UNIQUE (CATEGORY_NAME),
                FOREIGN KEY (PARENT_CATEGORY_ID) REFERENCES CATEGORIES (CATEGORY_ID)
            );
        """;
        executeUpdate(sql);
        System.out.println("Table CATEGORIES created.");
    }

    void createTableChats() {
        String sql = """
            CREATE TABLE IF NOT EXISTS CHATS (
                CHAT_ID INTEGER NOT NULL,
                USER1_ID INTEGER NOT NULL,
                USER2_ID INTEGER NOT NULL,
                LAST_MESSAGE TEXT,
                LAST_MESSAGE_TIME TIMESTAMP,
                PRIMARY KEY (CHAT_ID)
            );
        """;
        executeUpdate(sql);
        System.out.println("Table CHATS created.");
    }

    void createTableMessages() {
        String sql = """
            CREATE TABLE IF NOT EXISTS MESSAGES (
                MESSAGE_ID TEXT NOT NULL,
                CHAT_ID INTEGER NOT NULL,
                SENDER_ID INTEGER NOT NULL,
                MESSAGE_TEXT TEXT,
                SENT_TIME TIMESTAMP,
                PRIMARY KEY (MESSAGE_ID)
            );
        """;
        executeUpdate(sql);
        System.out.println("Table MESSAGES created.");
    }

    void createTablePurchases() {
        String sql = """
            CREATE TABLE IF NOT EXISTS PURCHASES (
                PURCHASE_ID INTEGER NOT NULL,
                USER_ID INTEGER,
                AD_ID INTEGER,
                PRICE REAL,
                PURCHASE_DATE TIMESTAMP,
                PRIMARY KEY (PURCHASE_ID),
                FOREIGN KEY (USER_ID) REFERENCES USERS (USER_ID),
                FOREIGN KEY (AD_ID) REFERENCES ADS (AD_ID)
            );
        """;
        executeUpdate(sql);
        System.out.println("Table PURCHASES created.");
    }

    void createTableSales() {
        String sql = """
            CREATE TABLE IF NOT EXISTS SALES (
                SALE_ID INTEGER NOT NULL,
                USER_ID INTEGER,
                AD_ID INTEGER,
                PRICE REAL,
                SALE_DATE TIMESTAMP,
                PRIMARY KEY (SALE_ID),
                FOREIGN KEY (USER_ID) REFERENCES USERS (USER_ID),
                FOREIGN KEY (AD_ID) REFERENCES ADS (AD_ID)
            );
        """;
        executeUpdate(sql);
        System.out.println("Table SALES created.");
    }

    /**
     * Создание таблицы SESSIONS.
     */

    public void createTableSessions() {
        String sql = """
        CREATE TABLE IF NOT EXISTS SESSIONS (
                          SESSION_ID VARCHAR(255) NOT NULL,
                          USER_ID INTEGER NOT NULL,
                          USERNAME TEXT NOT NULL,
                          DATA BLOB SUB_TYPE TEXT,
                          EXPIRY TIMESTAMP,
                          PRIMARY KEY (SESSION_ID)
                      );
    """;
        executeUpdate(sql);
        System.out.println("Table SESSIONS created.");
    }

    void createTableUsers() {
        String sql = """
        CREATE TABLE IF NOT EXISTS USERS (
            USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            USERNAME TEXT NOT NULL UNIQUE,
            EMAIL TEXT NOT NULL UNIQUE,
            PASSWORD_HASH TEXT NOT NULL,
            FIRST_NAME TEXT,
            LAST_NAME TEXT,
            PHONE TEXT,
            IS_ADMIN BOOLEAN DEFAULT FALSE,
            REGISTRATION_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
            LAST_LOGIN TIMESTAMP
        );
    
        CREATE INDEX IF NOT EXISTS idx_users_username ON USERS(USERNAME);
        CREATE INDEX IF NOT EXISTS idx_users_email ON USERS(EMAIL);
    """;
        executeUpdate(sql);
        System.out.println("Table USERS created.");
    }

    void createTableUserBasket() {
        String sql = """
            CREATE TABLE IF NOT EXISTS USER_BASKET (
                BASKET_ID INTEGER NOT NULL,
                USER_ID INTEGER NOT NULL,
                AD_ID INTEGER NOT NULL,
                PRIMARY KEY (BASKET_ID),
                FOREIGN KEY (USER_ID) REFERENCES USERS (USER_ID),
                FOREIGN KEY (AD_ID) REFERENCES ADS (AD_ID)
            );
        """;
        executeUpdate(sql);
        System.out.println("Table USER_BASKET created.");
    }

    private void executeUpdate(String sql) {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error executing SQL: " + e.getMessage());
        }
    }

    /**
     * Получение текущего подключения к базе данных.
     */
    public Connection getConnection() {
        return connection;
    }

    public int getOtherUserIdInChat(int chatId, int currentUserId) throws SQLException {
        String query = "SELECT USER1_ID, USER2_ID FROM CHATS WHERE CHAT_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, chatId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int user1 = rs.getInt("USER1_ID");
                int user2 = rs.getInt("USER2_ID");
                return user1 == currentUserId ? user2 : user1;
            }
            throw new SQLException("Чат не найден");
        }
    }

    public String getUsernameById(int userId) throws SQLException {
        String query = "SELECT USERNAME FROM USERS WHERE USER_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("USERNAME");
            }
            throw new SQLException("Пользователь не найден");
        }
    }

    /**
     * Получение всех активных объявлений для текущего пользователя.
     */
    public List<Ad> getAllActiveAdsForCurrentUser(int userId) {
        List<Ad> ads = new ArrayList<>();
        String query = """
            SELECT AD_ID, TITLE, PRICE, DESCRIPTION, IMAGE_PATH, LOCATION, STATUS, USER_ID
            FROM ADS
            WHERE STATUS = 'active' AND USER_ID != ?
        """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Ad ad = new Ad();
                ad.setAdId(rs.getInt("AD_ID"));
                ad.setTitle(rs.getString("TITLE"));
                ad.setPrice(rs.getDouble("PRICE"));
                ad.setDescription(rs.getString("DESCRIPTION"));
                ad.setImage(rs.getBytes("IMAGE_PATH"));
                ad.setLocation(rs.getString("LOCATION"));
                ad.setStatus(rs.getString("STATUS"));
                ad.setSellerId(rs.getInt("USER_ID"));
                ads.add(ad);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching active ads: " + e.getMessage());
        }
        return ads;
    }

    /**
     * Добавление товара в корзину.
     */
    public void addToBasket(int userId, int adId) {
        String checkQuery = "SELECT BASKET_ID FROM USER_BASKET WHERE USER_ID = ? AND AD_ID = ?";
        String insertQuery = "INSERT INTO USER_BASKET (USER_ID, AD_ID) VALUES (?, ?)";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
             PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, adId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                System.out.println("Товар с ID " + adId + " уже находится в корзине.");
                return;
            }
            insertStmt.setInt(1, userId);
            insertStmt.setInt(2, adId);
            insertStmt.executeUpdate();
            System.out.println("Товар с ID " + adId + " добавлен в корзину.");
        } catch (SQLException e) {
            System.err.println("Error adding item to basket: " + e.getMessage());
        }
    }

    /**
     * Получение или создание чата между двумя пользователями.
     */
    public int getOrCreateChat(int user1Id, int user2Id) {
        String checkQuery = "SELECT CHAT_ID FROM CHATS WHERE (USER1_ID = ? AND USER2_ID = ?) OR (USER1_ID = ? AND USER2_ID = ?)";
        String insertQuery = "INSERT INTO CHATS (CHAT_ID, USER1_ID, USER2_ID, LAST_MESSAGE, LAST_MESSAGE_TIME) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
             PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
            checkStmt.setInt(1, user1Id);
            checkStmt.setInt(2, user2Id);
            checkStmt.setInt(3, user2Id);
            checkStmt.setInt(4, user1Id);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("CHAT_ID");
            }
            int chatId = generateChatId();
            insertStmt.setInt(1, chatId);
            insertStmt.setInt(2, user1Id);
            insertStmt.setInt(3, user2Id);
            insertStmt.setString(4, "Новый чат");
            insertStmt.executeUpdate();
            System.out.println("Создан новый чат с ID: " + chatId);
            return chatId;
        } catch (SQLException e) {
            System.err.println("Error getting or creating chat: " + e.getMessage());
            return -1;
        }
    }
    public List<Ad> getFilteredAds(int currentUserId, String categoryName, Double minPrice, Double maxPrice) throws SQLException {
        List<Ad> ads = new ArrayList<>();

        StringBuilder query = new StringBuilder("""
        SELECT AD_ID, TITLE, PRICE, DESCRIPTION, IMAGE_PATH, LOCATION, STATUS, USER_ID, CATEGORY_ID
        FROM ADS
        WHERE STATUS = 'active' AND USER_ID != ?
    """);

        List<Object> params = new ArrayList<>();
        params.add(currentUserId);

        if (categoryName != null) {
            query.append(" AND CATEGORY_ID = (SELECT CATEGORY_ID FROM CATEGORIES WHERE CATEGORY_NAME = ?)");
            params.add(categoryName);
        }

        if (minPrice != null) {
            query.append(" AND PRICE >= ?");
            params.add(minPrice);
        }

        if (maxPrice != null) {
            query.append(" AND PRICE <= ?");
            params.add(maxPrice);
        }

        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    stmt.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    stmt.setInt(i + 1, (Integer) param);
                } else if (param instanceof Double) {
                    stmt.setDouble(i + 1, (Double) param);
                }
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Ad ad = new Ad();
                ad.setAdId(rs.getInt("AD_ID"));
                ad.setTitle(rs.getString("TITLE"));
                ad.setPrice(rs.getDouble("PRICE"));
                ad.setDescription(rs.getString("DESCRIPTION"));
                ad.setImage(rs.getBytes("IMAGE_PATH"));
                ad.setLocation(rs.getString("LOCATION"));
                ad.setStatus(rs.getString("STATUS"));
                ad.setSellerId(rs.getInt("USER_ID"));
                ads.add(ad);
            }
        }

        return ads;
    }
    /**
     * Генерация уникального ID чата.
     */
    private int generateChatId() {
        return (int) (Math.random() * 1_000_000);
    }

    /**
     * Поиск объявлений по запросу.
     */
    public List<Ad> searchAds(String query, int userId) {
        List<Ad> ads = new ArrayList<>();
        String sql = """
            SELECT AD_ID, TITLE, PRICE, DESCRIPTION, IMAGE_PATH, LOCATION, STATUS, USER_ID
            FROM ADS
            WHERE LOWER(TITLE) LIKE ? AND STATUS = 'active' AND USER_ID != ?
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + query.toLowerCase() + "%");
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Ad ad = new Ad();
                ad.setAdId(rs.getInt("AD_ID"));
                ad.setTitle(rs.getString("TITLE"));
                ad.setPrice(rs.getDouble("PRICE"));
                ad.setDescription(rs.getString("DESCRIPTION"));
                ad.setImage(rs.getBytes("IMAGE_PATH"));
                ad.setLocation(rs.getString("LOCATION"));
                ad.setStatus(rs.getString("STATUS"));
                ad.setSellerId(rs.getInt("USER_ID"));
                ads.add(ad);
            }
        } catch (SQLException e) {
            System.err.println("Error searching ads: " + e.getMessage());
        }
        return ads;
    }

    /**
     * Загрузка всех объявлений.
     */
    public List<Ad> loadAds() {
        List<Ad> ads = new ArrayList<>();

        List<Ad> allAds = InMemoryDatabase.getInstance().getAllActiveAdsForCurrentUser(SessionManager.getInstance().getLoggedInUserId());

        for (Ad ad : allAds) {
            if ("active".equals(ad.getStatus())) {
                ads.add(ad);
            }
        }

        return ads;
    }

    /**
     * Проверка, занято ли имя пользователя.
     */
    public boolean isUsernameTaken(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM USERS WHERE USERNAME = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    /**
     * Проверка, занят ли email.
     */
    public boolean isEmailTaken(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM USERS WHERE EMAIL = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }



    /**
     * Добавление нового пользователя.
     */
    public int addUser(String username, String email, String password, String firstName, String lastName, String phone) throws SQLException {
        String query = """
        INSERT INTO USERS (USERNAME, EMAIL, PASSWORD_HASH, FIRST_NAME, LAST_NAME, PHONE)
        VALUES (?, ?, ?, ?, ?, ?)
    """;
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.setString(4, firstName);
            stmt.setString(5, lastName);
            stmt.setString(6, phone);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("Не удалось получить USER_ID.");
        }
    }
    /**
     * Аутентификация пользователя.
     */
    public int authenticateUser(String username, String password) throws SQLException {
        String query = "SELECT USER_ID, IS_ADMIN FROM USERS WHERE USERNAME = ? AND PASSWORD_HASH = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("USER_ID");
                boolean isAdmin = rs.getBoolean("IS_ADMIN");
                if (isAdmin) {
                    System.out.println("Администратор вошел в систему");
                }
                return userId;
            }
        }
        return -1;
    }
    /**
     * Установка данных авторизованного пользователя.
     */
    public void setLoggedInUser(String username, int userId) throws SQLException {
        String query = "INSERT OR REPLACE INTO SESSIONS (SESSION_ID, USER_ID, USERNAME) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "current_session");
            stmt.setInt(2, userId);
            stmt.setString(3, username);
            stmt.executeUpdate();
        }
    }

    /**
     * Проверка, авторизован ли пользователь.
     */
    public boolean isLoggedIn() throws SQLException {
        String query = "SELECT COUNT(*) FROM SESSIONS WHERE SESSION_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "current_session");
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    /**
     * Получение имени авторизованного пользователя.
     */
    public String getLoggedInUsername() throws SQLException {
        String query = "SELECT USERNAME FROM SESSIONS WHERE SESSION_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "current_session");
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("USERNAME") : null;
        }
    }

    /**
     * Получение ID авторизованного пользователя.
     */
    public int getLoggedInUserId() throws SQLException {
        if (!isLoggedIn()) {
            return -1;
        }

        String query = "SELECT USER_ID FROM SESSIONS WHERE SESSION_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "current_session");
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("USER_ID") : -1;
        }
    }

    /**
     * Выход пользователя.
     */
    public void logout() throws SQLException {
        String query = "DELETE FROM SESSIONS WHERE SESSION_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "current_session");
            stmt.executeUpdate();
        }
    }



    /**
     * Получение списка товаров в корзине.
     */
    public List<Ad> getBasketItems(int userId) throws SQLException {
        List<Ad> items = new ArrayList<>();
        String query = """
        SELECT AD.AD_ID, AD.TITLE, AD.PRICE, AD.USER_ID
        FROM USER_BASKET UB
        JOIN ADS AD ON UB.AD_ID = AD.AD_ID
        WHERE UB.USER_ID = ?
    """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Ad ad = new Ad();
                ad.setAdId(rs.getInt("AD_ID"));
                ad.setTitle(rs.getString("TITLE"));
                ad.setPrice(rs.getDouble("PRICE"));
                ad.setSellerId(rs.getInt("USER_ID"));
                items.add(ad);
            }
        }
        return items;
    }
    public User getUserById(int userId) throws SQLException {
        String query = "SELECT USERNAME, EMAIL, FIRST_NAME, LAST_NAME, PHONE FROM USERS WHERE USER_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUsername(rs.getString("USERNAME"));
                user.setEmail(rs.getString("EMAIL"));
                user.setFirstName(rs.getString("FIRST_NAME"));
                user.setLastName(rs.getString("LAST_NAME"));
                user.setPhone(rs.getString("PHONE"));
                return user;
            }
        }
        return null;
    }
    public boolean updateUserProfile(int userId, String email, String firstName, String lastName, String phone) throws SQLException {
        String query = "UPDATE USERS SET EMAIL = ?, FIRST_NAME = ?, LAST_NAME = ?, PHONE = ? WHERE USER_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, firstName);
            stmt.setString(3, lastName);
            stmt.setString(4, phone);
            stmt.setInt(5, userId);
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        }
    }

    public void addMessage(int chatId, int senderId, String message) throws Exception {
        String query = "INSERT INTO MESSAGES (MESSAGE_ID, CHAT_ID, SENDER_ID, MESSAGE_TEXT, SENT_TIME) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, UUID.randomUUID().toString());
            stmt.setInt(2, chatId);
            stmt.setInt(3, senderId);
            stmt.setString(4, message);
            stmt.executeUpdate();
        }
    }
    public List<Chat> getChatsForUser(int userId) throws Exception {
        List<Chat> chats = new ArrayList<>();
        String query = "SELECT CHAT_ID, LAST_MESSAGE, LAST_MESSAGE_TIME FROM CHATS WHERE USER1_ID = ? OR USER2_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Chat chat = new Chat();
                chat.setChatId(rs.getInt("CHAT_ID"));
                chat.setLastMessage(rs.getString("LAST_MESSAGE"));

                // Преобразуем Timestamp в LocalDateTime
                Timestamp timestamp = rs.getTimestamp("LAST_MESSAGE_TIME");
                if (timestamp != null) {
                    chat.setLastMessageTime(timestamp.toLocalDateTime());
                }

                chats.add(chat);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching chats for user: " + e.getMessage());
            throw new Exception("Ошибка при получении чатов пользователя.", e);
        }
        return chats;
    }

    /**
     * Заполнение таблицы категорий начальными данными.
     */
    public void populateCategories() throws SQLException {
        String query = """
        INSERT OR IGNORE INTO CATEGORIES (CATEGORY_ID, CATEGORY_NAME, PARENT_CATEGORY_ID)
        VALUES (?, ?, ?)
    """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            insertCategory(stmt, 1, "Электроника", null);
            insertCategory(stmt, 2, "Компьютеры и комплектующие", 1);
            insertCategory(stmt, 3, "Смартфоны и аксессуары", 1);
            insertCategory(stmt, 4, "Телевизоры и видео", 1);

            insertCategory(stmt, 5, "Бытовая техника", null);
            insertCategory(stmt, 6, "Холодильники", 5);
            insertCategory(stmt, 7, "Стиральные машины", 5);
            insertCategory(stmt, 8, "Пылесосы", 5);

            insertCategory(stmt, 9, "Одежда и обувь", null);
            insertCategory(stmt, 10, "Мужская одежда", 9);
            insertCategory(stmt, 11, "Женская одежда", 9);
            insertCategory(stmt, 12, "Обувь", 9);
            stmt.executeBatch();
            System.out.println("Таблица категорий успешно заполнена.");
        } catch (SQLException e) {
            System.err.println("Ошибка при заполнении категорий: " + e.getMessage());
        }
        String adminQuery = """
        INSERT OR IGNORE INTO USERS
        (USERNAME, PASSWORD_HASH, IS_ADMIN, EMAIL)
        VALUES (?, ?, ?, ?)
    """;
        try (PreparedStatement stmt = connection.prepareStatement(adminQuery)) {
            stmt.setString(1, "admin");
            stmt.setString(2, "admin");
            stmt.setBoolean(3, true);
            stmt.setString(4, "admin@example.com");
            stmt.executeUpdate();
        }
    }

    /**
     * Вспомогательный метод для вставки категории.
     */
    private void insertCategory(PreparedStatement stmt, int categoryId, String categoryName, Integer parentCategoryId) throws SQLException {
        stmt.setInt(1, categoryId);
        stmt.setString(2, categoryName);
        if (parentCategoryId != null) {
            stmt.setInt(3, parentCategoryId);
        } else {
            stmt.setNull(3, java.sql.Types.INTEGER);
        }
        stmt.addBatch();
    }
    public List<Message> getMessagesForChat(int chatId) throws SQLException {
        List<Message> messages = new ArrayList<>();
        String query = """
        SELECT M.SENDER_ID, U.USERNAME, M.MESSAGE_TEXT, M.SENT_TIME
        FROM MESSAGES M
        JOIN USERS U ON M.SENDER_ID = U.USER_ID
        WHERE M.CHAT_ID = ?
        ORDER BY M.SENT_TIME
    """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, chatId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Message message = new Message();
                message.setSenderId(rs.getInt("SENDER_ID"));
                message.setSenderName(rs.getString("USERNAME"));
                message.setMessageText(rs.getString("MESSAGE_TEXT"));

                Timestamp timestamp = rs.getTimestamp("SENT_TIME");
                if (timestamp != null) {
                    message.setSentTime(timestamp.toLocalDateTime());
                }

                messages.add(message);
            }
        }
        return messages;
    }

    public void updateLastMessage(int chatId, String lastMessage) throws Exception {
        String query = "UPDATE CHATS SET LAST_MESSAGE = ?, LAST_MESSAGE_TIME = CURRENT_TIMESTAMP WHERE CHAT_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, lastMessage);
            stmt.setInt(2, chatId);
            stmt.executeUpdate();
        }
    }
    public boolean adExists(int adId) throws SQLException {
        String query = "SELECT COUNT(*) FROM ADS WHERE AD_ID = ? AND STATUS = 'active'";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, adId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }
    public List<String> loadCategories() throws Exception {
        List<String> categories = new ArrayList<>();
        String query = "SELECT CATEGORY_NAME FROM CATEGORIES";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                categories.add(rs.getString("CATEGORY_NAME"));
            }
        }
        System.out.println("Загружены категории: " + categories);
        return categories;
    }
    public int getCategoryIdByName(String categoryName) throws SQLException {
        String query = "SELECT CATEGORY_ID FROM CATEGORIES WHERE CATEGORY_NAME = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, categoryName);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("CATEGORY_ID") : -1;
        }
    }
    private int generateUniqueId() throws SQLException {
        String query = "SELECT COALESCE(MAX(AD_ID), 0) + 1 AS NEXT_ID FROM ADS";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getInt("NEXT_ID") : 1;
        }
    }
    public void createAd(int userId, int categoryId, String title, String description, double price, String location, byte[] imageData) throws SQLException {
        String query = """
        INSERT INTO ADS (AD_ID, USER_ID, CATEGORY_ID, TITLE, DESCRIPTION, PRICE, LOCATION, IMAGE_PATH, STATUS)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            int adId = generateUniqueId();
            stmt.setInt(1, adId); // AD_ID
            stmt.setInt(2, userId); // USER_ID
            stmt.setInt(3, categoryId); // CATEGORY_ID
            stmt.setString(4, title); // TITLE
            stmt.setString(5, description); // DESCRIPTION
            stmt.setDouble(6, price); // PRICE
            stmt.setString(7, location); // LOCATION
            if (imageData != null) {
                stmt.setBytes(8, imageData); // IMAGE_PATH
            } else {
                stmt.setNull(8, java.sql.Types.BLOB);
            }
            stmt.setString(9, "active"); // STATUS
            stmt.executeUpdate();
            System.out.println("Объявление сохранено в базе данных: ID=" + adId);
        }
    }
    public void updateAdStatus(int adId) throws Exception {
        String query = "UPDATE ADS SET STATUS = 'sold' WHERE AD_ID = ? AND STATUS = 'active'";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, adId);
            stmt.executeUpdate();
        }
    }
    public void addPurchaseRecord(int purchaseId, int userId, Ad ad) throws Exception {
        String query = """
        INSERT INTO PURCHASES (PURCHASE_ID, USER_ID, AD_ID, PRICE, PURCHASE_DATE)
        VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
    """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, purchaseId);
            stmt.setInt(2, userId);
            stmt.setInt(3, ad.getAdId());
            stmt.setDouble(4, ad.getPrice());
            stmt.executeUpdate();
        }
    }

    /**
     * Удаление товара из корзины.
     */
    public void removeFromBasket(int userId, int adId) throws SQLException {
        String query = "DELETE FROM USER_BASKET WHERE USER_ID = ? AND AD_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, adId);
            stmt.executeUpdate();
        }
    }

    /**
     * Очистка корзины.
     */
    public void clearBasket(int userId) throws SQLException {
        String query = "DELETE FROM USER_BASKET WHERE USER_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    /**
     * Получение общей стоимости товаров в корзине.
     */
    public double getTotalPrice(int userId) throws SQLException {
        String query = "SELECT SUM(PRICE) AS TOTAL_PRICE FROM ADS WHERE AD_ID IN (SELECT AD_ID FROM USER_BASKET WHERE USER_ID = ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getDouble("TOTAL_PRICE") : 0.0;
        }
    }

    /**
     * Получение количества товаров в корзине.
     */
    public int getBasketItemCount(int userId) throws SQLException {
        String query = "SELECT COUNT(*) FROM USER_BASKET WHERE USER_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    /**
     * Получение цены товара по его ID.
     */
    public double getPriceForAd(int adId) {
        String query = "SELECT PRICE FROM ADS WHERE AD_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, adId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("PRICE");
            } else {
                throw new IllegalArgumentException("Объявление с ID " + adId + " не найдено.");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching price for ad: " + e.getMessage());
            return -1;
        }
    }

    public List<Ad> getUserAds(int userId) throws SQLException {
        List<Ad> ads = new ArrayList<>();
        String query = """
        SELECT AD_ID, TITLE, PRICE, DESCRIPTION, LOCATION, STATUS, PUBLICATION_DATE
        FROM ADS
        WHERE USER_ID = ?
        ORDER BY PUBLICATION_DATE DESC
    """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Ad ad = new Ad();
                ad.setAdId(rs.getInt("AD_ID"));
                ad.setTitle(rs.getString("TITLE"));
                ad.setPrice(rs.getDouble("PRICE"));
                ad.setDescription(rs.getString("DESCRIPTION"));
                ad.setLocation(rs.getString("LOCATION"));
                ad.setStatus(rs.getString("STATUS"));
                ad.setPublicationDate(rs.getTimestamp("PUBLICATION_DATE").toLocalDateTime());
                ads.add(ad);
            }
        }
        return ads;
    }
}