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
            String query = "SELECT USERNAME, EMAIL, FIRST_NAME, LAST_NAME, PHONE FROM USERS WHERE USER_ID = ?";
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

                // Заполняем поля интерфейса
                usernameField.setText(user.getUsername());
                emailField.setText(user.getEmail());
                firstNameField.setText(user.getFirstName());
                lastNameField.setText(user.getLastName());
                phoneField.setText(user.getPhone());
            } else {
                showAlert("Ошибка", "Пользователь не найден.");
            }
        } catch (Exception e) {
            handleDatabaseError(e);
        }
    }

    /**
     * Сохранение изменений в профиле пользователя.
     */
    @FXML
    private void saveChanges() {
        int userId = SessionManager.getLoggedInUserId();
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

        try (Connection conn = Database.getConnection()) {
            String query = "UPDATE USERS SET EMAIL = ?, FIRST_NAME = ?, LAST_NAME = ?, PHONE = ? WHERE USER_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, firstName);
            stmt.setString(3, lastName);
            stmt.setString(4, phone);
            stmt.setInt(5, userId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                showAlert("Успех", "Профиль успешно обновлен.");
                SceneManager.showScene("profile");
            } else {
                showAlert("Ошибка", "Не удалось обновить профиль.");
            }
        } catch (Exception e) {
            handleDatabaseError(e);
        }
    }

    /**
     * Отмена редактирования и возврат на страницу профиля.
     */
    @FXML
    private void cancel() {
        try {
            SceneManager.showScene("profile");
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
