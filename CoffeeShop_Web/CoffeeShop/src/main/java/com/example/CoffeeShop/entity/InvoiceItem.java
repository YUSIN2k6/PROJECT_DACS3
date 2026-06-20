package com.example.CoffeeShop.entity;

import com.google.firebase.database.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItem {
    private int quantity;
    private double total;

    // Xoá các @PropertyName trên đầu biến đi
    private String itemId;
    private String itemName;
    private double unitPrice;

    // ========================================================
    // BỔ SUNG GETTER / SETTER THỦ CÔNG
    // ========================================================

    @PropertyName("item_id")
    public String getItemId() {
        return itemId;
    }

    @PropertyName("item_id")
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    @PropertyName("item_name")
    public String getItemName() {
        return itemName;
    }

    @PropertyName("item_name")
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    @PropertyName("unit_price")
    public double getUnitPrice() {
        return unitPrice;
    }

    @PropertyName("unit_price")
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }
}