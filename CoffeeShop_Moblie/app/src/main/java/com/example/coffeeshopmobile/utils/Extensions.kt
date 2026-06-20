package com.example.coffeeshopmobile.utils

import java.text.NumberFormat
import java.util.*

/**
 * Định dạng giá tiền VND
 * Ví dụ: 50000.0 -> "50.000 ₫"
 */
fun Double.formatPrice(): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return "${formatter.format(this)} ₫"
}

fun Int.formatPrice(): String {
    return this.toDouble().formatPrice()
}

// Các hàm check validate chuỗi rất tốt cho form đăng nhập sau này
fun String.isValidEmail(): Boolean = this.matches(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))
fun String.isValidPhone(): Boolean = this.length >= 10 && this.all { it.isDigit() }
fun String.isValidPassword(): Boolean = this.length >= 6
fun String.cleanInput(): String = this.trim().replace(Regex("\\s+"), " ")