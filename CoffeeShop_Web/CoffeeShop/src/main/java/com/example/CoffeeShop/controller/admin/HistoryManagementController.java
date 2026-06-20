package com.example.CoffeeShop.controller.admin;

import com.example.CoffeeShop.entity.Invoice;
import com.example.CoffeeShop.service.InvoiceService;
import com.example.CoffeeShop.util.PageSlice;
import com.example.CoffeeShop.util.PaginationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * HistoryManagementController - Xử lý trang quản lý lịch sử hoá đơn
 */
@Controller
public class HistoryManagementController {

    private final InvoiceService invoiceService;

    public HistoryManagementController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping("/admin/history_management")
    public String historyManagementPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        try {
            // 1. Lấy toàn bộ danh sách hóa đơn
            List<Invoice> allInvoices = invoiceService.getAllInvoices().get();
            if (allInvoices == null) {
                allInvoices = new ArrayList<>();
            }

            // Sắp xếp hóa đơn mới nhất lên đầu (Dựa vào thời gian tạo)
            allInvoices.sort((a, b) -> {
                if (a.getCreatedAt() == null)
                    return 1;
                if (b.getCreatedAt() == null)
                    return -1;
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            });

            PageSlice<Invoice> slice = PaginationUtils.slice(allInvoices, page, size);
            slice.applyToModel(model, "invoiceList");
            model.addAttribute("allInvoicesList", allInvoices); // Dữ liệu tất cả (để xuất Excel)

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("invoiceList", new ArrayList<>());
            model.addAttribute("allInvoicesList", new ArrayList<>());
            model.addAttribute("message", "Lỗi khi tải dữ liệu lịch sử hóa đơn từ Firebase!");
            model.addAttribute("messageType", "error");
        }
        return "admin/history_management";
    }
}
