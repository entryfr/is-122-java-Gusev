package org.example.main.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import org.example.main.utils.Database;
import org.example.main.utils.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

public class ChatController {

    @FXML
    private ListView<String> chatMessages;

    @FXML
    private TextArea messageInput;

    private int chatId;
    private int userId;

    /**
     * Инициализация контроллера.
     */
    @FXML
    public void initialize() {
        userId = SessionManager.getLoggedInUserId();
        if (userId == -1) {
            showAlert("Ошибка", "Пользователь не авторизован.");
            return;
        }

        loadChatMessages();
    }

    /**
     * Отправка нового сообщения.
     */
    @FXML
    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (message.isEmpty()) {
            showAlert("Ошибка", "Сообщение не может быть пустым.");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            String messageId = UUID.randomUUID().toString();

            String query = "INSERT INTO MESSAGES (MESSAGE_ID, CHAT_ID, SENDER_ID, MESSAGE_TEXT, SENT_TIME) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, messageId);
            stmt.setInt(2, chatId);
            stmt.setInt(3, userId);
            stmt.setString(4, message);
            stmt.executeUpdate();

            updateLastMessage(chatId, message);

            messageInput.clear();
            loadChatMessages();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось отправить сообщение.");
        }
    }

    /**
     * Загрузка сообщений чата из базы данных.
     */
    private void loadChatMessages() {
        try (Connection conn = Database.getConnection()) {
            String query = "SELECT SENDER_ID, MESSAGE_TEXT, SENT_TIME FROM MESSAGES WHERE CHAT_ID = ? ORDER BY SENT_TIME";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, chatId);
            ResultSet rs = stmt.executeQuery();

            chatMessages.getItems().clear();
            while (rs.next()) {
                int senderId = rs.getInt("SENDER_ID");
                String messageText = rs.getString("MESSAGE_TEXT");
                String sentTime = rs.getTimestamp("SENT_TIME").toString();

                // Получаем имя отправителя
                String senderName = getSenderName(senderId);

                // Добавляем сообщение в список
                chatMessages.getItems().add(String.format("[%s] %s: %s", sentTime, senderName, messageText));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить сообщения.");
        }
    }

    /**
     * Получение имени пользователя по его ID.
     */
    private String getSenderName(int senderId) {
        try (Connection conn = Database.getConnection()) {
            String query = "SELECT USERNAME FROM USERS WHERE USER_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, senderId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("USERNAME");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Неизвестный пользователь";
    }

    /**
     * Обновление последнего сообщения в чате.
     */
    private void updateLastMessage(int chatId, String lastMessage) {
        try (Connection conn = Database.getConnection()) {
            String query = "UPDATE CHATS SET LAST_MESSAGE = ?, LAST_MESSAGE_TIME = CURRENT_TIMESTAMP WHERE CHAT_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, lastMessage);
            stmt.setInt(2, chatId);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Установка ID чата.
     */
    public void setChatId(int chatId) {
        this.chatId = chatId;
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
}