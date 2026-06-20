package com.example.CoffeeShop.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * DashboardAdminController - Xử lý Dashboard Admin
 */
@Controller
public class DashboardAdminController {

    /**
     * Hiển thị Dashboard
     */
    @GetMapping("/admin/dashboardAdmin")
    public String dashboardAdminPage() {
        return "admin/dashboardAdmin";
    }
}
