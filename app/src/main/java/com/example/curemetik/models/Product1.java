package com.example.curemetik.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Product1 implements Parcelable {
    private String name;
    private double rating;
    private String imageUrl;
    private List<String> components;
    private String category;

    // Пустой конструктор (необходим для Firebase)
    public Product1() {
        // Пустой конструктор
    }

    // Конструктор с параметрами
    public Product1(String name, double rating, String imageUrl, List<String> components, String category) {
        this.name = name;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.components = components;
        this.category = category;
    }

    // Геттеры и сеттеры
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getComponents() {
        return components;
    }

    public void setComponents(List<String> components) {
        this.components = components;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // Реализация методов Parcelable

    protected Product1(Parcel in) {
        name = in.readString();
        rating = in.readDouble();
        imageUrl = in.readString();
        components = in.createStringArrayList();
        category = in.readString();
    }

    public static final Creator<Product1> CREATOR = new Creator<Product1>() {
        @Override
        public Product1 createFromParcel(Parcel in) {
            return new Product1(in);
        }

        @Override
        public Product1[] newArray(int size) {
            return new Product1[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeDouble(rating);
        dest.writeString(imageUrl);
        dest.writeStringList(components);
        dest.writeString(category);
    }
}
