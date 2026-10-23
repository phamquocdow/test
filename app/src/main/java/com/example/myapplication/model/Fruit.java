package com.example.myapplication.model;

import com.example.myapplication.util.DateUtils;

public class Fruit {
    private long id;
    private String name;
    private String category;
    private double price;
    private int quantity;
    private String expiryDate; // yyyy-MM-dd
    private String description;

    public Fruit() {
    }

    public Fruit(long id, String name, String category, double price, int quantity,
                 String expiryDate, String description) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.description = description;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isExpired() {
        return DateUtils.isExpired(expiryDate);
    }

    public boolean isInStock() {
        return quantity > 0;
    }
}
