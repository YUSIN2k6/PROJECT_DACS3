package com.example.CoffeeShop.controller.cashier;

import com.example.CoffeeShop.entity.Category;
import com.example.CoffeeShop.entity.MenuItem;
import com.example.CoffeeShop.service.CategoryService;
import com.example.CoffeeShop.service.MenuItemService;
import com.example.CoffeeShop.util.PageSlice;
import com.example.CoffeeShop.util.PaginationUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * InventoryController - Xử lý trang điều kho
 */
@Controller
public class InventoryController {

    private final MenuItemService menuItemService;
    private final CategoryService categoryService;

    public InventoryController(MenuItemService menuItemService, CategoryService categoryService) {
        this.menuItemService = menuItemService;
        this.categoryService = categoryService;
    }

    /**
     * Hiển thị trang điều kho có phân trang
     */
    @GetMapping("/cashier/inventory")
    public String inventoryPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        try {
            List<MenuItem> allItems = menuItemService.getAllMenuItems().get();
            List<Category> categories = categoryService.getAllCategories().get();

            if (allItems == null)
                allItems = new ArrayList<>();
            if (categories == null)
                categories = new ArrayList<>();

            // Cắt trang cho danh sách hiển thị
            PageSlice<MenuItem> slice = PaginationUtils.slice(allItems, page, size);
            slice.applyToModel(model, "menuList");

            // Truyền toàn bộ để JS làm form Cập nhật
            model.addAttribute("allMenuItems", allItems);
            model.addAttribute("categories", categories);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("menuList", new ArrayList<>());
            model.addAttribute("allMenuItems", new ArrayList<>());
            model.addAttribute("categories", new ArrayList<>());
        }
        return "cashier/inventory";
    }

    @PutMapping("/cashier/api/inventory/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateStatus(@PathVariable String id, @RequestBody UpdateStatusRequest request) {
        try {
            MenuItem item = menuItemService.getMenuItemById(id).get();
            if (item == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy món nước!"));
            }
            item.setStatus(request.status());
            menuItemService.updateMenuItem(id, item).get();
            return ResponseEntity.ok(Map.of("message", "Cập nhật trạng thái thành công!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi server: " + e.getMessage()));
        }
    }

    public record UpdateStatusRequest(String status) {
    }
}