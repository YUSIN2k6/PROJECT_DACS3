package com.example.CoffeeShop.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String uri = request.getRequestURI();

        // 1. Bỏ qua không kiểm tra các đường dẫn mặc định (login, css, js, font...)
        if (uri.startsWith("/login") || uri.startsWith("/logout") || uri.startsWith("/css/") ||
                uri.startsWith("/js/") || uri.startsWith("/images/") || uri.startsWith("/error")) {
            return true;
        }

        // Nếu người dùng vào thẳng localhost:8080/ -> Đá về trang login
        if (uri.equals("/")) {
            response.sendRedirect("/login");
            return false;
        }

        HttpSession session = request.getSession();
        Boolean isAuthenticated = (Boolean) session.getAttribute("isAuthenticated");
        String role = (String) session.getAttribute("userRole");

        // 2. Kẻ lạ mặt (Chưa đăng nhập) -> Lập tức đá về Login
        if (isAuthenticated == null || !isAuthenticated) {
            response.sendRedirect("/login");
            return false;
        }

        // 3. Phân quyền chặt chẽ (Tránh nhập link lộ liễu)
        // - Nếu link bắt đầu bằng /admin mà Role không phải admin -> Đuổi về login
        // (hoặc trang của họ)
        if (uri.startsWith("/admin") && !"admin".equalsIgnoreCase(role)) {
            response.sendRedirect("/login");
            return false;
        }

        // - Nếu link bắt đầu bằng /cashier mà Role không phải cashier -> Đuổi về login
        if (uri.startsWith("/cashier") && !"cashier".equalsIgnoreCase(role)) {
            response.sendRedirect("/login");
            return false;
        }

        // Nếu vượt qua mọi bài kiểm tra -> Cho phép đi tiếp vào Controller
        return true;
    }
}