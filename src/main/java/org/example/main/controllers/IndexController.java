package org.example.main.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.example.main.models.Ad;
import org.example.main.utils.SceneManager;
import org.example.main.utils.SessionManager;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class IndexController {
    @FXML
    private Button adminButton;
    @FXML
    public Label welcomeText;

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

    private final SceneManager sceneManager = SceneManager.getInstance();

    private final InMemoryDatabase inMemoryDatabase = new InMemoryDatabase();

    private final SessionManager sessionManager = SessionManager.getInstance();

    public void refreshAds() {
        loadAds();
    }
    /**
     * Инициализация контроллера.
     */
    @FXML
    public void initialize() {
        welcomeText.setText("Добро пожаловать в приложение!");
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
                    buyButton.setOnAction(event -> handleBuy(item.getAdId()));

                    Button messageButton = new Button("Написать продавцу");
                    messageButton.setOnAction(event -> openChatWithSeller(item.getSellerId()));

                    if (item.getSellerId() == sessionManager.getLoggedInUserId()) {
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

    private void updateUIBasedOnAuthStatus() {
        boolean loggedIn = sessionManager.isLoggedIn();
        authBlock.setVisible(!loggedIn);
        authBlock.setManaged(!loggedIn);
        userBlock.setVisible(loggedIn);
        userBlock.setManaged(loggedIn);

        if (loggedIn) {
            usernameLabel.setText(sessionManager.getLoggedInUsername());
            adminButton.setVisible(sessionManager.isAdmin());
        }
    }

    private boolean checkIfAdmin() {
        try {
            int userId = sessionManager.getLoggedInUserId();
            if (userId == -1) return false;

            String query = "SELECT IS_ADMIN FROM USERS WHERE USER_ID = ?";
            try (PreparedStatement stmt = InMemoryDatabase.getInstance().getConnection().prepareStatement(query)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                return rs.next() && rs.getBoolean("IS_ADMIN");
            }
        } catch (SQLException e) {
            System.err.println("Error checking admin status: " + e.getMessage());
            return false;
        }
    }

    @FXML
    private void openDatabaseView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/main/database_view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Просмотр базы данных");
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть просмотр БД");
        }
    }

    /**
     * Загрузка объявлений из базы данных.
     */
    public void loadAds() {
        if (adsList == null) {
            System.err.println("adsList is not initialized!");
            return;
        }

        try {
            int userId = SessionManager.getInstance().getLoggedInUserId();

            List<Ad> ads = InMemoryDatabase.getInstance().loadAds();
            if (ads == null || ads.isEmpty()) {
                System.out.println("Нет доступных объявлений для загрузки.");
                return;
            }

            adsList.getItems().clear();

            for (Ad ad : ads) {
                if (ad.getSellerId() != userId && "active".equals(ad.getStatus())) {
                    adsList.getItems().add(ad);

                    logAdDetails(ad);
                }
            }

            System.out.println("Всего загружено объявлений: " + adsList.getItems().size());

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить объявления: " + e.getMessage());
        }
    }

    /**
     * Вспомогательный метод для логирования деталей объявления.
     */
    private void logAdDetails(Ad ad) {
        System.out.println("Загружено объявление: ID=" + ad.getAdId()
                + ", Title='" + ad.getTitle()
                + "', Price=" + ad.getPrice()
                + ", Status=" + ad.getStatus());
    }

    /**
     * Открытие страницы корзины.
     */
    @FXML
    private void openBasket() {
        try {
            sceneManager.showScene("basket");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть страницу корзины.");
        }
    }
    @FXML
    private void openChatWindow() {
        try {
            sceneManager.showScene("chat");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть страницу корзины.");
        }
    }
    @FXML
    private void openProfile() {
        try {
            sceneManager.showScene("profile");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть страницу корзины.");
        }
    }
    @FXML
    private void createAd() {
        try {
            sceneManager.showScene("create_ad");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть страницу корзины.");
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

        try {
            int userId = sessionManager.getLoggedInUserId();
            List<Ad> ads = inMemoryDatabase.searchAds(query, userId);
            adsList.getItems().clear();
            adsList.getItems().addAll(ads);
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось выполнить поиск: " + e.getMessage());
        }
    }

    /**
     * Обработка покупки товара.
     */
    private void handleBuy(int adId) {
        try {
            int userId = sessionManager.getLoggedInUserId();
            double price = inMemoryDatabase.getPriceForAd(adId);

            if (price == -1) {
                showAlert("Ошибка", "Товар не найден.");
                return;
            }

            inMemoryDatabase.addToBasket(userId, adId);
            showAlert("Успех", "Товар успешно добавлен в корзину.");
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось добавить товар в корзину: " + e.getMessage());
        }
    }

    /**
     * Открытие чата с продавцом.
     */
    private void openChatWithSeller(int sellerId) {
        try {
            int currentUserId = sessionManager.getLoggedInUserId();
            int chatId = inMemoryDatabase.getOrCreateChat(currentUserId, sellerId);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/main/chat.fxml"));
            Parent root = loader.load();
            ChatController chatController = loader.getController();
            chatController.setChatId(chatId);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Чат");
            stage.show();
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось открыть чат: " + e.getMessage());
        }
    }

    /**
     * Обработка нажатия на кнопку "Войти".
     */
    @FXML
    private void handleLogin() {
        try {
            sceneManager.showScene("login");
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось загрузить страницу входа.");
        }
    }

    /**
     * Обработка нажатия на кнопку "Зарегистрироваться".
     */
    @FXML
    private void handleRegister() {
        try {
            sceneManager.showScene("register");
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось загрузить страницу регистрации.");
            e.printStackTrace(); // Печатает полный стек вызовов ошибки в консоль
            System.err.println("Причина ошибки: " + e.getMessage()); // Печатает только сообщение об ошибке
            showAlert("Ошибка", "Не удалось загрузить страницу регистрации. Причина: " + e.getMessage());

        }
    }

    /**
     * Обработка нажатия на кнопку "Выйти".
     */
    @FXML
    private void handleLogout() {
        try {
            sessionManager.logout();
            updateUIBasedOnAuthStatus();
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось выйти: " + e.getMessage());
        }
    }

    /**
     * Обновление интерфейса в зависимости от состояния авторизации.
     */
    /**
     * Обновление интерфейса в зависимости от состояния авторизации.
     */

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

    /**
     * Обработка двойного клика по объявлению.
     */
    @FXML
    private void handleAdDoubleClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            Ad selectedAd = adsList.getSelectionModel().getSelectedItem();
            if (selectedAd != null) {
                try {
                    sceneManager.showSceneWithParameters("ad_details", "adId", selectedAd.getAdId());
                } catch (Exception e) {
                    showAlert("Ошибка", "Не удалось открыть описание объявления.");
                }
            } else {
                showAlert("Ошибка", "Выберите объявление для просмотра.");
            }
        }
    }
}