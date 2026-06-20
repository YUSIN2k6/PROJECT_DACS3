package com.example.CoffeeShop.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveOrder {
    private String notes;
    private List<OrderItem> items; // Chứa danh sách OrderItem vừa tạo ở Bước 1
}