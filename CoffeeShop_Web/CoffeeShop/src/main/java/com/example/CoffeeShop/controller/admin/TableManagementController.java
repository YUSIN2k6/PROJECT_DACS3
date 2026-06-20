package com.example.CoffeeShop.controller.admin;

import com.example.CoffeeShop.entity.Table;
import com.example.CoffeeShop.service.TableService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * TableManagementController - Xử lý trang quản lý bàn và các tác vụ CRUD
 */
@Controller
public class TableManagementController {
    private final TableService tableService;

    public TableManagementController(TableService tableService) {
        this.tableService = tableService;
    }

    /**
     * Hiển thị trang quản lý bàn với dữ liệu từ Firebase
     */
    @GetMapping("/admin/table_management")
    public String tableManagementPage(Model model) {
        try {
            List<Table> tables = tableService.getAllTables().get();
            if (tables == null) {
                tables = new ArrayList<>();
            }
            model.addAttribute("tables", tables);
            model.addAttribute("totalTables", tables.size());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("tables", new ArrayList<>());
            model.addAttribute("totalTables", 0);
        }
        return "admin/table_management";
    }

    /**
     * Xử lý thêm bàn mới tự động lũy tiến số bàn
     */
    @PostMapping("/admin/table_management/add")
    public String addTable(RedirectAttributes redirectAttributes) {
        try {
            List<Table> tables = tableService.getAllTables().get();
            int maxNumber = 0;
            if (tables != null) {
                for (Table t : tables) {
                    if (t.getTableNumber() > maxNumber) {
                        maxNumber = t.getTableNumber();
                    }
                }
            }
            int newNumber = maxNumber + 1;
            String newId = String.format("TABLE%03d", newNumber); // Tạo ID định dạng TBL011, TBL012...

            tableService.createTable(newId, newNumber).get();

            redirectAttributes.addFlashAttribute("message", "Thêm bàn số " + newNumber + " thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "Lỗi khi thêm bàn mới trên hệ thống!");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/table_management";
    }

    /**
     * Xử lý xóa bàn dựa trên số bàn nhập vào từ giao diện
     */
    @PostMapping("/admin/table_management/delete")
    public String deleteTable(@RequestParam("tableNumber") int tableNumber, RedirectAttributes redirectAttributes) {
        try {
            List<Table> tables = tableService.getAllTables().get();
            Table targetTable = null;

            if (tables != null) {
                for (Table t : tables) {
                    if (t.getTableNumber() == tableNumber) {
                        targetTable = t;
                        break;
                    }
                }
            }

            if (targetTable != null) {
                tableService.deleteTable(targetTable.getId()).get();
                redirectAttributes.addFlashAttribute("message", "Xóa thành công bàn số " + tableNumber + "!");
                redirectAttributes.addFlashAttribute("messageType", "success");
            } else {
                redirectAttributes.addFlashAttribute("message", "Không tìm thấy bàn số " + tableNumber + " để xóa!");
                redirectAttributes.addFlashAttribute("messageType", "error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "Lỗi hệ thống khi thực hiện xóa bàn!");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/table_management";
    }
}