package org.example.main.controllers;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.example.main.utils.ImageUtils;
import org.example.main.utils.SceneManager;
import org.example.main.utils.SessionManager;

import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class CreateAdController {
    @FXML private Label welcomeText;
    @FXML private TextField titleField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField priceField;
    @FXML private TextArea descriptionField;
    @FXML private ImageView imageView;
    @FXML private ComboBox<String> cityComboBox;

    private String imagePath;
    private final InMemoryDatabase inMemoryDatabase = InMemoryDatabase.getInstance(); // Используем синглтон

    @FXML
    public void initialize() {
        try {
            loadCategories();
            initializeCitiesComboBox();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка инициализации", "Не удалось загрузить данные: " + e.getMessage());
        }
    }

    private void initializeCitiesComboBox() throws SQLException {
        final List<String> cities = Collections.unmodifiableList(
                Optional.ofNullable(inMemoryDatabase.getCities())
                        .orElseGet(ArrayList::new)
        );

        cityComboBox.getItems().setAll(cities);
        cityComboBox.setEditable(true);

        cityComboBox.setConverter(new StringConverter<String>() {
            @Override
            public String toString(String object) {
                return object != null ? object : "";
            }

            @Override
            public String fromString(String string) {
                return string;
            }
        });

        AtomicBoolean isUserSelecting = new AtomicBoolean(false);

        cityComboBox.setOnAction(event -> {
            String selected = cityComboBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                isUserSelecting.set(true);
                cityComboBox.getEditor().setText(selected);
                isUserSelecting.set(false);
            }
        });

        PauseTransition pause = new PauseTransition(Duration.millis(300));
        cityComboBox.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (isUserSelecting.get()) {
                return;
            }

            pause.setOnFinished(event -> {
                if (newVal == null || newVal.isEmpty()) {
                    cityComboBox.getItems().setAll(cities);
                    return;
                }

                List<String> filtered = cities.stream()
                        .filter(Objects::nonNull)
                        .filter(city -> city.toLowerCase().contains(newVal.toLowerCase()))
                        .collect(Collectors.toList());

                cityComboBox.getItems().setAll(filtered);

                cityComboBox.getEditor().setText(newVal);
            });
            pause.playFromStart();
        });
    }

    private void loadCategories() {
        try {
            List<String> categories = inMemoryDatabase.loadCategories();
            categoryComboBox.getItems().clear();
            if (categories != null && !categories.isEmpty()) {
                categoryComboBox.getItems().addAll(categories);
                categoryComboBox.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить категории.");
        }
    }

    @FXML
    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите изображение");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            imagePath = selectedFile.getAbsolutePath();
            imageView.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

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
            String location = cityComboBox.getEditor().getText();

            if (title.isEmpty() || category == null || priceText.isEmpty() ||
                    description.isEmpty() || location == null || location.trim().isEmpty()) {
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

            int categoryId = inMemoryDatabase.getCategoryIdByName(category);
            if (categoryId == -1) {
                showAlert("Ошибка", "Категория '" + category + "' не найдена.");
                return;
            }

            byte[] imageData = imagePath != null ? ImageUtils.loadImage(imagePath) : null;
            int userId = SessionManager.getInstance().getLoggedInUserId();

            inMemoryDatabase.createAd(
                    userId,
                    categoryId,
                    title,
                    description,
                    price,
                    location,
                    imageData
            );

            showAlert("Успех", "Объявление успешно создано!");
            refreshAdsOnIndexPage();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось создать объявление: " + e.getMessage());
        }
    }

    private void refreshAdsOnIndexPage() {
        try {
            SceneManager sceneManager = SceneManager.getInstance();
            IndexController indexController = (IndexController) sceneManager.getCurrentController("index");

            if (indexController != null) {
                indexController.refreshAds();
            } else {
                System.err.println("IndexController не доступен");
            }

            sceneManager.showScene("index");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось обновить список объявлений: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void redirectToLogin() {
        try {
            Stage currentStage = (Stage) titleField.getScene().getWindow();
            currentStage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/main/login.fxml"));
            Stage newStage = new Stage();
            newStage.setScene(new Scene(loader.load()));
            newStage.setTitle("Вход");
            newStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось перейти на страницу входа.");
        }
    }
}