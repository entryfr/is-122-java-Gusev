package org.example.main.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class Chat {
    private int chatId;
    private int user1Id;
    private int user2Id;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private final List<Message> messages;

    /**
     * Конструктор по умолчанию.
     */
    public Chat() {
        this.messages = new ArrayList<>();
    }


    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
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

        @Override
        public String toString() {
            return String.format(
                    "ChatInfo{id=%d, lastMsg='%s', lastTime=%s}",
                    chatId, lastMessage, lastMessageTime
            );
        }
    }
}