package org.example.main.controllers;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import org.example.main.utils.Database;
import org.example.main.utils.ImageUtils;
import org.example.main.utils.SessionManager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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
        List<String> categories = new ArrayList<>();
        try (Connection conn = Database.getConnection()) {
            String query = "SELECT CATEGORY_NAME FROM CATEGORIES";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                categories.add(rs.getString("CATEGORY_NAME"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить категории.");
        }
        categoryComboBox.getItems().addAll(categories);
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

            String uniqueImagePath = null;
            if (imagePath != null) {
                uniqueImagePath = generateUniqueFileName(imagePath);
            }

            String query = "INSERT INTO ADS (USER_ID, TITLE, CATEGORY_ID, PRICE, DESCRIPTION, IMAGE_PATH, LOCATION) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);

            int userId = SessionManager.getLoggedInUserId(); // Используем ID авторизованного пользователя
            stmt.setInt(1, userId);
            stmt.setString(2, title);
            stmt.setInt(3, categoryId);
            stmt.setDouble(4, price);
            stmt.setString(5, description);

            if (uniqueImagePath != null) {
                byte[] imageData = ImageUtils.loadImage(imagePath);
                stmt.setBytes(6, imageData); // Загрузка изображения как BLOB
            } else {
                stmt.setNull(6, java.sql.Types.BLOB); // Если изображение не выбрано
            }

            stmt.setString(7, location);
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
        int categoryId = -1;
        try (Connection conn = Database.getConnection()) {
            String query = "SELECT CATEGORY_ID FROM CATEGORIES WHERE CATEGORY_NAME = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, categoryName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                categoryId = rs.getInt("CATEGORY_ID");
            }
        }
        return categoryId;
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

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/main/views/index.fxml"));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setTitle("Главная страница");
            newStage.setScene(new Scene(root));
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
            // Закрытие текущего окна
            Stage currentStage = (Stage) titleField.getScene().getWindow();
            currentStage.close();

            // Загрузка FXML-файла страницы входа
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/main/login.fxml"));
            Parent root = loader.load();

            // Создание нового окна
            Stage newStage = new Stage();
            newStage.setTitle("Вход");
            newStage.setScene(new Scene(root));
            newStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось перейти на страницу входа.");
        }
    }
}