package com.example.lunethglassvault;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class FacturaItem extends RealmObject {

    @PrimaryKey
    private String code;            // Item/Product Barcode or Internal Code
    private String facturaNumber;   // Supplier invoice reference code
    private String supplier;        // Name of the glassware manufacturer/supplier
    private String name;            // Product name
    private double price;           // Selling price per unit
    private String imageUri;        // File path to the photo snapped via Camera
    private int stockQuantity;      // Remaining quantity available

    // Default empty constructor (Required by Realm)
    public FacturaItem() {
    }

    // Constructor with parameters
    public FacturaItem(String code, String facturaNumber, String supplier, String name, double price, String imageUri, int stockQuantity) {
        this.code = code;
        this.facturaNumber = facturaNumber;
        this.supplier = supplier;
        this.name = name;
        this.price = price;
        this.imageUri = imageUri;
        this.stockQuantity = stockQuantity;
    }

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFacturaNumber() {
        return facturaNumber;
    }

    public void setFacturaNumber(String facturaNumber) {
        this.facturaNumber = facturaNumber;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
}