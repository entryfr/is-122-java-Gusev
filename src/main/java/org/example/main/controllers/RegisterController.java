package org.example.main.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.example.main.utils.SceneManager;
import org.example.main.utils.SessionManager;

import java.sql.SQLException;
import java.util.regex.Pattern;

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
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[0-9]{10,15}$");

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText().trim();

        // Проверка обязательных полей
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Ошибка", "Поля 'Логин', 'Email' и 'Пароль' обязательны для заполнения.");
            return;
        }

        // Проверка длины имени пользователя
        if (username.length() < 4) {
            showAlert("Ошибка", "Имя пользователя должно содержать не менее 4 символов.");
            return;
        }

        // Проверка email
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showAlert("Ошибка", "Введите корректный email адрес.");
            return;
        }

        // Проверка телефона, если он указан
        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            showAlert("Ошибка", "Введите корректный номер телефона (10-15 цифр, можно начинать с +).");
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

            int userId = inMemoryDatabase.addUser(
                    username,
                    email,
                    password,
                    firstName.isEmpty() ? null : firstName,
                    lastName.isEmpty() ? null : lastName,
                    phone.isEmpty() ? null : phone
            );

            if (userId > 0) {
                sessionManager.setLoggedInUser(username, userId, false);
                sceneManager.showScene("index");
                Object controller = sceneManager.getCurrentController("index");
                if (controller instanceof IndexController) {
                    ((IndexController) controller).updateUIBasedOnAuthStatus();
                }
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

    @FXML
    private void cancel() {
        try {
            sceneManager.showScene("index");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось вернуться на главную страницу.");
        }
    }
}