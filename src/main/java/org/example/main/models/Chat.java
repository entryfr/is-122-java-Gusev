package org.example.main.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Chat {
    // Поля класса
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
     *
     * @param chatId       ID чата
     * @param user1Id      ID первого пользователя
     * @param user2Id      ID второго пользователя
     */
    public Chat(int chatId, int user1Id, int user2Id) {
        this.chatId = chatId;
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.messages = new ArrayList<>();
    }

    // Геттеры
    public int getChatId() {
        return chatId;
    }

    public int getUser1Id() {
        return user1Id;
    }

    public int getUser2Id() {
        return user2Id;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public List<Message> getMessages() {
        return messages;
    }

    // Сеттеры
    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public void setUser1Id(int user1Id) {
        this.user1Id = user1Id;
    }

    public void setUser2Id(int user2Id) {
        this.user2Id = user2Id;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    /**
     * Добавление нового сообщения в чат.
     *
     * @param message новое сообщение
     */
    public void addMessage(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("Сообщение не может быть null");
        }
        messages.add(message);

        this.lastMessage = message.getMessageText();
        this.lastMessageTime = message.getSentTime();
    }

    /**
     * Очистка списка сообщений (например, при удалении чата).
     */
    public void clearMessages() {
        messages.clear();
        this.lastMessage = null;
        this.lastMessageTime = null;
    }

    /**
     * Проверка, является ли пользователь участником чата.
     *
     * @param userId ID пользователя
     * @return true, если пользователь участвует в чате
     */
    public boolean isParticipant(int userId) {
        return this.user1Id == userId || this.user2Id == userId;
    }

    /**
     * Получение другого участника чата.
     *
     * @param currentUserId ID текущего пользователя
     * @return ID другого участника
     */
    public int getOtherParticipantId(int currentUserId) {
        if (this.user1Id == currentUserId) {
            return this.user2Id;
        } else if (this.user2Id == currentUserId) {
            return this.user1Id;
        } else {
            throw new IllegalArgumentException("Текущий пользователь не является участником чата");
        }
    }

    /**
     * Переопределение метода toString для удобного вывода информации о чате.
     */
    @Override
    public String toString() {
        return String.format(
                "Chat{chatId=%d, user1Id=%d, user2Id=%d, lastMessage='%s', lastMessageTime=%s}",
                chatId, user1Id, user2Id, lastMessage, lastMessageTime
        );
    }
}