package com.example.CoffeeShop.entity;

import com.google.firebase.database.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id;
    private String username;
    private String email;
    private String role;
    private String status;

    private String passwordHash; // Bỏ annotation trên đầu biến này đi

    @PropertyName("staff_id")
    private String staffId;

    @PropertyName("last_login")
    private String lastLogin;

    @PropertyName("createdAt")
    private String createdAt;

    // --- BỔ SUNG THÊM CÁC HÀM GETTER / SETTER THỦ CÔNG ĐỂ ÉP ĐÚNG PHÂN TÁCH NO SỬ
    // DỤNG FIREBASE QUERY ---

    @PropertyName("password_hash")
    public String getPasswordHash() {
        return passwordHash;
    }

    @PropertyName("password_hash")
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @PropertyName("staff_id")
    public String getStaffId() {
        return staffId;
    }

    @PropertyName("staff_id")
    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }
}