package com.example.curemetik.models;

import java.util.Date;

public class CheckHistory {
    private String productId;
    private Date date;
    private String productName;
    private double rating;
    private String imageUrl;

    // Конструкторы, геттеры и сеттеры

    public CheckHistory() {}

    public CheckHistory(String productId, Date date, String productName, double rating, String imageUrl) {
        this.productId = productId;
        this.date = date;
        this.productName = productName;
        this.rating = rating;
        this.imageUrl = imageUrl;
    }

    public String getProductId() {
        return productId;
    }

    public Date getDate() {
        return date;
    }

    public String getProductName() {
        return productName;
    }

    public double getRating() {
        return rating;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
