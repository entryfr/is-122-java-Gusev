package org.example.main.controllers;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.main.utils.Database;
import org.example.main.utils.SceneManager;
import org.example.main.utils.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.logging.Logger;

public class ChatController {

    @FXML
    private ListView<String> chatList;

    @FXML
    private VBox chatMessagesContainer;

    @FXML
    private TextField messageInput;

    private int chatId = -1;
    private int userId;

    private static final Logger logger = Logger.getLogger(ChatController.class.getName());

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

        loadChats();

        // Слушатель для выбора чата
        chatList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                try {
                    // Извлекаем ID выбранного чата из строки
                    String[] parts = newValue.split(" ");
                    int selectedChatId = Integer.parseInt(parts[0].replace("[", "").replace("]", ""));
                    setChatId(selectedChatId);
                    loadChatMessages();
                } catch (NumberFormatException e) {
                    logger.severe("Invalid chat ID format: " + newValue);
                    showAlert("Ошибка", "Некорректный формат ID чата.");
                }
            }
        });
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
            loadChatMessages(); // Обновляем отображение сообщений
        } catch (Exception e) {
            handleDatabaseError(e);
        }
    }

    /**
     * Загрузка списка чатов.
     */
    private void loadChats() {
        try (Connection conn = Database.getConnection()) {
            String query = "SELECT CHAT_ID, LAST_MESSAGE, LAST_MESSAGE_TIME FROM CHATS WHERE USER1_ID = ? OR USER2_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();

            chatList.getItems().clear();
            while (rs.next()) {
                int chatId = rs.getInt("CHAT_ID");
                String lastMessage = rs.getString("LAST_MESSAGE");
                String lastMessageTime = rs.getTimestamp("LAST_MESSAGE_TIME").toString();

                // Форматируем строку для отображения в списке чатов
                chatList.getItems().add(String.format("[%d] %s - %s", chatId, lastMessageTime, lastMessage));
            }
        } catch (Exception e) {
            handleDatabaseError(e);
        }
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
     * Загрузка сообщений чата из базы данных.
     */
    private void loadChatMessages() {
        if (chatId == -1) {
            logger.warning("Chat ID is not set. Cannot load messages.");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            String query = "SELECT M.SENDER_ID, U.USERNAME, M.MESSAGE_TEXT, M.SENT_TIME " +
                    "FROM MESSAGES M " +
                    "JOIN USERS U ON M.SENDER_ID = U.USER_ID " +
                    "WHERE M.CHAT_ID = ? ORDER BY M.SENT_TIME";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, chatId);
            ResultSet rs = stmt.executeQuery();

            chatMessagesContainer.getChildren().clear(); // Очищаем контейнер
            while (rs.next()) {
                int senderId = rs.getInt("SENDER_ID");
                String senderName = rs.getString("USERNAME");
                String messageText = rs.getString("MESSAGE_TEXT");
                Timestamp sentTime = rs.getTimestamp("SENT_TIME");

                LocalDateTime localSentTime = sentTime.toLocalDateTime();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                String formattedTime = localSentTime.format(formatter);
                // Создаем Label для сообщения
                Label messageLabel = new Label(String.format("[%s] %s:(%s)", senderName, messageText, formattedTime));

                if (senderId == SessionManager.getLoggedInUserId()) {
                    messageLabel.getStyleClass().add("user-message");
                } else {
                    messageLabel.getStyleClass().add("other-message");
                }
                HBox messageBox = new HBox();
                messageBox.setAlignment(senderId == SessionManager.getLoggedInUserId()
                        ? javafx.geometry.Pos.CENTER_RIGHT
                        : javafx.geometry.Pos.CENTER_LEFT);
                messageBox.getChildren().add(messageLabel);

                chatMessagesContainer.getChildren().add(messageBox);
                chatMessagesContainer.getChildren().add(messageLabel);
            }
        } catch (Exception e) {
            handleDatabaseError(e);
        }
    }

    /**
     * Установка ID чата.
     */
    public void setChatId(int chatId) {
        this.chatId = chatId;
        loadChatMessages();
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
            handleDatabaseError(e);
        }
    }

    /**
     * Проверка существования чата.
     */
    private boolean chatExists(int user1Id, int user2Id) {
        try (Connection conn = Database.getConnection()) {
            String query = "SELECT COUNT(*) FROM CHATS WHERE (USER1_ID = ? AND USER2_ID = ?) OR (USER1_ID = ? AND USER2_ID = ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, user1Id);
            stmt.setInt(2, user2Id);
            stmt.setInt(3, user2Id);
            stmt.setInt(4, user1Id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            handleDatabaseError(e);
        }
        return false;
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

    /**
     * Обработка ошибок базы данных.
     */
    private void handleDatabaseError(Exception e) {
        logger.severe("Database error: " + e.getMessage());
        showAlert("Ошибка базы данных", "Произошла ошибка при работе с базой данных.");
    }
}