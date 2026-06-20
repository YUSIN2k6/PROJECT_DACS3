package com.example.coffeeshopmobile.base

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * BaseActivity
 *
 * Project này là 100% Jetpack Compose, vì vậy không dùng ViewBinding / setContentView.
 * Lớp này giữ lại các tiện ích chung như Toast và trạng thái loading.
 */
abstract class BaseActivity : ComponentActivity() {

    /**
     * Compose-friendly loading state that screens can observe.
     * You can wire this into a global LoadingDialog inside setContent if needed.
     */
    var isLoading: Boolean by mutableStateOf(false)
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
        observeViewModel()
    }

    /**
     * Setup UI elements (buttons, listeners, etc.)
     * Activity con override để setup các views của nó
     */
    abstract fun setupUI()

    /**
     * Observe ViewModel LiveData
     * Activity con override nếu cần observe
     */
    open fun observeViewModel() {}

    /**
     * Show global loading state.
     */
    fun showLoading() {
        isLoading = true
    }

    /**
     * Hide global loading state.
     */
    fun hideLoading() {
        isLoading = false
    }

    /**
     * Hiển thị Toast notification
     */
    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }
}