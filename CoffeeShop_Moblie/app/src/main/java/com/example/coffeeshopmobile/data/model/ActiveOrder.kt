package com.example.coffeeshopmobile.data.model

data class ActiveOrder(
    var notes: String = "",
    var items: List<OrderItem> = emptyList()
)