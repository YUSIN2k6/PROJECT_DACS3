package com.example.CoffeeShop.entity;

import com.google.firebase.database.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Table {
    private String id;
    private String status;
    private int tableNumber;
    private String createdAt;

    // THÊM BIẾN NÀY: Để đồng bộ với App Mobile
    private Integer mergedWith;

    private ActiveOrder currentOrder;

    @PropertyName("table_number")
    public int getTableNumber() {
        return tableNumber;
    }

    @PropertyName("table_number")
    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    @PropertyName("createdAt")
    public String getCreatedAt() {
        return createdAt;
    }

    @PropertyName("createdAt")
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    // ÉP ĐỌC ĐÚNG TRƯỜNG "current_order" TỪ FIREBASE
    @PropertyName("current_order")
    public ActiveOrder getCurrentOrder() {
        return currentOrder;
    }

    @PropertyName("current_order")
    public void setCurrentOrder(ActiveOrder currentOrder) {
        this.currentOrder = currentOrder;
    }

    // --- BỔ SUNG: ÉP ĐỌC ĐÚNG TRƯỜNG "merged_with" TỪ FIREBASE ---
    @PropertyName("merged_with")
    public Integer getMergedWith() {
        return mergedWith;
    }

    @PropertyName("merged_with")
    public void setMergedWith(Integer mergedWith) {
        this.mergedWith = mergedWith;
    }
}