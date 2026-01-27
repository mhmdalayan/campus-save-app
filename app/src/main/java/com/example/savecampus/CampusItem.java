package com.example.savecampus;

public class CampusItem {

    public int id;
    public String name;
    public String description;
    public int availablePortions;
    public String expiresAt;
    public String imagePath;

    public double price;
    public int discountPercent;

    public CampusItem(
            int id,
            String name,
            String description,
            int availablePortions,
            String expiresAt,
            String imagePath,
            double price,
            int discountPercent
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.availablePortions = availablePortions;
        this.expiresAt = expiresAt;
        this.imagePath = imagePath;
        this.price = price;
        this.discountPercent = discountPercent;
    }
}
