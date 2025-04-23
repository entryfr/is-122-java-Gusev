package org.example.main.models;

import javafx.scene.image.Image;
import java.time.LocalDateTime;

public class Ad {
    private int adId;
    private int sellerId;
    private String title;
    private int categoryId;
    private double price;
    private String description;
    private byte[] image;
    private String location;
    private String status;
    private LocalDateTime publicationDate; // Добавляем новое поле

    public Ad() {}

    // Обновляем конструктор
    public Ad(int adId, int sellerId, String title, int categoryId, double price,
              String description, byte[] image, String location, String status,
              LocalDateTime publicationDate) {
        this.adId = adId;
        this.sellerId = sellerId;
        this.title = title;
        this.categoryId = categoryId;
        this.price = price;
        this.description = description;
        this.image = image;
        this.location = location;
        this.status = status;
        this.publicationDate = publicationDate;
    }

    // Добавляем геттер и сеттер для publicationDate
    public LocalDateTime getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(LocalDateTime publicationDate) {
        this.publicationDate = publicationDate;
    }
    /**
     * Геттеры и сеттеры.
     */
    public int getAdId() {
        return adId;
    }

    public void setAdId(int adId) {
        this.adId = adId;
    }

    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public Image getImageAsFXImage() {
        if (image == null || image.length == 0) {
            return null;
        }
        return new Image(new java.io.ByteArrayInputStream(image));
    }


    @Override
    public String toString() {
        return title != null ? title +" "+ price +" руб."  : "Без названия";
    }
}