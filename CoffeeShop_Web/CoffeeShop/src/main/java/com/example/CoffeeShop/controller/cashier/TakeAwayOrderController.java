package com.example.CoffeeShop.controller.cashier;

import com.example.CoffeeShop.entity.Invoice;
import com.example.CoffeeShop.entity.InvoiceItem;
import com.example.CoffeeShop.entity.MenuItem;
import com.example.CoffeeShop.entity.Table;
import com.example.CoffeeShop.repository.TableRepository;
import com.example.CoffeeShop.service.InvoiceService;
import com.example.CoffeeShop.service.MenuItemService;
import com.example.CoffeeShop.service.TableService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TakeAwayOrderController - Thanh toán tại quầy và Tạo hóa đơn độc lập
 */
@Controller
public class TakeAwayOrderController {

    private final MenuItemService menuItemService;
    private final InvoiceService invoiceService;
    private final TableService tableService;
    private final TableRepository tableRepository;

    public TakeAwayOrderController(MenuItemService menuItemService, InvoiceService invoiceService,
            TableService tableService, TableRepository tableRepository) {
        this.menuItemService = menuItemService;
        this.invoiceService = invoiceService;
        this.tableService = tableService;
        this.tableRepository = tableRepository;
    }

    @GetMapping("/cashier/take-away_order")
    public String takeawayorderPage(Model model) {
        try {
            // Lấy thực đơn
            List<MenuItem> allItems = menuItemService.getAllMenuItems().get();
            if (allItems == null) {
                allItems = new ArrayList<>();
            }
            List<MenuItem> activeItems = allItems.stream()
                    .filter(item -> "available".equalsIgnoreCase(item.getStatus()))
                    .collect(Collectors.toList());
            model.addAttribute("menuItems", activeItems);

            // Lấy danh sách bàn để đẩy vào ô Select
            List<Table> allTables = tableService.getAllTables().get();
            model.addAttribute("allTables", allTables != null ? allTables : new ArrayList<>());

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("menuItems", new ArrayList<>());
            model.addAttribute("allTables", new ArrayList<>());
        }
        return "cashier/take-away_order";
    }

    // ========================================================
    // API XỬ LÝ THANH TOÁN (POST) - TẠO HÓA ĐƠN ĐỘC LẬP
    // ========================================================

    public record OrderRequest(String targetTableId, List<OrderItem> items, double totalAmount) {
    }

    public record OrderItem(String id, String name, double price, int quantity) {
    }

    @PostMapping("/cashier/api/checkout")
    @ResponseBody
    public ResponseEntity<?> processCheckout(@RequestBody OrderRequest request) {
        try {
            if (request.items() == null || request.items().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Giỏ hàng trống!"));
            }

            String targetTableId = (request.targetTableId() != null && !request.targetTableId().isEmpty())
                    ? request.targetTableId()
                    : "TABLE0";
            int targetTableNumber = 0;

            // Truy vấn lấy số bàn thực tế để in lên hóa đơn
            if (!"TABLE0".equals(targetTableId) && !"0".equals(targetTableId)) {
                Table t = tableRepository.findById(targetTableId).get();
                if (t != null) {
                    targetTableNumber = t.getTableNumber();
                }
            }

            // Tạo hóa đơn
            Invoice invoice = new Invoice();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmmss");
            String invoiceId = LocalDateTime.now().format(formatter);

            invoice.setId(invoiceId);
            invoice.setTableId(targetTableId);
            invoice.setTableNumber(targetTableNumber);
            invoice.setInvoiceDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            invoice.setCreatedAt(LocalDateTime.now().toString());
            invoice.setTotalAmount(request.totalAmount());
            invoice.setSubtotal(request.totalAmount());
            invoice.setTax(0);
            invoice.setDiscount(0);
            invoice.setStatus("paid");
            invoice.setPaymentMethod("Tiền mặt");
            invoice.setNotes(targetTableNumber == 0 ? "Đơn mang về"
                    : "Tạo hóa đơn bổ sung/chữa cháy (Bàn " + targetTableNumber + ")");

            List<InvoiceItem> invoiceItems = new ArrayList<>();
            for (OrderItem reqItem : request.items()) {
                InvoiceItem item = new InvoiceItem();
                item.setItemId(reqItem.id());
                item.setItemName(reqItem.name());
                item.setUnitPrice(reqItem.price());
                item.setQuantity(reqItem.quantity());
                item.setTotal(reqItem.price() * reqItem.quantity());
                invoiceItems.add(item);
            }
            invoice.setItems(invoiceItems);

            // Lưu độc lập vào node Invoices
            invoiceService.createInvoice(invoice).get();

            // LƯU Ý: Đã gỡ bỏ toàn bộ code tác động (xóa/đổi trạng thái) đến
            // tableRepository
            // Trang Takeaway giờ đây hoàn toàn độc lập với Sơ đồ bàn!

            return ResponseEntity.ok(Map.of(
                    "message", "Thanh toán thành công!",
                    "invoiceId", invoiceId));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Lỗi khi lưu hóa đơn: " + e.getMessage()));
        }
    }
}