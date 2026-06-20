package com.example.coffeeshopmobile.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeeshopmobile.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    // ĐÃ ĐỔI THÀNH USERNAME
    var username by mutableStateOf("")
    var password by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var isLoginSuccess by mutableStateOf(false)

    fun login() {
        val cleanUsername = username.trim()

        if (cleanUsername.isEmpty()) {
            errorMessage = "Vui lòng nhập tên tài khoản."
            return
        }
        if (password.isEmpty()) {
            errorMessage = "Vui lòng nhập mật khẩu."
            return
        }

        errorMessage = null
        isLoading = true

        viewModelScope.launch {
            // GỌI HÀM KIỂM TRA BCRYPT VỪA TẠO
            val error = repository.loginWithUsername(cleanUsername, password)
            if (error == null) {
                isLoginSuccess = true
            } else {
                errorMessage = error
            }
            isLoading = false
        }
    }
}