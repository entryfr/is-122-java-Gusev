package org.example.main.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.main.utils.SceneManager;
import org.example.main.utils.SessionManager;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void cancel() {
        try {
            SceneManager.getInstance().showScene("index");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Не удалось вернуться на главную страницу.");
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Введите имя пользователя и пароль.");
            return;
        }

        try {
            int userId = sessionManager.authenticateUser(username, password);
            if (userId != -1) {
                sceneManager.showScene("index");
                Object controller = sceneManager.getCurrentController("index");
                if (controller instanceof IndexController) {
                    ((IndexController) controller).updateUIBasedOnAuthStatus();
                }
            } else {
                showAlert("Неверное имя пользователя или пароль.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Не удалось войти: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private final SessionManager sessionManager = SessionManager.getInstance();
    private final SceneManager sceneManager = SceneManager.getInstance();
}