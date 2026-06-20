package com.example.coffeeshopmobile.base

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * BaseRepository - Lớp cha quản lý kết nối Firebase cho toàn bộ Repository
 */
open class BaseRepository {

    // Khởi tạo sẵn Firebase Auth dùng chung cho việc đăng nhập/phân quyền
    protected val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    // Khởi tạo sẵn Realtime Database trỏ thẳng vào gốc dữ liệu của quán
    protected val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    /**
     * Lấy ID của nhân viên đang đăng nhập hiện tại
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Kiểm tra xem có nhân viên nào đang đăng nhập trong máy không
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Đăng xuất tài khoản nhân viên khỏi thiết bị
     */
    fun logout() {
        auth.signOut()
    }
}