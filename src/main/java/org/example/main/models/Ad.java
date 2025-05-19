package org.example.main.models;

import java.time.LocalDateTime;

public class Ad {
    private int adId;
    private int sellerId;
    private int categoryId; // Новое поле
    private String title;
    private double price;
    private String description;
    private byte[] image;
    private String location;
    private String status;
    private String categoryName;
    private LocalDateTime publicationDate; // Добавляем поле

    public Ad() {}

    // Геттеры и сеттеры
    public int getAdId() { return adId; }
    public void setAdId(int adId) { this.adId = adId; }
    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }
    public int getCategoryId() { return categoryId; } // Новый геттер
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; } // Новый сеттер
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public LocalDateTime getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDateTime publicationDate) { this.publicationDate = publicationDate; }

    @Override
    public String toString() {
        return title != null ? title + " " + price + " руб." : "Без названия";
    }
}