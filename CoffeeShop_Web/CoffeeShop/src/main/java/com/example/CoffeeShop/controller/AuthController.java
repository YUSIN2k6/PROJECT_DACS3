package com.example.CoffeeShop.controller;

import com.example.CoffeeShop.entity.User;
import com.example.CoffeeShop.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import java.util.concurrent.ExecutionException;

/**
 * AuthController - Xử lý xác thực mật khẩu băm BCrypt thật từ Firebase
 */
@Controller
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder; // Thêm bộ mã hóa mật khẩu

    // Tiêm cả UserService và PasswordEncoder qua Constructor
    public AuthController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Hiển thị trang đăng nhập
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Xử lý kiểm tra dữ liệu và đăng nhập từ Firebase
     */
    @PostMapping("/login")
    public String handleLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // 1. XỬ LÝ LỖI TRỐNG DỮ LIỆU
        if (username == null || username.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Vui lòng nhập tài khoản hoặc email!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/login";
        }

        if (password == null || password.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Vui lòng nhập mật khẩu!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/login";
        }

        try {
            // 2. TÌM KIẾM TÀI KHOẢN TRÊN FIREBASE
            User user = userService.getUserByUsername(username.trim()).get();

            // 3. KIỂM TRA ĐĂNG NHẬP BẰNG BCRYPT MATCHES
            // passwordEncoder.matches sẽ tự giải mã chuỗi muối ngẫu nhiên trên Firebase và
            // đối chiếu
            if (user != null && passwordEncoder.matches(password, user.getPasswordHash())) {

                // Cập nhật thời gian đăng nhập lên Firebase
                userService.updateLastLogin(user.getId()).get();

                // Thiết lập Session lưu trạng thái đăng nhập
                session.setAttribute("loggedInUser", user.getUsername());
                session.setAttribute("userRole", user.getRole());
                session.setAttribute("isAuthenticated", true);

                redirectAttributes.addFlashAttribute("message", "Đăng nhập thành công!");
                redirectAttributes.addFlashAttribute("messageType", "success");

                // Điều hướng trang theo phân quyền (Role)
                if ("admin".equalsIgnoreCase(user.getRole())) {
                    return "redirect:/admin/dashboardAdmin";
                } else {
                    return "redirect:/cashier/dashboardCashier";
                }

            } else {
                // Xử lý lỗi sai thông tin đăng nhập hoặc không tìm thấy user
                redirectAttributes.addFlashAttribute("message", "Tài khoản hoặc mật khẩu không chính xác!");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/login";
            }

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "Hệ thống kết nối cơ sở dữ liệu thất bại!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/login";
        }
    }

    /**
     * Xử lý đăng xuất
     */
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate(); // Xóa và hủy toàn bộ Session cũ

        redirectAttributes.addFlashAttribute("message", "Đăng xuất thành công!");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/login";
    }
}