package com.example.coffeeshopmobile.data.model

import com.google.firebase.database.PropertyName

data class User(
    var id: String = "",
    var username: String = "",
    var role: String = "",
    var status: String = "",

    // Đã đổi thẳng tên biến thành password_hash giống hệt JSON trên Firebase
    @PropertyName("password_hash")
    var password_hash: String = ""
)