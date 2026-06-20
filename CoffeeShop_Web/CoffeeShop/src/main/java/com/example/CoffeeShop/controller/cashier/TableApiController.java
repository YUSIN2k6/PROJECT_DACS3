package com.example.CoffeeShop.controller.cashier;

import com.example.CoffeeShop.entity.ActiveOrder;
import com.example.CoffeeShop.entity.Invoice;
import com.example.CoffeeShop.entity.InvoiceItem;
import com.example.CoffeeShop.entity.Table;
import com.example.CoffeeShop.repository.InvoiceRepository;
import com.example.CoffeeShop.repository.TableRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tables")
public class TableApiController {

    private final TableRepository tableRepository;
    private final InvoiceRepository invoiceRepository;

    // Tiêm cả 2 Repository để vừa dọn bàn, vừa lưu lịch sử
    public TableApiController(TableRepository tableRepository, InvoiceRepository invoiceRepository) {
        this.tableRepository = tableRepository;
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * API đồng bộ món nước pha chế (Đã sửa logic khi hết món)
     */
    @PostMapping("/{tableId}/sync-order")
    public ResponseEntity<?> syncOrder(@PathVariable String tableId, @RequestBody ActiveOrder activeOrder) {
        try {
            // Kiểm tra xem mảng items có rỗng không
            boolean hasItems = activeOrder.getItems() != null && !activeOrder.getItems().isEmpty();

            // Kiểm tra xem tất cả các món ĐÃ CÓ đã làm xong chưa
            boolean isAllServed = hasItems && activeOrder.getItems().stream().allMatch(item -> item.isServed());

            Map<String, Object> updates = new java.util.HashMap<>();
            updates.put("current_order", activeOrder);

            // LOGIC NGHIỆP VỤ MỚI CHUẨN XÁC:
            if (!hasItems) {
                // Nếu bị hủy hết sạch món -> Trở về trạng thái "Có khách" (Chờ gọi món khác)
                updates.put("status", "occupied");
            } else if (isAllServed) {
                // Nếu tất cả các món đều xong -> "Đã phục vụ"
                updates.put("status", "served");
            } else {
                // Còn món đang làm -> "Đang chuẩn bị"
                updates.put("status", "preparing");
            }

            tableRepository.update(tableId, updates).get();
            return ResponseEntity.ok(Map.of("message", "Đồng bộ trạng thái pha chế lên Firebase thành công!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Lỗi đồng bộ Firebase: " + e.getMessage()));
        }
    }

    /**
     * API THANH TOÁN (MỚI): Tạo hóa đơn, lưu lịch sử và dọn dẹp bàn
     */
    @PostMapping("/{tableId}/checkout")
    public ResponseEntity<?> checkoutTable(@PathVariable String tableId, @RequestParam String status) {
        try {
            Table table = tableRepository.findById(tableId).get();
            if (table == null)
                return ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy bàn!"));

            ActiveOrder activeOrder = table.getCurrentOrder();
            if (activeOrder != null && activeOrder.getItems() != null && !activeOrder.getItems().isEmpty()) {
                // ... (Phần code khởi tạo và lưu Invoice của bạn giữ nguyên, không thay đổi gì)
                Invoice invoice = new Invoice();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmmss");
                String invoiceId = LocalDateTime.now().format(formatter);
                invoice.setId(invoiceId);
                invoice.setTableId(tableId);
                invoice.setTableNumber(table.getTableNumber());
                invoice.setInvoiceDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                invoice.setCreatedAt(LocalDateTime.now().toString());

                double totalAmount = 0;
                List<InvoiceItem> invoiceItems = new ArrayList<>();
                for (com.example.CoffeeShop.entity.OrderItem orderItem : activeOrder.getItems()) {
                    InvoiceItem invItem = new InvoiceItem();
                    String id = orderItem.getItemId() != null ? orderItem.getItemId() : orderItem.getItemName();
                    String name = orderItem.getItemName() != null ? orderItem.getItemName() : "Sản phẩm";
                    invItem.setItemId(id);
                    invItem.setItemName(name);
                    invItem.setUnitPrice(orderItem.getPrice());
                    invItem.setQuantity(orderItem.getQuantity());
                    invItem.setTotal(orderItem.getPrice() * orderItem.getQuantity());
                    invoiceItems.add(invItem);
                    totalAmount += invItem.getTotal();
                }

                invoice.setItems(invoiceItems);
                invoice.setTotalAmount(totalAmount);
                invoice.setSubtotal(totalAmount);
                invoice.setTax(0);
                invoice.setDiscount(0);
                invoice.setStatus(status);
                invoice.setPaymentMethod("Tiền mặt");
                invoice.setNotes(status.equals("cancelled") ? "Hủy bàn" : "Thanh toán tại bàn");

                invoiceRepository.save(invoiceId, invoice).get();
            }

            // 3. ĐÃ SỬA: Reset bàn về trạng thái trống (Cần set merged_with = null)
            Map<String, Object> updates = new java.util.HashMap<>();
            updates.put("status", "available");
            updates.put("current_order", null);
            updates.put("merged_with", null); // <--- XÓA LIÊN KẾT GỘP
            tableRepository.update(tableId, updates).get();

            return ResponseEntity.ok(Map.of("message", "Thanh toán hoàn tất!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi Server: " + e.getMessage()));
        }
    }

    /**
     * API ĐỔI BÀN (NÂNG CẤP): Đồng bộ thuật toán dò tìm merged_with từ Mobile
     */
    @PostMapping("/{tableId}/switch")
    public ResponseEntity<?> switchTable(@PathVariable String tableId, @RequestParam String targetTableId) {
        try {
            Table sourceTable = tableRepository.findById(tableId).get();
            Table targetTable = tableRepository.findById(targetTableId).get();

            if (sourceTable == null || targetTable == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Bàn không tồn tại trên hệ thống!"));
            }
            if (!"available".equals(targetTable.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Bàn đích hiện không trống. Không thể đổi!"));
            }

            int oldNumber = sourceTable.getTableNumber();
            int newNumber = targetTable.getTableNumber();

            // 1. Chuyển dữ liệu sang bàn mới (Đã Sửa: Bê luôn thuộc tính merged_with sang)
            Map<String, Object> targetUpdates = new java.util.HashMap<>();
            targetUpdates.put("status", sourceTable.getStatus());
            targetUpdates.put("current_order", sourceTable.getCurrentOrder());
            targetUpdates.put("merged_with", sourceTable.getMergedWith()); // <--- MANG THEO DỮ LIỆU GỘP

            // 2. Xóa sạch dữ liệu bàn cũ
            Map<String, Object> sourceUpdates = new java.util.HashMap<>();
            sourceUpdates.put("status", "available");
            sourceUpdates.put("current_order", null);
            sourceUpdates.put("merged_with", null); // <--- LÀM TRỐNG

            tableRepository.update(targetTableId, targetUpdates).get();
            tableRepository.update(tableId, sourceUpdates).get();

            // 4. ĐÃ SỬA LẠI THUẬT TOÁN: Bắt bằng thuộc tính merged_with chứ không phải
            // chuỗi notes
            if (!"merged".equals(sourceTable.getStatus())) {
                List<Table> allTables = tableRepository.findAll().get();
                for (Table t : allTables) {
                    if ("merged".equals(t.getStatus()) && t.getMergedWith() != null && t.getMergedWith() == oldNumber) {
                        Map<String, Object> linkedTableUpdates = new java.util.HashMap<>();
                        linkedTableUpdates.put("merged_with", newNumber); // Trỏ bàn gộp sang số bàn mới
                        tableRepository.update(t.getId(), linkedTableUpdates).get();
                    }
                }
            }

            return ResponseEntity.ok(Map.of("message", "Đổi bàn thành công!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi Server: " + e.getMessage()));
        }
    }

    /**
     * API GỘP BÀN (MỚI CHUẨN): Loại bỏ hoàn toàn ActiveOrder ảo
     */
    @PostMapping("/{tableId}/merge")
    public ResponseEntity<?> mergeTable(@PathVariable String tableId, @RequestParam String targetTableId) {
        try {
            Table sourceTable = tableRepository.findById(tableId).get();
            Table targetTable = tableRepository.findById(targetTableId).get();

            if (sourceTable == null || targetTable == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Bàn không tồn tại trên hệ thống!"));
            }
            if (!"available".equals(sourceTable.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Chỉ có thể gộp từ một bàn trống!"));
            }
            if ("available".equals(targetTable.getStatus())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Bàn đích đang trống, hãy chọn bàn có khách!"));
            }

            // ĐÃ SỬA: Thay vì tạo ActiveOrder chứa note, giờ chỉ cần set trường merged_with
            // y hệt bên Mobile
            Map<String, Object> sourceUpdates = new java.util.HashMap<>();
            sourceUpdates.put("status", "merged");
            sourceUpdates.put("merged_with", targetTable.getTableNumber()); // Lưu dạng số nguyên Int

            tableRepository.update(tableId, sourceUpdates).get();
            return ResponseEntity.ok(Map.of("message", "Gộp bàn thành công!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi Server: " + e.getMessage()));
        }
    }
}