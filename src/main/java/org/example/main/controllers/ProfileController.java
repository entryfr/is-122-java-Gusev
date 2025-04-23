package org.example.main.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.main.models.User;
import org.example.main.utils.SceneManager;
import org.example.main.utils.SessionManager;

public class ProfileController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField registrationDateField;

    // Экземпляры зависимостей
    private final InMemoryDatabase inMemoryDatabase = new InMemoryDatabase();
    private final SessionManager sessionManager = SessionManager.getInstance();

    // Экземпляр SceneManager
    private final SceneManager sceneManager = SceneManager.getInstance();

    /**
     * Инициализация контроллера.
     */
    @FXML
    public void initialize() {
        loadUserProfile();
    }

    /**
     * Загрузка данных пользователя из базы данных.
     */
    private void loadUserProfile() {
        int userId = sessionManager.getLoggedInUserId();
        if (userId == -1) {
            showAlert("Ошибка", "Пользователь не авторизован.");
            return;
        }

        try {
            User user = inMemoryDatabase.getUserById(userId);
            if (user != null) {
                // Заполняем поля интерфейса
                usernameField.setText(user.getUsername());
                emailField.setText(user.getEmail());
                firstNameField.setText(user.getFirstName());
                lastNameField.setText(user.getLastName());
                phoneField.setText(user.getPhone());
                registrationDateField.setText(user.getRegistrationDate());
            } else {
                showAlert("Ошибка", "Пользователь не найден.");
            }
        } catch (Exception e) {
            handleDatabaseError(e);
        }
    }

    /**
     * Обработка нажатия на кнопку "Редактировать профиль".
     */
    @FXML
    private void editProfile() {
        try {
            sceneManager.showScene("edit_profile");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить страницу редактирования профиля.");
        }
    }

    /**
     * Обработка нажатия на кнопку "Выйти из аккаунта".
     */
    @FXML
    private void logout() {
        sessionManager.logout();
        try {
            sceneManager.showScene("index");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось выйти из аккаунта.");
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
    private void cancel() {
        try {
            sceneManager.showScene("index");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось вернуться на главную страницу.");
        }
    }

    /**
     * Обработка ошибок базы данных.
     */
    private void handleDatabaseError(Exception e) {
        System.err.println("Database error: " + e.getMessage());
        showAlert("Ошибка базы данных", "Произошла ошибка при работе с базой данных.");
    }
}