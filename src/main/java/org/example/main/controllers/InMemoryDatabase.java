package org.example.main.controllers;

import org.example.main.models.Purchase;
import org.example.main.models.Ad;
import org.example.main.models.Chat;
import org.example.main.models.Message;
import org.example.main.models.User;
import org.example.main.utils.SessionManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InMemoryDatabase {
    protected Connection connection;
    private static InMemoryDatabase instance;
    private static final String DB_PATH ="local_database.db";//"\\\\172.20.10.2\\Database\\local_database.db";
    private static final String DB_FILE_NAME = "local_database.db";
    private static final String SCHEMA_FILE = "schema.sql";
    private static final String DATA_FILE = "data.sql";

    private InMemoryDatabase() {
        try {
            String dbUri = DB_PATH.replace("\\", "/");
            System.out.println("Путь к БД: " + dbUri);

            boolean dbExists = Files.exists(Paths.get(DB_PATH));
            System.out.println("Файл БД существует? " + dbExists);

            connection = DriverManager.getConnection("jdbc:sqlite:" + dbUri);

            if (!dbExists) {
                System.out.println("Инициализация новой БД...");
                initializeDatabase();
            } else {
                System.out.println("Подключение к существующей БД.");
            }
        } catch (SQLException | IOException e) {
            System.err.println("Ошибка при подключении к БД: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static synchronized InMemoryDatabase getInstance() {
        if (instance == null) {
            instance = new InMemoryDatabase();
        }
        return instance;
    }

    private void initializeDatabase() throws SQLException, IOException {
        executeSqlScript(readResourceFile(SCHEMA_FILE));
        System.out.println("Database tables successfully created");
        executeSqlScript(readResourceFile(DATA_FILE));
        System.out.println("Initial data successfully loaded");
    }

    private String readResourceFile(String fileName) throws IOException {
        String filePath = "C:/Users/user152/IdeaProjects/Main/" + fileName;
        return Files.readString(Paths.get(filePath));
    }
    public void addCategory(String categoryName) throws SQLException {
        String query = "INSERT INTO CATEGORIES (CATEGORY_NAME) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, categoryName);
            stmt.executeUpdate();
        }
    }

    public void addCity(String cityName) throws SQLException {
        String query = "INSERT INTO ALLCITY (CITY) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, cityName);
            stmt.executeUpdate();
        }
    }
    private void executeSqlScript(String sqlScript) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            String[] statements = sqlScript.split(";");
            for (String statement : statements) {
                statement = statement.trim();
                if (!statement.isEmpty()) {
                    stmt.executeUpdate(statement);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL script execution error: " + e.getMessage());
            throw e;
        }
    }

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

    public List<String> loadCategories() throws SQLException {
        List<String> categories = new ArrayList<>();
        String query = "SELECT CATEGORY_NAME FROM CATEGORIES";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                categories.add(rs.getString("CATEGORY_NAME"));
            }
        }
        System.out.println("Loaded categories: " + categories);
        return categories;
    }

    public int getOtherUserIdInChat(int chatId, int currentUserId) throws SQLException {
        String query = "SELECT USER1_ID, USER2_ID FROM CHATS WHERE CHAT_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(

                    1, chatId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int user1 = rs.getInt("USER1_ID");
                int user2 = rs.getInt("USER2_ID");
                return user1 == currentUserId ? user2 : user1;
            }
            throw new SQLException("Chat not found");
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
            throw new SQLException("User not found");
        }
    }

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

    public void addToBasket(int userId, int adId) {
        String checkQuery = "SELECT BASKET_ID FROM USER_BASKET WHERE USER_ID = ? AND AD_ID = ?";
        String insertQuery = "INSERT INTO USER_BASKET (USER_ID, AD_ID) VALUES (?, ?)";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
             PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, adId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                System.out.println("Item with ID " + adId + " is already in the basket.");
                return;
            }
            insertStmt.setInt(1, userId);
            insertStmt.setInt(2, adId);
            insertStmt.executeUpdate();
            System.out.println("Item with ID " + adId + " added to basket.");
        } catch (SQLException e) {
            System.err.println("Error adding item to basket: " + e.getMessage());
        }
    }

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
            insertStmt.setString(4, "New chat");
            insertStmt.executeUpdate();
            System.out.println("Created new chat with ID: " + chatId);
            return chatId;
        } catch (SQLException e) {
            System.err.println("Error getting or creating chat: " + e.getMessage());
            return -1;
        }
    }

    public List<Ad> getFilteredAds(int currentUserId, String categoryName, String city, Double minPrice, Double maxPrice) throws SQLException {
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
        if (city != null) {
            query.append(" AND LOCATION = ?");
            params.add(city);
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

    private int generateChatId() {
        return (int) (Math.random() * 1_000_000);
    }

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

    public boolean isUsernameTaken(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM USERS WHERE USERNAME = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public boolean isEmailTaken(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM USERS WHERE EMAIL = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

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
            throw new SQLException("Failed to retrieve USER_ID.");
        }
    }
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
                    System.out.println("Administrator logged in");
                }
                return userId;
            }
        }
        return -1;
    }

    public void setLoggedInUser(String username, int userId) throws SQLException {
        String query = "INSERT OR REPLACE INTO SESSIONS (SESSION_ID, USER_ID, USERNAME) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "current_session");
            stmt.setInt(2, userId);
            stmt.setString(3, username);
            stmt.executeUpdate();
        }
    }

    public boolean isLoggedIn() throws SQLException {
        String query = "SELECT COUNT(*) FROM SESSIONS WHERE SESSION_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "current_session");
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public String getLoggedInUsername() throws SQLException {
        String query = "SELECT USERNAME FROM SESSIONS WHERE SESSION_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "current_session");
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("USERNAME") : null;
        }
    }

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

    public void logout() throws SQLException {
        String query = "DELETE FROM SESSIONS WHERE SESSION_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "current_session");
            stmt.executeUpdate();
        }
    }

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

    public void addMessage(int chatId, int senderId, String message) throws SQLException {
        String query = "INSERT INTO MESSAGES (MESSAGE_ID, CHAT_ID, SENDER_ID, MESSAGE_TEXT, SENT_TIME) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, UUID.randomUUID().toString());
            stmt.setInt(2, chatId);
            stmt.setInt(3, senderId);
            stmt.setString(4, message);
            stmt.executeUpdate();
        }
    }

    public List<Chat> getChatsForUser(int userId) throws SQLException {
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
                Timestamp timestamp = rs.getTimestamp("LAST_MESSAGE_TIME");
                if (timestamp != null) {
                    chat.setLastMessageTime(timestamp.toLocalDateTime());
                }
                chats.add(chat);
            }
        }
        return chats;
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

    public void updateLastMessage(int chatId, String lastMessage) throws SQLException {
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
            stmt.setInt(1, adId);
            stmt.setInt(2, userId);
            stmt.setInt(3, categoryId);
            stmt.setString(4, title);
            stmt.setString(5, description);
            stmt.setDouble(6, price);
            stmt.setString(7, location);
            if (imageData != null) {
                stmt.setBytes(8, imageData);
            } else {
                stmt.setNull(8, java.sql.Types.BLOB);
            }
            stmt.setString(9, "active");
            stmt.executeUpdate();
            System.out.println("Ad saved to database: ID=" + adId);
        }
    }

    public void updateAdStatus(int adId) throws SQLException {
        String query = "UPDATE ADS SET STATUS = 'sold' WHERE AD_ID = ? AND STATUS = 'active'";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, adId);
            stmt.executeUpdate();
        }
    }

    public void addPurchaseRecord(int purchaseId, int userId, Ad ad) throws SQLException {
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

    public List<Purchase> getUserPurchases(int userId) throws SQLException {
        List<Purchase> purchases = new ArrayList<>();
        String query = """
            SELECT P.PURCHASE_ID, P.AD_ID, P.PRICE, P.PURCHASE_DATE, A.TITLE
            FROM PURCHASES P
            JOIN ADS A ON P.AD_ID = A.AD_ID
            WHERE P.USER_ID = ?
            ORDER BY P.PURCHASE_DATE DESC
        """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Purchase purchase = new Purchase();
                purchase.setPurchaseId(rs.getInt("PURCHASE_ID"));
                purchase.setAdId(rs.getInt("AD_ID"));
                purchase.setPrice(rs.getDouble("PRICE"));
                purchase.setPurchaseDate(rs.getTimestamp("PURCHASE_DATE").toLocalDateTime());
                purchase.setAdTitle(rs.getString("TITLE"));
                purchases.add(purchase);
            }
        }
        return purchases;
    }

    public void removeFromBasket(int userId, int adId) throws SQLException {
        String query = "DELETE FROM USER_BASKET WHERE USER_ID = ? AND AD_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, adId);
            stmt.executeUpdate();
        }
    }

    public void clearBasket(int userId) throws SQLException {
        String query = "DELETE FROM USER_BASKET WHERE USER_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    public double getTotalPrice(int userId) throws SQLException {
        String query = "SELECT SUM(PRICE) AS TOTAL_PRICE FROM ADS WHERE AD_ID IN (SELECT AD_ID FROM USER_BASKET WHERE USER_ID = ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getDouble("TOTAL_PRICE") : 0.0;
        }
    }

