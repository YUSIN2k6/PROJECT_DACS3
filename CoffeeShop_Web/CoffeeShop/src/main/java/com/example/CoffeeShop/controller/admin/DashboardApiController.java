package com.example.CoffeeShop.controller.admin;

import com.example.CoffeeShop.entity.*;
import com.example.CoffeeShop.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/api/dashboard")
public class DashboardApiController {

    private final InvoiceService invoiceService;
    private final StaffService staffService;
    private final TableService tableService;
    private final MenuItemService menuItemService;
    private final CategoryService categoryService;

    public DashboardApiController(InvoiceService invoiceService, StaffService staffService,
            TableService tableService, MenuItemService menuItemService,
            CategoryService categoryService) {
        this.invoiceService = invoiceService;
        this.staffService = staffService;
        this.tableService = tableService;
        this.menuItemService = menuItemService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<?> getDashboardData(@RequestParam(defaultValue = "week") String filter) throws Exception {
        // 1. Fetch toàn bộ dữ liệu
        List<Invoice> invoices = invoiceService.getAllInvoices().get();
        List<Staff> staffs = staffService.getAllStaff().get();
        List<Table> tables = tableService.getAllTables().get();
        List<MenuItem> menuItems = menuItemService.getAllMenuItems().get();
        List<Category> categories = categoryService.getAllCategories().get();

        if (invoices == null)
            invoices = new ArrayList<>();
        if (menuItems == null)
            menuItems = new ArrayList<>();
        if (categories == null)
            categories = new ArrayList<>();

        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();

        // 2. TÍNH 4 Ô THỐNG KÊ (Đã cập nhật đếm số món đang bán)
        int totalStaff = staffs != null ? staffs.size() : 0;
        int totalTables = tables != null ? tables.size() : 0;
        double monthRevenue = 0;

        // Đếm số món nước đang ở trạng thái "available"
        int totalActiveMenuItems = (int) menuItems.stream()
                .filter(item -> "available".equalsIgnoreCase(item.getStatus()))
                .count();

        Map<String, Integer> topDrinksMap = new HashMap<>();
        Map<String, Double> categorySalesMap = new HashMap<>();

        // Map tên món ra tên danh mục để vẽ biểu đồ tròn
        Map<String, String> catIdToName = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        Map<String, String> itemNameToCatName = new HashMap<>();
        for (MenuItem item : menuItems) {
            if (item.getName() != null && item.getCategoryId() != null) {
                itemNameToCatName.put(item.getName(), catIdToName.getOrDefault(item.getCategoryId(), "Khác"));
            }
        }

        List<Invoice> paidInvoices = invoices.stream()
                .filter(i -> "paid".equals(i.getStatus()))
                .toList();

        // 3. Quét qua các hóa đơn đã thanh toán để gom số liệu
        for (Invoice inv : paidInvoices) {
            LocalDate invDate = parseDateSafely(inv.getInvoiceDate());
            if (invDate == null)
                continue;

            boolean isThisMonth = (invDate.getMonthValue() == currentMonth && invDate.getYear() == currentYear);

            if (isThisMonth) {
                monthRevenue += inv.getTotalAmount();
            }

            if (inv.getItems() != null) {
                for (InvoiceItem item : inv.getItems()) {
                    // Gom Top Nước Bán Chạy và Doanh thu Danh Mục (bất chấp bộ lọc là gì để biểu đồ
                    // dưới có data)
                    topDrinksMap.put(item.getItemName(),
                            topDrinksMap.getOrDefault(item.getItemName(), 0) + item.getQuantity());
                    String catName = itemNameToCatName.getOrDefault(item.getItemName(), "Khác");
                    categorySalesMap.put(catName, categorySalesMap.getOrDefault(catName, 0.0) + item.getTotal());
                }
            }
        }

        // 4. Xử lý Biểu đồ Doanh thu Tổng quan theo bộ lọc
        List<String> revLabels = new ArrayList<>();
        List<Double> revData = new ArrayList<>();

        if ("year".equals(filter)) {
            double[] months = new double[12];
            for (Invoice inv : paidInvoices) {
                LocalDate d = parseDateSafely(inv.getInvoiceDate());
                if (d != null && d.getYear() == currentYear) {
                    months[d.getMonthValue() - 1] += inv.getTotalAmount();
                }
            }
            for (int i = 1; i <= 12; i++) {
                revLabels.add("Tháng " + i);
                revData.add(months[i - 1]);
            }
        } else if ("month".equals(filter)) {
            double[] weeks = new double[4];
            for (Invoice inv : paidInvoices) {
                LocalDate d = parseDateSafely(inv.getInvoiceDate());
                if (d != null && d.getYear() == currentYear && d.getMonthValue() == currentMonth) {
                    int day = d.getDayOfMonth();
                    if (day <= 7)
                        weeks[0] += inv.getTotalAmount();
                    else if (day <= 14)
                        weeks[1] += inv.getTotalAmount();
                    else if (day <= 21)
                        weeks[2] += inv.getTotalAmount();
                    else
                        weeks[3] += inv.getTotalAmount();
                }
            }
            revLabels.addAll(Arrays.asList("Tuần 1", "Tuần 2", "Tuần 3", "Tuần 4"));
            for (double w : weeks)
                revData.add(w);
        } else {
            // Tuần qua (7 ngày gần nhất)
            for (int i = 6; i >= 0; i--) {
                LocalDate targetDate = today.minusDays(i);
                revLabels.add(targetDate.format(DateTimeFormatter.ofPattern("dd/MM")));
                double dayRev = 0;
                for (Invoice inv : paidInvoices) {
                    LocalDate invD = parseDateSafely(inv.getInvoiceDate());
                    if (targetDate.equals(invD))
                        dayRev += inv.getTotalAmount();
                }
                revData.add(dayRev);
            }
        }

        // Đóng gói JSON trả về Frontend
        Map<String, Object> result = new HashMap<>();
        result.put("monthRevenue", monthRevenue);
        result.put("totalMenuItems", totalActiveMenuItems); // Dữ liệu mới thay cho số ly
        result.put("totalStaff", totalStaff);
        result.put("totalTables", totalTables);

        // Lọc lấy Top 5
        List<Map.Entry<String, Integer>> top5 = topDrinksMap.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5).toList();
        result.put("topDrinkLabels", top5.stream().map(Map.Entry::getKey).toList());
        result.put("topDrinkData", top5.stream().map(Map.Entry::getValue).toList());

        result.put("catLabels", new ArrayList<>(categorySalesMap.keySet()));
        result.put("catData", new ArrayList<>(categorySalesMap.values()));

        result.put("revLabels", revLabels);
        result.put("revData", revData);

        return ResponseEntity.ok(result);
    }

    private LocalDate parseDateSafely(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty())
            return null;
        try {
            return LocalDateTime.parse(dateStr.replace(" ", "T")).toLocalDate();
        } catch (Exception e) {
            try {
                String[] parts = dateStr.split("[ /:-]");
                if (parts.length >= 3 && parts[2].length() == 4) {
                    return LocalDate.of(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[0]));
                }
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }
}