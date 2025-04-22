package org.example.main.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.example.main.models.Ad;
import org.example.main.utils.Database;
import org.example.main.utils.SceneManager;
import org.example.main.utils.SessionManager;
import javafx.scene.input.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class IndexController {

    @FXML
    private Label welcomeText;

    @FXML
    private TextField searchField;

    @FXML
    private ListView<Ad> adsList;

    @FXML
    private HBox authBlock;

    @FXML
    private HBox userBlock;

    @FXML
    private Label usernameLabel;

    /**
     * Инициализация контроллера.
     */

    @FXML
    public void initialize() {
        welcomeText.setText("Добро пожаловать в приложение!");

        if (adsList != null && adsList.getScene() != null) {
            adsList.getScene().getStylesheets().add(getClass().getResource("/org/css/styles.css").toExternalForm());
        }

        updateUIBasedOnAuthStatus();

        loadAds();

        adsList.setCellFactory(param -> new ListCell<Ad>() {
            @Override
            protected void updateItem(Ad item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Text titleText = new Text(item.getTitle());
                    Text priceText = new Text(String.format("%.2f руб.", item.getPrice()));
                    Text locationText = new Text(item.getLocation());

                    Button buyButton = new Button("Купить");
                    buyButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                    buyButton.setOnAction(event -> {
                        System.out.println("Покупка товара: " + item.getTitle());
                        IndexController.handleBuy(item.getAdId());
                    });

                    Button messageButton = new Button("Написать продавцу");
                    messageButton.setOnAction(event -> openChatWithSeller(item.getSellerId()));


                    if (item.getSellerId() == SessionManager.getLoggedInUserId()) {
                        titleText.setFill(Color.RED);
                    } else {
                        titleText.setFill(Color.BLACK);
                    }

                    HBox hbox = new HBox(10, titleText, priceText, locationText, buyButton, messageButton);
                    hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    setGraphic(hbox);
                }
            }
        });
    }
    /**
     * Получение или создание чата между текущим пользователем и продавцом.
     */
    private int getOrCreateChat(int user1Id, int user2Id) throws Exception {
        try (Connection conn = Database.getConnection()) {
            String checkQuery = "SELECT CHAT_ID FROM CHATS WHERE (USER1_ID = ? AND USER2_ID = ?) OR (USER1_ID = ? AND USER2_ID = ?)";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, user1Id);
            checkStmt.setInt(2, user2Id);
            checkStmt.setInt(3, user2Id);
            checkStmt.setInt(4, user1Id);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("CHAT_ID");
            } else {

                String insertQuery = "INSERT INTO CHATS (CHAT_ID, USER1_ID, USER2_ID, LAST_MESSAGE, LAST_MESSAGE_TIME) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                int chatId = generateChatId();
                insertStmt.setInt(1, chatId);
                insertStmt.setInt(2, user1Id);
                insertStmt.setInt(3, user2Id);
                insertStmt.setString(4, "Новый чат");
                insertStmt.executeUpdate();
                return chatId;
            }
        }
    }

    /**
     * Генерация уникального ID чата.
     */
    private int generateChatId() {
        return (int) (Math.random() * 1_000_000);
    }
    private void openChatWithSeller(int sellerId) {
        try {
            int currentUserId = SessionManager.getLoggedInUserId();
            int chatId = getOrCreateChat(currentUserId, sellerId);


            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/main/chat.fxml"));
            Parent root = loader.load();

            ChatController chatController = loader.getController();
            chatController.setChatId(chatId);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Чат");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть чат.");
        }
    }

    @FXML
    private void openProfile() {
        try {
            SceneManager.showScene("profile");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить страницу профиля.");
        }
    }

    /**
     * Открытие страницы корзины.
     */
    @FXML
    private void openBasket() {
        System.out.println("Открытие страницы корзины...");
        try {
            SceneManager.showScene("basket");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Обработка покупки товара.
     */
    public static void handleBuy(int adId) {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement checkOwnerStmt = conn.prepareStatement(
                    "SELECT USER_ID, PRICE FROM ADS WHERE AD_ID = ? AND STATUS = 'active'"
            );
            checkOwnerStmt.setInt(1, adId);
            ResultSet rs = checkOwnerStmt.executeQuery();

            if (!rs.next()) {
                System.out.println("Объявление не найдено или уже продано.");
                return;
            }

            int sellerId = rs.getInt("USER_ID");
            double price = rs.getDouble("PRICE");

            if (sellerId == SessionManager.getLoggedInUserId()) {
                System.out.println("Вы не можете купить свой собственный товар.");
                return;
            }

            addToBasket(conn, SessionManager.getLoggedInUserId(), adId);

            System.out.println("Товар успешно добавлен в корзину!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Не удалось добавить товар в корзину: " + e.getMessage());
        }
    }

    /**
     * Метод для добавления товара в корзину.
     */
    private static void addToBasket(Connection conn, int userId, int adId) throws Exception {
        String checkQuery = "SELECT BASKET_ID FROM USER_BASKET WHERE USER_ID = ? AND AD_ID = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
        checkStmt.setInt(1, userId);
        checkStmt.setInt(2, adId);
        ResultSet rs = checkStmt.executeQuery();

        if (rs.next()) {
            System.out.println("Товар с ID " + adId + " уже находится в корзине.");
            return;
        }

        String query = "INSERT INTO USER_BASKET (USER_ID, AD_ID) VALUES (?, ?)";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, userId);
        stmt.setInt(2, adId);
        stmt.executeUpdate();

        System.out.println("Товар с ID " + adId + " добавлен в корзину.");
    }

    /**
     * Получение цены товара по его ID.
     */
    private static double getPriceForAd(int adId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT PRICE FROM ADS WHERE AD_ID = ?");
            stmt.setInt(1, adId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("PRICE");
            } else {
                throw new IllegalArgumentException("Объявление с ID " + adId + " не найдено.");
            }
        }
    }

    /**
     * Загрузка объявлений из базы данных.
     */
    private void loadAds() {
        try (Connection conn = Database.getConnection()) {
            String query = "SELECT AD_ID, TITLE, PRICE, DESCRIPTION, IMAGE_PATH, LOCATION, STATUS, USER_ID FROM ADS";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            adsList.getItems().clear();
            while (rs.next()) {
                int adId = rs.getInt("AD_ID");
                String title = rs.getString("TITLE");
                double price = rs.getDouble("PRICE");
                String description = rs.getString("DESCRIPTION");
                byte[] imageBytes = rs.getBytes("IMAGE_PATH");
                String location = rs.getString("LOCATION");
                String status = rs.getString("STATUS");
                int sellerId = rs.getInt("USER_ID");

                System.out.println("Loaded AD: ID=" + adId + ", Title=" + title + ", Status=" + status);

                boolean isAvailable = "active".equals(status);
                boolean isCurrentUserSeller = sellerId == SessionManager.getLoggedInUserId();

                if (isAvailable && !isCurrentUserSeller) {
                    Ad ad = new Ad();
                    ad.setAdId(adId);
                    ad.setTitle(title);
                    ad.setPrice(price);
                    ad.setDescription(description);
                    ad.setImage(imageBytes);
                    ad.setLocation(location);
                    ad.setStatus(status);
                    ad.setSellerId(sellerId);

                    adsList.getItems().add(ad);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить объявления: " + e.getMessage());
        }
    }

    /**
     * Обработка нажатия на кнопку "Поиск".
     */
    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        if (query.isEmpty()) {
            loadAds();
            return;
        }

        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT AD_ID, TITLE, PRICE, DESCRIPTION, IMAGE_PATH, LOCATION, STATUS, USER_ID FROM ADS WHERE LOWER(TITLE) LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + query.toLowerCase() + "%");
            ResultSet rs = stmt.executeQuery();

            adsList.getItems().clear();
            while (rs.next()) {
                int adId = rs.getInt("AD_ID");
                String title = rs.getString("TITLE");
                double price = rs.getDouble("PRICE");
                String description = rs.getString("DESCRIPTION");
                byte[] imageBytes = rs.getBytes("IMAGE_PATH");
                String location = rs.getString("LOCATION");
                String status = rs.getString("STATUS");
                int sellerId = rs.getInt("USER_ID");

                boolean isAvailable = "active".equals(status);
                boolean isCurrentUserSeller = sellerId == SessionManager.getLoggedInUserId();

                if (isAvailable && !isCurrentUserSeller) {
                    Ad ad = new Ad();
                    ad.setAdId(adId);
                    ad.setTitle(title);
                    ad.setPrice(price);
                    ad.setDescription(description);
                    ad.setImage(imageBytes);
                    ad.setLocation(location);
                    adsList.getItems().add(ad);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось выполнить поиск.");
        }
    }

    /**
     * Обработка нажатия на кнопку "Создать объявление".
     */
    @FXML
    private void createAd() {
        try {
            SceneManager.showScene("create_ad");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить страницу создания объявления.");
        }
    }

    /**
     * Обработка нажатия на кнопку "Войти".
     */
    @FXML
    private void handleLogin() {
        try {
            SceneManager.showScene("login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Обработка нажатия на кнопку "Зарегистрироваться".
     */
    @FXML
    private void handleRegister() {
        try {
            SceneManager.showScene("register");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Обработка нажатия на кнопку "Выйти".
     */
    @FXML
    private void handleLogout() {
        SessionManager.logout();
        updateUIBasedOnAuthStatus();
    }

    /**
     * Открытие окна чата.
     */
    @FXML
    private void openChatWindow() {
        try {
            SceneManager.showScene("chat");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Обновление интерфейса в зависимости от состояния авторизации.
     */
    private void updateUIBasedOnAuthStatus() {
        if (SessionManager.isLoggedIn()) {
            authBlock.setVisible(false);
            authBlock.setManaged(false);
            userBlock.setVisible(true);
            userBlock.setManaged(true);
            usernameLabel.setText(SessionManager.getLoggedInUsername());
        } else {
            authBlock.setVisible(true);
            authBlock.setManaged(true);
            userBlock.setVisible(false);
            userBlock.setManaged(false);
        }
    }

    /**
     * Отображение диалогового окна с сообщением.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleAdDoubleClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            Ad selectedAd = adsList.getSelectionModel().getSelectedItem();
            if (selectedAd != null) {
                try {

                    SceneManager.showSceneWithParameter("ad_details", "adId", selectedAd.getAdId());
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Ошибка", "Не удалось открыть описание объявления.");
                }
            } else {
                showAlert("Ошибка", "Выберите объявление для просмотра.");
            }
        }
    }
}