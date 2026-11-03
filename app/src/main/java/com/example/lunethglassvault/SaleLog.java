package com.example.lunethglassvault;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class SaleLog extends RealmObject {

    @PrimaryKey
    private String uuid;          // Unique transaction ID (e.g., UUID.randomUUID().toString())
    private String userUuid;      // UUID of the employee who made the sale (for multi-user filtering)
    private String itemCode;      // Linked product code
    private String facturaNumber; // Linked supplier invoice
    private int quantitySold;     // Units sold to customer
    private double totalPrice;    // Calculated total (price * quantitySold)
    private long timestamp;       // System.currentTimeMillis() when the sale occurred

    // Default empty constructor (Required by Realm)
    public SaleLog() {
    }

    // Constructor with parameters
    public SaleLog(String uuid, String userUuid, String itemCode, String facturaNumber, int quantitySold, double totalPrice, long timestamp) {
        this.uuid = uuid;
        this.userUuid = userUuid;
        this.itemCode = itemCode;
        this.facturaNumber = facturaNumber;
        this.quantitySold = quantitySold;
        this.totalPrice = totalPrice;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getFacturaNumber() {
        return facturaNumber;
    }

    public void setFacturaNumber(String facturaNumber) {
        this.facturaNumber = facturaNumber;
    }

    public int getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(int quantitySold) {
        this.quantitySold = quantitySold;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setImageUri(String capturedImageUriString) {

    }
}