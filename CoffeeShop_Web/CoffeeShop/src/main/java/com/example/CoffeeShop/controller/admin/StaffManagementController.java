package com.example.CoffeeShop.controller.admin;

import com.example.CoffeeShop.entity.Staff;
import com.example.CoffeeShop.service.StaffAccountService;
import com.example.CoffeeShop.service.StaffService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import com.example.CoffeeShop.util.PageSlice;
import com.example.CoffeeShop.util.PaginationUtils;

/**
 * StaffManagementController - Xử lý trang quản lý Nhân sự
 */
@Controller
public class StaffManagementController {

    private final StaffService staffService;
    private final StaffAccountService staffAccountService;

    // Tiêm StaffService qua Constructor
    public StaffManagementController(StaffService staffService, StaffAccountService staffAccountService) {
        this.staffService = staffService;
        this.staffAccountService = staffAccountService;
    }

    /**
     * Hiển thị trang quản lý Nhân sự
     */
    @GetMapping("/admin/staff_management")
    public String staffManagementPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int size,
            Model model) {
        try {
            List<Staff> allStaff = staffService.getAllStaff().get();
            PageSlice<Staff> slice = PaginationUtils.slice(allStaff, page, size);
            slice.applyToModel(model, "staffList");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("staffList", new ArrayList<>());
            model.addAttribute("message", "Lỗi khi kết nối cơ sở dữ liệu Firebase!");
            model.addAttribute("messageType", "error");
        }
        return "admin/staff_management";
    }

    @PostMapping("/admin/api/staff")
    @ResponseBody
    public ResponseEntity<?> createStaff(@RequestBody StaffAccountService.CreateStaffCommand request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(staffAccountService.createStaffWithUser(request));
    }

    @GetMapping("/admin/api/staff/{staffId}")
    @ResponseBody
    public ResponseEntity<?> getStaffDetail(@PathVariable String staffId) {
        return ResponseEntity.ok(staffAccountService.getStaffDetail(staffId));
    }

    @PutMapping("/admin/api/staff/{staffId}")
    @ResponseBody
    public ResponseEntity<?> updateStaff(@PathVariable String staffId, @RequestBody StaffAccountService.UpdateStaffCommand request) {
        return ResponseEntity.ok(staffAccountService.updateStaffWithUser(staffId, request));
    }

    @DeleteMapping("/admin/api/staff/{staffId}")
    @ResponseBody
    public ResponseEntity<?> deleteStaff(@PathVariable String staffId) {
        return ResponseEntity.ok(staffAccountService.deleteStaffWithUser(staffId));
    }
}
