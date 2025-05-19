package org.example.main.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.main.models.Ad;
import org.example.main.utils.SceneManager;
import org.example.main.utils.SessionManager;

import java.sql.SQLException;
import java.util.List;

public class EditAdController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> locationComboBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final InMemoryDatabase db = InMemoryDatabase.getInstance();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final SceneManager sceneManager = SceneManager.getInstance();
    private Ad ad;

    @FXML
    public void initialize() {
        loadCategories();
        loadCities();
        loadAdData();
    }

    private void loadCategories() {
        try {
            List<String> categories = db.loadCategories();
            categoryComboBox.setItems(FXCollections.observableArrayList(categories));
        } catch (SQLException e) {
            showAlert("Ошибка", "Не удалось загрузить категории: " + e.getMessage());
        }
    }

    private void loadCities() {
        try {
            List<String> cities = db.getCities();
            locationComboBox.setItems(FXCollections.observableArrayList(cities));
        } catch (SQLException e) {
            showAlert("Ошибка", "Не удалось загрузить города: " + e.getMessage());
        }
    }

    private void loadAdData() {
        int adId = (int) sceneManager.getPassedParameter("adId", -1);
        try {
            List<Ad> userAds = db.getUserAds(sessionManager.getLoggedInUserId());
            ad = userAds.stream().filter(a -> a.getAdId() == adId).findFirst().orElse(null);
            if (ad != null) {
                titleField.setText(ad.getTitle());
                descriptionField.setText(ad.getDescription());
                priceField.setText(String.valueOf(ad.getPrice()));
                categoryComboBox.setValue(ad.getCategoryName());
                locationComboBox.setValue(ad.getLocation());
            } else {
                showAlert("Ошибка", "Объявление не найдено");
                sceneManager.showScene("profile");
            }
        } catch (SQLException e) {
            showAlert("Ошибка", "Не удалось загрузить данные объявления: " + e.getMessage());
        }
    }

    @FXML
    private void saveAd() {
        if (!validateFields()) {
            return;
        }

        try {
            int categoryId = db.getCategoryIdByName(categoryComboBox.getValue());
            db.updateAd(
                    ad.getAdId(),
                    categoryId,
                    titleField.getText(),
                    descriptionField.getText(),
                    Double.parseDouble(priceField.getText()),
                    locationComboBox.getValue()
            );
            showAlert("Успех", "Объявление успешно обновлено");
            sceneManager.showScene("profile");
            // Обновляем список объявлений в ProfileController
            ProfileController profileController = (ProfileController) sceneManager.getCurrentController("profile");
            if (profileController != null) {
                profileController.loadUserAds();
            }
        } catch (SQLException e) {
            showAlert("Ошибка", "Не удалось обновить объявление: " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        try {
            sceneManager.showScene("profile");
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось вернуться в профиль");
        }
    }

    private boolean validateFields() {
        if (titleField.getText().isEmpty()) {
            showAlert("Ошибка", "Введите заголовок объявления");
            return false;
        }
        if (descriptionField.getText().isEmpty()) {
            showAlert("Ошибка", "Введите описание объявления");
            return false;
        }
        if (priceField.getText().isEmpty()) {
            showAlert("Ошибка", "Введите цену");
            return false;
        }
        try {
            double price = Double.parseDouble(priceField.getText());
            if (price <= 0) {
                showAlert("Ошибка", "Цена должна быть больше 0");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Цена должна быть числом");
            return false;
        }
        if (categoryComboBox.getValue() == null) {
            showAlert("Ошибка", "Выберите категорию");
            return false;
        }
        if (locationComboBox.getValue() == null) {
            showAlert("Ошибка", "Выберите город");
            return false;
        }
        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}