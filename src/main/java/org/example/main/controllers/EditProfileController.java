package org.example.main.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.main.models.User;
import org.example.main.utils.SceneManager;
import org.example.main.utils.SessionManager;

import java.sql.SQLException;

public class EditProfileController {

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

    private final InMemoryDatabase inMemoryDatabase = new InMemoryDatabase();
    private final SessionManager sessionManager = SessionManager.getInstance();

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
            } else {
                showAlert("Ошибка", "Пользователь не найден.");
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    /**
     * Сохранение изменений в профиле пользователя.
     */
    @FXML
    private void saveChanges() {
        int userId = sessionManager.getLoggedInUserId();
        if (userId == -1) {
            showAlert("Ошибка", "Пользователь не авторизован.");
            return;
        }

        String email = emailField.getText();
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String phone = phoneField.getText();

        if (email.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            showAlert("Ошибка", "Все обязательные поля должны быть заполнены.");
            return;
        }

        try {
            boolean success = inMemoryDatabase.updateUserProfile(userId, email, firstName, lastName, phone);
            if (success) {
                showAlert("Успех", "Профиль успешно обновлен.");
                sceneManager.showScene("profile");
            } else {
                showAlert("Ошибка", "Не удалось обновить профиль.");
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    /**
     * Отмена редактирования и возврат на страницу профиля.
     */
    @FXML
    private void cancel() {
        try {
            sceneManager.showScene("profile");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось вернуться на страницу профиля.");
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

    /**
     * Обработка ошибок базы данных.
     */
    private void handleDatabaseError(Exception e) {
        System.err.println("Database error: " + e.getMessage());
        showAlert("Ошибка базы данных", "Произошла ошибка при работе с базой данных.");
    }
}