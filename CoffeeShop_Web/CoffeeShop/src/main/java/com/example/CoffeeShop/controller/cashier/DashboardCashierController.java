package com.example.CoffeeShop.controller.cashier;

import com.example.CoffeeShop.entity.Table;
import com.example.CoffeeShop.service.TableService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.ArrayList;
import java.util.List;

@Controller
public class DashboardCashierController {

    private final TableService tableService;

    public DashboardCashierController(TableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping("/cashier/dashboardCashier")
    public String dashboardCashierPage(Model model) {
        try {
            List<Table> allTables = tableService.getAllTables().get();
            model.addAttribute("javaTables", allTables != null ? allTables : new ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("javaTables", new ArrayList<>());
        }
        return "cashier/dashboardCashier";
    }
}