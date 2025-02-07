package com.example.curemetik.models;

public class CosmeticItem {
    private String name;
    private String description;
    private boolean isSelected;
    private int rating;

    public CosmeticItem() {
        // Default constructor required for calls to DataSnapshot.getValue(CosmeticItem.class)
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public CosmeticItem(String name, String description, boolean isSelected, int rating) {
        this.name = name;
        this.description = description;
        this.isSelected = isSelected;
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
