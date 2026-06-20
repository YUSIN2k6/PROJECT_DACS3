package com.example.coffeeshopmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.coffeeshopmobile.navigation.AppNavigation
import com.example.coffeeshopmobile.ui.theme.CoffeeShopMobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CoffeeShopMobileTheme {
                // Gọi bộ não điều hướng tổng của toàn bộ App
                AppNavigation()
            }
        }
    }
}