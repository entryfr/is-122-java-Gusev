package org.example.main.models;

public class Ad {
    private int adId;
    private int userId; // Идентификатор пользователя (продавца)
    private String title;
    private int categoryId;
    private double price;
    private String description;
    private byte[] image;
    private String location;
    private String status; // Статус объявления (например, "active" или "sold")

    public int getSellerId() {
        return userId; // Возвращаем userId как sellerId
    }

    public void setSellerId(int sellerId) {
        this.userId = sellerId; // Устанавливаем userId как sellerId
    }

    public int getAdId() {
        return adId;
    }

    public void setAdId(int adId) {
        this.adId = adId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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
}