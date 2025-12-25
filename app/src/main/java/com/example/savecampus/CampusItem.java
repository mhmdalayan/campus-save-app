package com.example.savecampus;

public class CampusItem {
    private final String name;
    private final String price;
    private final String description;
    private final int imageId;

    public CampusItem(String name, String price, String description, int imageId) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageId = imageId;
    }

    // --- Getters to access the data ---
    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public int getImageId() {
        return imageId;
    }
}