//    public int getBasketItemCount(int userId) throws SQLException {
//        String query = "SELECT COUNT(*) FROM USER_BASKET WHERE USER_ID = ?";
//        try (PreparedStatement stmt = connection.prepareStatement(query)) {
//            stmt.setInt(1, userId);
//            ResultSet rs = stmt.executeQuery();
//            return rs.next() ? rs.getInt(1) : 0;
//        }
//    }

    public List<Ad> getUserAds(int userId) throws SQLException {
        List<Ad> ads = new ArrayList<>();
        String query = """
        SELECT A.AD_ID, A.TITLE, A.PRICE, A.DESCRIPTION, A.LOCATION, A.STATUS, A.PUBLICATION_DATE, A.CATEGORY_ID, C.CATEGORY_NAME
        FROM ADS A
        LEFT JOIN CATEGORIES C ON A.CATEGORY_ID = C.CATEGORY_ID
        WHERE A.USER_ID = ?
        ORDER BY A.PUBLICATION_DATE DESC
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
                ad.setCategoryId(rs.getInt("CATEGORY_ID"));
                ad.setCategoryName(rs.getString("CATEGORY_NAME"));
                ads.add(ad);
            }
        }
        return ads;
    }
    public void updateAd(int adId, int categoryId, String title, String description, double price, String location) throws SQLException {
        String query = """
        UPDATE ADS 
        SET CATEGORY_ID = ?, TITLE = ?, DESCRIPTION = ?, PRICE = ?, LOCATION = ?
        WHERE AD_ID = ? AND STATUS = 'active'
    """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, categoryId);
            stmt.setString(2, title);
            stmt.setString(3, description);
            stmt.setDouble(4, price);
            stmt.setString(5, location);
            stmt.setInt(6, adId);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new SQLException("Не удалось обновить объявление. Возможно, оно неактивно или не существует.");
            }
            System.out.println("Объявление обновлено: ID=" + adId);
        }
    }
    public String getCategoryNameById(int categoryId) throws SQLException {
        String query = "SELECT CATEGORY_NAME FROM CATEGORIES WHERE CATEGORY_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("CATEGORY_NAME") : null;
        }
    }

    public Connection getConnection() {
        return connection;
    }
    public void deleteAd(int adId, int userId) throws SQLException {
        String query = "DELETE FROM ADS WHERE AD_ID = ? AND USER_ID = ? AND STATUS = 'active'";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, adId);
            stmt.setInt(2, userId);
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted == 0) {
                throw new SQLException("Не удалось удалить объявление. Возможно, оно не существует или не принадлежит пользователю.");
            }
            System.out.println("Объявление удалено: ID=" + adId);
        }
    }

}