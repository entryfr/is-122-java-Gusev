package org.example.main.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.example.main.utils.SceneManager;
import org.example.main.utils.SessionManager;

import java.sql.SQLException;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField phoneField;
    @FXML private TextField passwordField;

    private final InMemoryDatabase inMemoryDatabase = InMemoryDatabase.getInstance();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final SceneManager sceneManager = SceneManager.getInstance();

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Ошибка", "Поля 'Логин', 'Email' и 'Пароль' обязательны для заполнения.");
            return;
        }

        try {
            if (inMemoryDatabase.isUsernameTaken(username)) {
                showAlert("Ошибка", "Этот логин уже занят.");
                return;
            }
            if (inMemoryDatabase.isEmailTaken(email)) {
                showAlert("Ошибка", "Этот email уже используется.");
                return;
            }

            // Добавляем пользователя в БД
            int userId = inMemoryDatabase.addUser(
                    username,
                    email,
                    password,
                    firstName.isEmpty() ? null : firstName,
                    lastName.isEmpty() ? null : lastName,
                    phone.isEmpty() ? null : phone
            );

            if (userId > 0) {
                sessionManager.setLoggedInUser(username, userId);

                sceneManager.showScene("index");

                showAlert("Успех", "Регистрация прошла успешно!");
            } else {
                showAlert("Ошибка", "Не удалось зарегистрировать пользователя.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Ошибка базы данных: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Неизвестная ошибка: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}