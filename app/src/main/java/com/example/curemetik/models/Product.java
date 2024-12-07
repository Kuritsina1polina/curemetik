package com.example.curemetik.models;

import java.util.List;

public class Product {
    private String name;
    private float rating;
    private List<String> components;
    private String imageUrl;

    public Product() {
        // Default constructor required for calls to DataSnapshot.getValue(Product.class)
    }

    public Product(String name, float rating, List<String> components, String imageUrl) {
        this.name = name;
        this.rating = rating;
        this.components = components;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public float getRating() {
        return rating;
    }

    public List<String> getComponents() {
        return components;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
