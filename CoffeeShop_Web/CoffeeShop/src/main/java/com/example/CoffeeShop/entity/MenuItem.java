package com.example.CoffeeShop.entity;

import com.google.firebase.database.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {
    private String id;
    private String name;
    private double price;
    private String status;

    private String categoryId;
    private String imageUrl;
    private String createdAt;

    @PropertyName("category_id")
    public String getCategoryId() {
        return categoryId;
    }

    @PropertyName("category_id")
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    @PropertyName("image_url")
    public String getImageUrl() {
        return imageUrl;
    }

    @PropertyName("image_url")
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @PropertyName("createdAt")
    public String getCreatedAt() {
        return createdAt;
    }

    @PropertyName("createdAt")
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
}
