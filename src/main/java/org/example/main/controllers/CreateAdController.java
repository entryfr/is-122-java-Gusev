package org.example.main.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.main.utils.Database;
import org.example.main.utils.ImageUtils;
import org.example.main.utils.SessionManager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CreateAdController {

    @FXML
    private TextField titleField;
    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private TextField priceField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private ImageView imageView;
    @FXML
    private TextField locationField;

    private String imagePath;

    /**
     * Инициализация контроллера.
     */
    @FXML
    public void initialize() {
        loadCategories();
    }

    /**
     * Загрузка категорий из таблицы CATEGORIES.
     */
    private void loadCategories() {
        try (Connection conn = Database.getConnection()) {
            String query = "SELECT CATEGORY_NAME FROM CATEGORIES";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                categoryComboBox.getItems().add(rs.getString("CATEGORY_NAME"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить категории.");
        }
    }

    /**
     * Обработка нажатия на кнопку "Выбрать изображение".
     */
    @FXML
    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите изображение");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            imagePath = selectedFile.getAbsolutePath();
            Image image = new Image(selectedFile.toURI().toString());
            imageView.setImage(image);
        }
    }

    /**
     * Обработка нажатия на кнопку "Создать объявление".
     */
    @FXML
    private void handleCreateAd() {
        if (!SessionManager.isLoggedIn()) {
            showAlert("Ошибка", "Вы должны быть авторизованы для создания объявления.");
            redirectToLogin();
            return;
        }

        String title = titleField.getText();
        String category = categoryComboBox.getValue();
        String priceText = priceField.getText();
        String description = descriptionField.getText();
        String location = locationField.getText();

        if (title.isEmpty() || category == null || priceText.isEmpty() || description.isEmpty() || location.isEmpty()) {
            showAlert("Ошибка", "Все поля должны быть заполнены.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Цена должна быть числом.");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            // Получаем ID категории по её имени
            int categoryId = getCategoryIdByName(category);
            if (categoryId == -1) {
                showAlert("Ошибка", "Категория '" + category + "' не найдена.");
                return;
            }

            // Генерация уникального имени файла для изображения
            String uniqueImagePath = null;
            byte[] imageData = null;
            if (imagePath != null) {
                uniqueImagePath = generateUniqueFileName(imagePath);
                imageData = ImageUtils.loadImage(imagePath);
            }

            String query = "INSERT INTO ADS (AD_ID, USER_ID, CATEGORY_ID, TITLE, DESCRIPTION, PRICE, LOCATION, IMAGE_PATH, STATUS) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);

            int adId = generateUniqueId();

            stmt.setInt(1, adId); // AD_ID
            stmt.setInt(2, SessionManager.getLoggedInUserId()); // USER_ID
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

            showAlert("Успех", "Объявление успешно создано!");
            redirectToIndex();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось создать объявление: " + e.getMessage());
        }
    }

    /**
     * Получение ID категории по её имени.
     */
    private int getCategoryIdByName(String categoryName) throws Exception {
        try (Connection conn = Database.getConnection()) {
            String query = "SELECT CATEGORY_ID FROM CATEGORIES WHERE CATEGORY_NAME = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, categoryName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("CATEGORY_ID");
            }
        }
        return -1;
    }

    /**
     * Генерация уникального имени файла для изображения.
     */
    private String generateUniqueFileName(String originalFilePath) {
        String extension = "";
        int lastDotIndex = originalFilePath.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = originalFilePath.substring(lastDotIndex);
        }
        return java.util.UUID.randomUUID().toString() + extension;
    }

    /**
     * Генерация уникального ID для объявления.
     */
    private int generateUniqueId() throws Exception {
        try (Connection conn = Database.getConnection()) {
            String query = "SELECT MAX(AD_ID) AS MAX_ID FROM ADS";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("MAX_ID") + 1;
            }
        }
        return 1;
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

    /**
     * Переход на главную страницу.
     */
    private void redirectToIndex() {
        try {
            Stage currentStage = (Stage) titleField.getScene().getWindow();
            currentStage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/main/index.fxml"));
            Scene scene = new Scene(loader.load());
            Stage newStage = new Stage();
            newStage.setTitle("Главная страница");
            newStage.setScene(scene);
            newStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось перейти на главную страницу.");
        }
    }

    /**
     * Перенаправление на страницу входа.
     */
    private void redirectToLogin() {
        try {
            Stage currentStage = (Stage) titleField.getScene().getWindow();
            currentStage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/main/login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage newStage = new Stage();
            newStage.setTitle("Вход");
            newStage.setScene(scene);
            newStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось перейти на страницу входа.");
        }
    }
}