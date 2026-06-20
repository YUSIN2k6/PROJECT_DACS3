package com.example.coffeeshopmobile.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Giữ nguyên thiết lập màu chủ đạo của quán
private val LightColorScheme = lightColorScheme(
    primary = CoffeePrimary,
    secondary = CoffeeDark,
    background = BackgroundLight,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun CoffeeShopMobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Tắt màu động (Android 12+) để không bị mất màu Nâu cà phê
    content: @Composable () -> Unit
) {
    // 1. Ép toàn bộ App dùng Light Theme (Giao diện sáng) cho dễ nhìn ngoài trời
    val colorScheme = LightColorScheme

    // 2. Đổi màu thanh trạng thái (Status Bar) phía trên cùng của điện thoại
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb() // Đổi thành màu CoffeePrimary
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    // 3. Áp dụng Theme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Lấy từ file Type.kt
        content = content
    )
}