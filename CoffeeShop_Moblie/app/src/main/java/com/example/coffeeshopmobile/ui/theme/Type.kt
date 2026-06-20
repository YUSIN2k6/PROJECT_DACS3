package com.example.coffeeshopmobile.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Định nghĩa các kiểu chữ dùng chung cho toàn bộ App
val Typography = Typography(
    // 1. Chữ nội dung bình thường (Mô tả, chữ thường)
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),

    // 2. Chữ cho các Tiêu đề lớn (Ví dụ: Chữ "Danh sách bàn" ở đầu trang)
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),

    // 3. Chữ cho các Tiêu đề vừa (Ví dụ: Chữ "BÀN 1" trong các ô vuông)
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // 4. Chữ cho các Nút bấm (Button)
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // 5. Chữ chú thích nhỏ (Ví dụ: Chữ "có khách" hoặc "đang gộp 1")
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.2.sp
    )
)