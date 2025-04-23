package org.example.main.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.main.utils.ImageUtils;
import org.example.main.utils.SceneManager;
import org.example.main.utils.SessionManager;

import java.io.File;
import java.util.List;

public class CreateAdController {
    @FXML
    private Label welcomeText;

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

    private final InMemoryDatabase inMemoryDatabase = new InMemoryDatabase();
    private final SessionManager sessionManager = SessionManager.getInstance();

    /**
     * Инициализация контроллера.
     */
    @FXML
    public void initialize() {
        try {

            List<String> categories = InMemoryDatabase.getInstance().loadCategories();
            categoryComboBox.getItems().addAll(categories);

            if (!categories.isEmpty()) {
                categoryComboBox.setValue(categories.get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить категории.");
        }

    }

    /**
     * Загрузка категорий из таблицы CATEGORIES.
     */
    private void loadCategories() {
        try {
            categoryComboBox.getItems().addAll(inMemoryDatabase.loadCategories());
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
        try {
            if (!SessionManager.getInstance().isLoggedIn()) {
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

            int categoryId = InMemoryDatabase.getInstance().getCategoryIdByName(category);
            if (categoryId == -1) {
                showAlert("Ошибка", "Категория '" + category + "' не найдена.");
                return;
            }

            byte[] imageData = null;
            if (imagePath != null) {
                imageData = ImageUtils.loadImage(imagePath);
            }

            int userId = SessionManager.getInstance().getLoggedInUserId();

            InMemoryDatabase.getInstance().createAd(
                    userId,
                    categoryId,
                    title,
                    description,
                    price,
                    location,
                    imageData
            );

            showAlert("Успех", "Объявление успешно создано!");

            SceneManager sceneManager = SceneManager.getInstance();
            IndexController indexController = (IndexController) sceneManager.getCurrentController("index");
            if (indexController != null) {
                indexController.refreshAds();
            }

            // Возвращаемся на главную страницу
            sceneManager.showScene("index");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось создать объявление: " + e.getMessage());
        }
    }

    /**
     * Метод для обновления списка объявлений на главной странице.
     */
    private void updateAdsListOnIndexPage() {
        try {
            IndexController indexController = (IndexController) SceneManager.getInstance().getCurrentController("index");

            if (indexController != null) {
                indexController.loadAds();
            } else {
                System.err.println("IndexController is not available! Check if the controller was properly registered in SceneManager.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error updating ads list on index page: " + e.getMessage());

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
    private void redirectToIndex() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/main/index.fxml"));
            Parent root = loader.load();
            IndexController indexController = loader.getController();
            SceneManager sceneManager = SceneManager.getInstance();
            sceneManager.registerController("index", indexController);

            Scene scene = new Scene(root);
            Stage stage = (Stage) welcomeText.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Главная страница");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить главную страницу.");
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
            loader.load();
            Stage newStage = new Stage();
            newStage.setTitle("Вход");
            newStage.setScene(new Scene(loader.getRoot()));
            newStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось перейти на страницу входа.");
        }
    }
}