package com.example.CoffeeShop.entity;

import com.google.firebase.database.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Staff {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String position;
    private String status;

    @PropertyName("hire_date")
    private String hireDate;

    @PropertyName("createdAt")
    private String createdAt;
}
