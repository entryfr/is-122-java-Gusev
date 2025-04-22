package org.example.main.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.main.models.User;
import org.example.main.utils.Database;
import org.example.main.utils.SceneManager;
import org.example.main.utils.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
        int userId = SessionManager.getLoggedInUserId();
        if (userId == -1) {
            showAlert("Ошибка", "Пользователь не авторизован.");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            String query = "SELECT USERNAME, EMAIL, FIRST_NAME, LAST_NAME, PHONE, REGISTRATION_DATE FROM USERS WHERE USER_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUsername(rs.getString("USERNAME"));
                user.setEmail(rs.getString("EMAIL"));
                user.setFirstName(rs.getString("FIRST_NAME"));
                user.setLastName(rs.getString("LAST_NAME"));
                user.setPhone(rs.getString("PHONE"));
                user.setRegistrationDate(rs.getString("REGISTRATION_DATE"));

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
            SceneManager.showScene("edit_profile");
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
        SessionManager.logout();
        try {
            SceneManager.showScene("index");
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
            SceneManager.showScene("index");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось вернуться на страницу профиля.");
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