package org.example.main.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import org.example.main.models.User;
import org.example.main.models.Ad;
import org.example.main.models.Purchase;
import org.example.main.utils.SceneManager;
import org.example.main.utils.SessionManager;

import java.sql.SQLException;
import java.util.List;

public class ProfileController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField phoneField;
    @FXML private TextField registrationDateField;
    @FXML private Button editButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private VBox editControls;
    @FXML private ListView<Ad> userAdsList;
    @FXML private ListView<Purchase> userPurchasesList;

    private final InMemoryDatabase db = InMemoryDatabase.getInstance();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final SceneManager sceneManager = SceneManager.getInstance();

    private boolean editMode = false;

    @FXML
    public void initialize() {
        loadUserProfile();
        loadUserAds();
        loadUserPurchases();
        setupEditControls();
        setupAdsList();
        setupPurchasesList();
    }

    private void loadUserProfile() {
        int userId = sessionManager.getLoggedInUserId();
        if (userId == -1) {
            showAlert("Ошибка", "Пользователь не авторизован");
            return;
        }

        try {
            User user = db.getUserById(userId);
            if (user != null) {
                usernameField.setText(user.getUsername());
                emailField.setText(user.getEmail());
                firstNameField.setText(user.getFirstName());
                lastNameField.setText(user.getLastName());
                phoneField.setText(user.getPhone());
                registrationDateField.setText(user.getRegistrationDate());

                setFieldsEditable(false);
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    public void loadUserAds() {
        int userId = sessionManager.getLoggedInUserId();
        if (userId == -1) return;

        try {
            List<Ad> ads = db.getUserAds(userId);
            userAdsList.getItems().clear();
            userAdsList.getItems().addAll(ads);
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    private void loadUserPurchases() {
        int userId = sessionManager.getLoggedInUserId();
        if (userId == -1) return;

        try {
            List<Purchase> purchases = db.getUserPurchases(userId);
            userPurchasesList.getItems().clear();
            userPurchasesList.getItems().addAll(purchases);
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }



    private void setupPurchasesList() {
        userPurchasesList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Purchase purchase, boolean empty) {
                super.updateItem(purchase, empty);
                if (empty || purchase == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(10);
                    Label titleLabel = new Label(purchase.getAdTitle());
                    Label priceLabel = new Label(String.format("Цена: %.2f руб.", purchase.getPrice()));
                    Label dateLabel = new Label("Дата: " + purchase.getPurchaseDate().toString());

                    hbox.getChildren().addAll(titleLabel, priceLabel, dateLabel);
                    setGraphic(hbox);
                }
            }
        });

        userPurchasesList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Purchase selectedPurchase = userPurchasesList.getSelectionModel().getSelectedItem();
                if (selectedPurchase != null) {
                    try {
                        sceneManager.showSceneWithParameters("ad_details", "adId", selectedPurchase.getAdId());
                    } catch (Exception e) {
                        showAlert("Ошибка", "Не удалось открыть описание объявления.");
                    }
                }
            }
        });
    }

    private void setupEditControls() {
        editControls.setVisible(false);
        saveButton.setVisible(false);
        cancelButton.setVisible(false);
    }



    @FXML
    private void toggleEditMode() {
        editMode = !editMode;

        if (editMode) {
            editButton.setText("Отменить");
            editControls.setVisible(true);
            saveButton.setVisible(true);
            cancelButton.setVisible(true);
            setFieldsEditable(true);
            usernameField.setEditable(false);
            registrationDateField.setEditable(false);
        } else {
            editButton.setText("Редактировать");
            editControls.setVisible(false);
            saveButton.setVisible(false);
            cancelButton.setVisible(false);
            setFieldsEditable(false);
            loadUserProfile();
        }
    }

    @FXML
    private void saveProfile() {
        int userId = sessionManager.getLoggedInUserId();
        if (userId == -1) {
            showAlert("Ошибка", "Пользователь не авторизован");
            return;
        }

        try {
            boolean updated = db.updateUserProfile(
                    userId,
                    emailField.getText(),
                    firstNameField.getText(),
                    lastNameField.getText(),
                    phoneField.getText()
            );

            if (updated) {
                showAlert("Успех", "Профиль успешно обновлен");
                toggleEditMode();
                loadUserProfile();
            } else {
                showAlert("Ошибка", "Не удалось обновить профиль");
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    @FXML
    private void cancelEdit() {
        toggleEditMode();
    }

    @FXML
    private void logout() {
        try {
            sessionManager.logout();
            sceneManager.showScene("index");
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось выйти из системы");
        }
    }

    @FXML
    private void backToIndex() {
        try {
            sceneManager.showScene("index");
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось вернуться на главную страницу");
        }
    }

    private void setFieldsEditable(boolean editable) {
        emailField.setEditable(editable);
        firstNameField.setEditable(editable);
        lastNameField.setEditable(editable);
        phoneField.setEditable(editable);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleDatabaseError(SQLException e) {
        e.printStackTrace();
        showAlert("Ошибка базы данных", e.getMessage());
    }
    private void openEditAd(Ad ad) {
        try {
            sceneManager.showSceneWithParameters("edit_ad", "adId", ad.getAdId());
            // loadUserAds(); // Удаляем, перенесем обновление в EditAdController
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось открыть форму редактирования объявления.");
        }
    }
    private void setupAdsList() {
        userAdsList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Ad ad, boolean empty) {
                super.updateItem(ad, empty);
                if (empty || ad == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(10);
                    Label titleLabel = new Label(ad.getTitle());
                    Label priceLabel = new Label(String.format("Цена: %.2f руб.", ad.getPrice()));
                    Label statusLabel = new Label("Статус: " + ad.getStatus());
                    Button editAdButton = new Button("Редактировать");
                    Button deleteAdButton = new Button("Удалить"); // Новая кнопка
                    editAdButton.setOnAction(event -> openEditAd(ad));
                    deleteAdButton.setOnAction(event -> deleteAd(ad)); // Обработчик для удаления

                    hbox.getChildren().addAll(titleLabel, priceLabel, statusLabel, editAdButton, deleteAdButton);
                    setGraphic(hbox);
                }
            }
        });

        userAdsList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Ad selectedAd = userAdsList.getSelectionModel().getSelectedItem();
                if (selectedAd != null) {
                    try {
                        sceneManager.showSceneWithParameters("ad_details", "adId", selectedAd.getAdId());
                    } catch (Exception e) {
                        showAlert("Ошибка", "Не удалось открыть описание объявления.");
                    }
                }
            }
        });
    }
    private void deleteAd(Ad ad) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Подтверждение удаления");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Вы уверены, что хотите удалить объявление \"" + ad.getTitle() + "\"?");
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    db.deleteAd(ad.getAdId(), sessionManager.getLoggedInUserId());
                    showAlert("Успех", "Объявление успешно удалено");
                    loadUserAds(); // Обновляем список
                } catch (SQLException e) {
                    showAlert("Ошибка", "Не удалось удалить объявление: " + e.getMessage());
                }
            }
        });
    }
}