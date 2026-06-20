package com.example.coffeeshopmobile.data.model

import com.google.firebase.database.PropertyName

data class OrderItem(
    @get:PropertyName("item_id")
    @set:PropertyName("item_id")
    var itemId: String = "",

    @get:PropertyName("item_name")
    @set:PropertyName("item_name")
    var itemName: String = "",

    var quantity: Int = 1,
    var price: Double = 0.0,
    var served: Boolean = false,
    var timestamp: String = "",

    // THÊM BIẾN NÀY: Để biết món đã bấm nút gửi xuống quầy bar chưa
    var sentToKitchen: Boolean = false
) {
    val total: Double
        get() = price * quantity
}