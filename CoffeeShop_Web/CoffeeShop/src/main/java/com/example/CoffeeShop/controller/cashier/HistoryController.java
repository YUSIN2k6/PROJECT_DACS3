package com.example.CoffeeShop.controller.cashier;

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
 * HistoryController - Xử lý trang lịch sử cho Thu Ngân
 */
@Controller("cashierHistoryController") // Thêm tên bean để tránh trùng lặp với admin
public class HistoryController {

    private final InvoiceService invoiceService;

    public HistoryController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping("/cashier/history")
    public String historyPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        try {
            List<Invoice> allInvoices = invoiceService.getAllInvoices().get();
            if (allInvoices == null) {
                allInvoices = new ArrayList<>();
            }

            // Sắp xếp mới nhất lên đầu
            allInvoices.sort((a, b) -> {
                if (a.getCreatedAt() == null)
                    return 1;
                if (b.getCreatedAt() == null)
                    return -1;
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            });

            // Sử dụng tiện ích phân trang chuẩn
            PageSlice<Invoice> slice = PaginationUtils.slice(allInvoices, page, size);
            slice.applyToModel(model, "invoiceList");

            // Truyền mảng tất cả hóa đơn để JS lấy chi tiết
            model.addAttribute("allInvoicesList", allInvoices);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("invoiceList", new ArrayList<>());
            model.addAttribute("allInvoicesList", new ArrayList<>());
        }
        return "cashier/history";
    }
}