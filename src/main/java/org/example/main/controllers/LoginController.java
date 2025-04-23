package org.example.main.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.main.utils.SceneManager;
import org.example.main.utils.SessionManager;


import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    /**
     * Обработка отмены входа.
     */
    @FXML
    private void cancel() {
        try {
            SceneManager.getInstance().showScene("index");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось вернуться на главную страницу.");
        }
    }

    /**
     * Обработка входа пользователя.
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Ошибка", "Введите имя пользователя и пароль.");
            return;
        }

        try {
            int userId = SessionManager.getInstance().authenticateUser(username, password);
            if (userId != -1) {
                SessionManager.getInstance().setLoggedInUser(username, userId);
                SceneManager.getInstance().showScene("index");
                return;
            }

            userId = InMemoryDatabase.getInstance().authenticateUser(username, password);
            if (userId == -1) {
                showAlert("Ошибка", "Неверное имя пользователя или пароль.");
                return;
            }

            SessionManager.getInstance().setLoggedInUser(username, userId);
            SceneManager.getInstance().showScene("index");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Ошибка базы данных", "Не удалось выполнить вход: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось войти: " + e.getMessage());
        }
    }

    /**
     * Отображение диалогового окна с сообщением.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}