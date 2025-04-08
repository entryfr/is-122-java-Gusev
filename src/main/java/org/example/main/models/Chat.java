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

    private String messageInput;

    private List<Message> messages;

    public Chat() {
        this.messages = new ArrayList<>();
    }

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

    public String getMessageInput() {
        return messageInput;
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

    public void setMessageInput(String messageInput) {
        this.messageInput = messageInput;
    }


    public void addMessage(Message message) {
        messages.add(message);
        this.lastMessage = message.getMessageText();
        this.lastMessageTime = message.getSentTime();
    }


    public void clearMessageInput() {
        this.messageInput = "";
    }
}