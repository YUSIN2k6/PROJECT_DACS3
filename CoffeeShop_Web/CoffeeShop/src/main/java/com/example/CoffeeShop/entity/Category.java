package com.example.CoffeeShop.entity;

import com.google.firebase.database.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    private String id;
    private String name;

    @PropertyName("createdAt")
    private String createdAt;
}