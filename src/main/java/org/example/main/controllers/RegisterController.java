package org.example.main.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import org.example.main.models.User;
import org.example.main.utils.Database;
import org.example.main.utils.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RegisterController {

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
    private TextField passwordField;

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String phone = phoneField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Ошибка", "Все поля должны быть заполнены.");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            PreparedStatement checkUsernameStmt = conn.prepareStatement("SELECT COUNT(*) FROM USERS WHERE USERNAME = ?");
            checkUsernameStmt.setString(1, username);
            ResultSet rs = checkUsernameStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                showAlert("Ошибка", "Имя пользователя уже занято.");
                return;
            }

            PreparedStatement checkEmailStmt = conn.prepareStatement("SELECT COUNT(*) FROM USERS WHERE EMAIL = ?");
            checkEmailStmt.setString(1, email);
            rs = checkEmailStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                showAlert("Ошибка", "Этот email уже используется.");
                return;
            }

            PreparedStatement getMaxUserIdStmt = conn.prepareStatement("SELECT COALESCE(MAX(USER_ID), 0) + 1 FROM USERS");
            rs = getMaxUserIdStmt.executeQuery();
            int userId = rs.next() ? rs.getInt(1) : 1;

            String hashedPassword = hashPassword(password);

            String query = "INSERT INTO USERS (USER_ID, USERNAME, EMAIL, PASSWORD_HASH, FIRST_NAME, LAST_NAME, PHONE) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);

            stmt.setInt(1, userId);
            stmt.setString(2, username);
            stmt.setString(3, email);
            stmt.setString(4, hashedPassword);
            stmt.setString(5, firstName);
            stmt.setString(6, lastName);
            stmt.setString(7, phone);

            stmt.executeUpdate();

            SessionManager.setLoggedInUser(username, userId);

            navigateToMainPage();

            showAlert("Успех", "Регистрация прошла успешно!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось зарегистрироваться.");
        }
    }

    private void navigateToMainPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/main/index.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Главная страница");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String hashPassword(String password) {
        return password;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}