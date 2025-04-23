package org.example.main.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Chat {
    private int chatId;
    private int user1Id;
    private int user2Id;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private List<Message> messages;

    /**
     * Конструктор по умолчанию.
     */
    public Chat() {
        this.messages = new ArrayList<>();
    }

    /**
     * Конструктор с параметрами.
     */
    public Chat(int chatId, int user1Id, int user2Id) {
        this();
        this.chatId = chatId;
        this.user1Id = user1Id;
        this.user2Id = user2Id;
    }

    /**
     * Конструктор для создания ChatInfo.
     */
    public Chat(int chatId, String lastMessage, LocalDateTime lastMessageTime) {
        this.chatId = chatId;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.messages = new ArrayList<>();
    }

    // Геттеры и сеттеры
    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public int getUser1Id() {
        return user1Id;
    }

    public void setUser1Id(int user1Id) {
        this.user1Id = user1Id;
    }

    public int getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(int user2Id) {
        this.user2Id = user2Id;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public List<Message> getMessages() {
        return new ArrayList<>(messages); // Возвращаем копию для безопасности
    }

    /**
     * Добавление нового сообщения в чат.
     */
    public void addMessage(Message message) {
        Objects.requireNonNull(message, "Сообщение не может быть null");
        messages.add(message);
        updateLastMessage(message);
    }

    private void updateLastMessage(Message message) {
        this.lastMessage = message.getMessageText();
        this.lastMessageTime = message.getSentTime();
    }

    /**
     * Очистка списка сообщений.
     */
    public void clearMessages() {
        messages.clear();
        this.lastMessage = null;
        this.lastMessageTime = null;
    }

    /**
     * Проверка участия пользователя в чате.
     */
    public boolean isParticipant(int userId) {
        return this.user1Id == userId || this.user2Id == userId;
    }

    /**
     * Получение ID собеседника.
     */
    public int getOtherParticipantId(int currentUserId) {
        if (!isParticipant(currentUserId)) {
            throw new IllegalArgumentException("Пользователь не является участником чата");
        }
        return user1Id == currentUserId ? user2Id : user1Id;
    }

    /**
     * Создает легковесный объект с основной информацией о чате.
     */
    public ChatInfo toChatInfo() {
        return new ChatInfo(chatId, lastMessage, lastMessageTime);
    }

    @Override
    public String toString() {
        return String.format(
                "Chat{id=%d, users=[%d,%d], lastMsg='%s', lastTime=%s}",
                chatId, user1Id, user2Id, lastMessage, lastMessageTime
        );
    }

    /**
     * Вложенный класс для представления краткой информации о чате.
     */
    public static class ChatInfo {
        private final int chatId;
        private final String lastMessage;
        private final LocalDateTime lastMessageTime;

        public ChatInfo(int chatId, String lastMessage, LocalDateTime lastMessageTime) {
            this.chatId = chatId;
            this.lastMessage = lastMessage;
            this.lastMessageTime = lastMessageTime;
        }

        public int getChatId() {
            return chatId;
        }

        public String getLastMessage() {
            return lastMessage;
        }

        public LocalDateTime getLastMessageTime() {
            return lastMessageTime;
        }

        @Override
        public String toString() {
            return String.format(
                    "ChatInfo{id=%d, lastMsg='%s', lastTime=%s}",
                    chatId, lastMessage, lastMessageTime
            );
        }
    }
}