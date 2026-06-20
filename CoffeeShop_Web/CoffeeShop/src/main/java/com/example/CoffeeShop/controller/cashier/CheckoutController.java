package com.example.CoffeeShop.controller.cashier;

import com.example.CoffeeShop.entity.Table;
import com.example.CoffeeShop.service.TableService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CheckoutController {

    private final TableService tableService;

    public CheckoutController(TableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping("/cashier/checkout")
    public String checkoutPage(Model model) {
        try {
            List<Table> allTables = tableService.getAllTables().get();
            List<Table> checkoutTables = new ArrayList<>();

            if (allTables != null) {
                for (Table t : allTables) {
                    // ĐÃ SỬA: Chỉ nạp vào danh sách tính tiền những bàn "Đã phục vụ" hoặc "Chờ tính
                    // tiền"
                    String status = t.getStatus();
                    if ("pending".equals(status) || "served".equals(status)) {
                        checkoutTables.add(t);
                    }
                }
            }
            model.addAttribute("pendingTablesList", checkoutTables);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("pendingTablesList", new ArrayList<>());
        }
        return "cashier/checkout";
    }
}