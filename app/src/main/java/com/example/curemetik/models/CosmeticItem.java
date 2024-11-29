package com.example.curemetik.models;

public class CosmeticItem {
    private String name;
    private String description;
    private boolean isSelected;

    public CosmeticItem() {
        // Default constructor required for calls to DataSnapshot.getValue(CosmeticItem.class)
    }

    public CosmeticItem(String name, String description, boolean isSelected) {
        this.name = name;
        this.description = description;
        this.isSelected = isSelected;
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
