package org.example.main.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.main.utils.Database;
import org.example.main.utils.SessionManager;
import org.example.main.models.Ad;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.example.main.utils.SceneManager;

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
    private void openBasket() {
        System.out.println("Открытие страницы корзины...");
        try {
            SceneManager.showScene("basket");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void initialize() {
        welcomeText.setText("Добро пожаловать в приложение!");

        updateUIBasedOnAuthStatus();


        loadAds();
    }
    public static void handleBuy(int adId) {
        try (Connection conn = Database.getConnection()) {

            PreparedStatement checkOwnerStmt = conn.prepareStatement(
                    "SELECT USER_ID FROM ADS WHERE AD_ID = ?"
            );
            checkOwnerStmt.setInt(1, adId);
            int sellerId = checkOwnerStmt.executeQuery().getInt("USER_ID");

            if (sellerId == SessionManager.getLoggedInUserId()) {
                System.out.println("Вы не можете купить свой собственный товар.");
                return;
            }

            PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE ADS SET STATUS = 'sold' WHERE AD_ID = ?"
            );
            updateStmt.setInt(1, adId);
            updateStmt.executeUpdate();

            PreparedStatement purchaseStmt = conn.prepareStatement(
                    "INSERT INTO PURCHASES (USER_ID, AD_ID, PRICE, PURCHASE_DATE) VALUES (?, ?, ?, CURRENT_TIMESTAMP)"
            );
            purchaseStmt.setInt(1, SessionManager.getLoggedInUserId());
            purchaseStmt.setInt(2, adId);
            purchaseStmt.setDouble(3, getPriceForAd(adId)); // Получаем цену товара
            purchaseStmt.executeUpdate();

            System.out.println("Покупка успешно завершена!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Не удалось завершить покупку.");
        }
    }

    private static double getPriceForAd(int adId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT PRICE FROM ADS WHERE AD_ID = ?");
            stmt.setInt(1, adId);
            return stmt.executeQuery().getDouble("PRICE");
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

                // Проверяем, доступен ли товар для отображения
                boolean isAvailable = "active".equals(status); // Товар активен
                boolean isCurrentUserSeller = sellerId == SessionManager.getLoggedInUserId();

                if (isAvailable && !isCurrentUserSeller) {
                    // Создаем объект Ad
                    Ad ad = new Ad();
                    ad.setAdId(adId);
                    ad.setTitle(title);
                    ad.setPrice(price);
                    ad.setDescription(description);
                    ad.setImage(imageBytes);
                    ad.setLocation(location);
                    ad.setStatus(status);
                    ad.setSellerId(sellerId);

                    // Добавляем объявление в список
                    adsList.getItems().add(ad);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить объявления.");
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
                    // Создаем объект Ad
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/main/create_ad.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Создать объявление");
            stage.show();
        } catch (IOException e) {
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/main/login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Вход");
            stage.show();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/main/register.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Регистрация");
            stage.show();
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
     * Обновление интерфейса в зависимости от состояния авторизации.
     */
    private void updateUIBasedOnAuthStatus() {
        if (SessionManager.isLoggedIn()) {
            // Если пользователь авторизован
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
}