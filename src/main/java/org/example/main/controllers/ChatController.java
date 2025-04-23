package org.example.main.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.example.main.models.Chat;
import org.example.main.models.Message;
import org.example.main.utils.SceneManager;
import org.example.main.utils.SessionManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

public class ChatController {

    @FXML private ListView<String> chatListView;
    @FXML private VBox messagesContainer;
    @FXML private TextField messageInput;
    @FXML private Button sendButton;
    @FXML private Label chatTitleLabel;
    @FXML private ScrollPane messagesScrollPane;

    private int currentChatId = -1;
    private int currentUserId;
    private String currentUsername;
    private int otherUserId = -1;

    private final InMemoryDatabase database = InMemoryDatabase.getInstance();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final SceneManager sceneManager = SceneManager.getInstance();
    private static final Logger logger = Logger.getLogger(ChatController.class.getName());
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    @FXML
    private void handleBackToIndex() {
        try {
            SceneManager.getInstance().showScene("index");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось вернуться на главную страницу.");
        }
    }
    @FXML
    public void initialize() {
        setupUser();
        setupUI();
        loadUserChats();
        setupEventHandlers();
    }

    private void setupUser() {
        currentUserId = sessionManager.getLoggedInUserId();
        if (currentUserId == -1) {
            showAlert("Ошибка", "Пользователь не авторизован");
            return;
        }
        currentUsername = sessionManager.getLoggedInUsername();
    }

    private void setupUI() {
        sendButton.setDisable(true);
        messagesScrollPane.vvalueProperty().bind(messagesContainer.heightProperty());
        messageInput.setPromptText("Введите сообщение...");
    }

    private void setupEventHandlers() {
        chatListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                handleChatSelection(newVal);
            }
        });

        messageInput.textProperty().addListener((obs, oldVal, newVal) -> {
            sendButton.setDisable(newVal.trim().isEmpty());
        });

        messageInput.setOnAction(event -> sendMessage());
    }

    private void handleChatSelection(String selectedChat) {
        try {
            String[] parts = selectedChat.split(" ");
            currentChatId = Integer.parseInt(parts[0].replace("[", "").replace("]", ""));
            otherUserId = getOtherUserIdFromChat(currentChatId);
            updateChatTitle();
            loadChatMessages();
        } catch (Exception e) {
            logger.severe("Ошибка выбора чата: " + e.getMessage());
            showAlert("Ошибка", "Невозможно загрузить чат");
        }
    }

    private int getOtherUserIdFromChat(int chatId) throws Exception {
        return database.getOtherUserIdInChat(chatId, currentUserId);
    }

    private void updateChatTitle() {
        try {
            String otherUsername = database.getUsernameById(otherUserId);
            chatTitleLabel.setText("Чат с " + otherUsername);
            chatTitleLabel.setTextFill(Color.BLUE);
        } catch (Exception e) {
            chatTitleLabel.setText("Чат");
            logger.warning("Не удалось получить имя собеседника: " + e.getMessage());
        }
    }

    @FXML
    private void sendMessage() {
        String messageText = messageInput.getText().trim();
        if (messageText.isEmpty()) return;

        try {
            database.addMessage(currentChatId, currentUserId, messageText);
            database.updateLastMessage(currentChatId, messageText);

            displayMessage(currentUserId, currentUsername, messageText, LocalDateTime.now());
            messageInput.clear();

            loadUserChats();
        } catch (Exception e) {
            handleDatabaseError(e);
        }
    }

    private void displayMessage(int senderId, String senderName, String text, LocalDateTime time) {
        String timeString = time.format(timeFormatter);
        Label messageLabel = new Label(String.format("%s (%s): %s", senderName, timeString, text));

        HBox messageBox = new HBox(messageLabel);
        messageBox.setMaxWidth(messagesContainer.getWidth() * 0.8);

        if (senderId == currentUserId) {
            messageLabel.setStyle("-fx-background-color: #DCF8C6; -fx-padding: 5px; -fx-background-radius: 5px;");
            messageBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        } else {
            messageLabel.setStyle("-fx-background-color: #ECECEC; -fx-padding: 5px; -fx-background-radius: 5px;");
            messageBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        }

        messagesContainer.getChildren().add(messageBox);
    }

    private void loadUserChats() {
        try {
            List<Chat> chats = database.getChatsForUser(currentUserId);
            chatListView.getItems().clear();

            for (Chat chat : chats) {
                String lastMessageTime = chat.getLastMessageTime().format(timeFormatter);
                String chatEntry = String.format("[%d] %s: %s",
                        chat.getChatId(), lastMessageTime, chat.getLastMessage());
                chatListView.getItems().add(chatEntry);
            }
        } catch (Exception e) {
            handleDatabaseError(e);
        }
    }

    private void loadChatMessages() {
        messagesContainer.getChildren().clear();

        try {
            List<Message> messages = database.getMessagesForChat(currentChatId);
            for (Message message : messages) {
                displayMessage(
                        message.getSenderId(),
                        message.getSenderName(),
                        message.getMessageText(),
                        message.getSentTime()
                );
            }
        } catch (Exception e) {
            handleDatabaseError(e);
        }
    }

    @FXML
    private void backToPrevious() {
        try {
            SceneManager.getInstance().showScene("index");
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось вернуться назад");
        }
    }

    @FXML
    private void refreshChats() {
        loadUserChats();
        if (currentChatId != -1) {
            loadChatMessages();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleDatabaseError(Exception e) {
        logger.severe("Database error: " + e.getMessage());
        showAlert("Ошибка", "Ошибка при работе с базой данных");
    }

    public void setChatId(int chatId) {
        this.currentChatId = chatId;
        try {
            this.otherUserId = getOtherUserIdFromChat(chatId);
            updateChatTitle();
            loadChatMessages();
        } catch (Exception e) {
            handleDatabaseError(e);
        }
    }
}