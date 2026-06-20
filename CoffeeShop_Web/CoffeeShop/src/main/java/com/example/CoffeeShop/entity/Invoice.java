package com.example.CoffeeShop.entity;

import com.google.firebase.database.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
    private String id;
    private double subtotal;
    private double tax;
    private double discount;
    private String status;
    private String notes;

    // Xoá các @PropertyName trên đầu biến đi
    private String tableId;
    private int tableNumber;
    private String invoiceDate;
    private double totalAmount;
    private String paymentMethod;
    private String staffId;

    @PropertyName("createdAt")
    private String createdAt;

    // Danh sách các món trong hóa đơn
    private List<InvoiceItem> items;

    // ========================================================
    // BỔ SUNG GETTER / SETTER THỦ CÔNG CHO CÁC TRƯỜNG CÓ DẤU "_"
    // ========================================================

    @PropertyName("table_id")
    public String getTableId() {
        return tableId;
    }

    @PropertyName("table_id")
    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    @PropertyName("table_number")
    public int getTableNumber() {
        return tableNumber;
    }

    @PropertyName("table_number")
    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    @PropertyName("invoice_date")
    public String getInvoiceDate() {
        return invoiceDate;
    }

    @PropertyName("invoice_date")
    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    @PropertyName("total_amount")
    public double getTotalAmount() {
        return totalAmount;
    }

    @PropertyName("total_amount")
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    @PropertyName("payment_method")
    public String getPaymentMethod() {
        return paymentMethod;
    }

    @PropertyName("payment_method")
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @PropertyName("staff_id")
    public String getStaffId() {
        return staffId;
    }

    @PropertyName("staff_id")
    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }
}