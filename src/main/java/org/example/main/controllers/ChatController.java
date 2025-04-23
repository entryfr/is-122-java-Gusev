package org.example.main.controllers;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.main.utils.SceneManager;
import org.example.main.utils.SessionManager;

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

    private final InMemoryDatabase inMemoryDatabase = new InMemoryDatabase();
    private final SessionManager sessionManager = SessionManager.getInstance();

    private final SceneManager sceneManager = SceneManager.getInstance();

    private static final Logger logger = Logger.getLogger(ChatController.class.getName());

    /**
     * Инициализация контроллера.
     */
    @FXML
    public void initialize() {
        userId = sessionManager.getLoggedInUserId();
        if (userId == -1) {
            showAlert("Ошибка", "Пользователь не авторизован.");
            return;
        }

        loadChats();

        chatList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                try {
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

        try {
            String messageId = UUID.randomUUID().toString();
            inMemoryDatabase.addMessage(chatId, userId, message);

            updateLastMessage(chatId, message);

            messageInput.clear();
            loadChatMessages();
        } catch (Exception e) {
            handleDatabaseError(e);
        }
    }

    /**
     * Загрузка списка чатов.
     */
    private void loadChats() {
        try {
            var chats = inMemoryDatabase.getChatsForUser(userId);
            chatList.getItems().clear();
            for (var chat : chats) {
                int chatId = chat.getChatId();
                String lastMessage = chat.getLastMessage();
                LocalDateTime lastMessageTime = chat.getLastMessageTime();

                Timestamp timestamp = Timestamp.valueOf(lastMessageTime);

                chatList.getItems().add(String.format("[%d] %s - %s", chatId, timestamp, lastMessage));
            }
        } catch (Exception e) {
            handleDatabaseError(e);
        }
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

    /**
     * Загрузка сообщений чата из базы данных.
     */
    private void loadChatMessages() {
        if (chatId == -1) {
            logger.warning("Chat ID is not set. Cannot load messages.");
            return;
        }

        try {
            var messages = inMemoryDatabase.getMessagesForChat(chatId);

            chatMessagesContainer.getChildren().clear();
            for (var message : messages) {
                int senderId = message.getSenderId();
                String senderName = message.getSenderName();
                String messageText = message.getMessageText();
                LocalDateTime sentTime = message.getSentTime();

                LocalDateTime localSentTime = sentTime;
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                String formattedTime = localSentTime.format(formatter);

                Label messageLabel = new Label(String.format("[%s] %s:(%s)", senderName, messageText, formattedTime));

                if (senderId == sessionManager.getLoggedInUserId()) {
                    messageLabel.getStyleClass().add("user-message");
                } else {
                    messageLabel.getStyleClass().add("other-message");
                }

                HBox messageBox = new HBox();
                messageBox.setAlignment(senderId == sessionManager.getLoggedInUserId()
                        ? javafx.geometry.Pos.CENTER_RIGHT
                        : javafx.geometry.Pos.CENTER_LEFT);
                messageBox.getChildren().add(messageLabel);

                chatMessagesContainer.getChildren().add(messageBox);
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
        try {
            inMemoryDatabase.updateLastMessage(chatId, lastMessage);
        } catch (Exception e) {
            handleDatabaseError(e);
        }
    }

    /**
     * Проверка существования чата.
     */
    private boolean chatExists(int user1Id, int user2Id) {
        try {
            return inMemoryDatabase.chatExists(user1Id, user2Id);
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