package com.example.coffeeshopmobile.data.repository

import com.example.coffeeshopmobile.base.BaseRepository
import com.example.coffeeshopmobile.data.model.User
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import org.mindrot.jbcrypt.BCrypt

class AuthRepository : BaseRepository() {
    private val databaseRef = FirebaseDatabase.getInstance().getReference("users")

    suspend fun loginWithUsername(username: String, pass: String): String? {
        return try {
            // 1. Tìm tài khoản theo username trong nhánh "users"
            val snapshot = databaseRef.orderByChild("username").equalTo(username).get().await()

            if (!snapshot.exists()) {
                return "Tài khoản không tồn tại!"
            }

            // Lấy dữ liệu User ra
            val userSnapshot = snapshot.children.first()
            val user = userSnapshot.getValue(User::class.java)

            if (user == null) {
                return "Lỗi dữ liệu tài khoản!"
            }

            if (user.status != "active") {
                return "Tài khoản đang bị khóa!"
            }

            if (user.password_hash.isEmpty()) {
                return "Tài khoản này chưa được thiết lập mật khẩu!"
            }

            // 2. LOGIC KIỂM TRA MẬT KHẨU THÔNG MINH
            val isBcrypt = user.password_hash.startsWith("$2a$") ||
                    user.password_hash.startsWith("$2b$") ||
                    user.password_hash.startsWith("$2y$")

            val isPasswordCorrect = if (isBcrypt) {
                // TRICK CHUYÊN GIA: Đổi tạm cờ $2b$ hoặc $2y$ về $2a$ để thư viện Android đọc được
                val compatibleHash = user.password_hash.replaceFirst("^\\$2[by]\\$".toRegex(), "\\$2a\\$")
                BCrypt.checkpw(pass, compatibleHash)
            } else {
                pass == user.password_hash
            }

            if (isPasswordCorrect) {
                null // Đăng nhập thành công, không có lỗi
            } else {
                "Mật khẩu không chính xác!"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Lỗi hệ thống: ${e.message}"
        }
    }
}