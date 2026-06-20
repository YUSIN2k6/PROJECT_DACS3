package com.example.CoffeeShop.entity;

import com.google.firebase.database.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private String itemId;
    private String itemName;
    private int quantity;
    private double price;
    private boolean served;
    private String timestamp;
    private boolean sentToKitchen;

    // ÉP FIREBASE ĐỌC ĐÚNG TRƯỜNG "item_id" CÓ DẤU GẠCH DƯỚI TỪ JSON
    @PropertyName("item_id")
    public String getItemId() {
        return itemId;
    }

    @PropertyName("item_id")
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    // ÉP FIREBASE ĐỌC ĐÚNG TRƯỜNG "item_name" CÓ DẤU GẠCH DƯỚI TỪ JSON
    @PropertyName("item_name")
    public String getItemName() {
        return itemName;
    }

    @PropertyName("item_name")
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    // ÉP FIREBASE ĐỌC ĐÚNG TRƯỜNG "sentToKitchen" (Hoặc sent_to_kitchen nếu đổi)
    @PropertyName("sentToKitchen")
    public boolean isSentToKitchen() {
        return sentToKitchen;
    }

    @PropertyName("sentToKitchen")
    public void setSentToKitchen(boolean sentToKitchen) {
        this.sentToKitchen = sentToKitchen;
    }
}