package com.example.coffeeshopmobile.data.model

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName

@IgnoreExtraProperties
data class MenuItem(
    var id: String = "",
    var name: String = "",
    var price: Double = 0.0,
    var status: String = "available", // available (Còn hàng), unavailable (Hết hàng)

    @get:PropertyName("category_id")
    @set:PropertyName("category_id")
    var categoryId: String = "",

    @get:PropertyName("image_url")
    @set:PropertyName("image_url")
    var imageUrl: String? = null,

    var createdAt: String? = null
)