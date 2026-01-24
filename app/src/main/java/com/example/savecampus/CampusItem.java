package com.example.savecampus;

public class CampusItem {

    public int id;
    public String name;
    public int availablePortions;
    public String expiresAt;
    public String imagePath;

    public CampusItem(
            int id,
            String name,
            int availablePortions,
            String expiresAt,
            String imagePath
    ) {
        this.id = id;
        this.name = name;
        this.availablePortions = availablePortions;
        this.expiresAt = expiresAt;
        this.imagePath = imagePath;
    }
}
