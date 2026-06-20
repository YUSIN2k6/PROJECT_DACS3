package com.example.CoffeeShop.service;

import com.example.CoffeeShop.entity.Invoice;
import com.example.CoffeeShop.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public CompletableFuture<List<Invoice>> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public CompletableFuture<Invoice> getInvoiceById(String id) {
        return invoiceRepository.findById(id);
    }

    public CompletableFuture<Void> createInvoice(Invoice invoice) {
        invoice.setCreatedAt(LocalDateTime.now().toString());
        if (invoice.getInvoiceDate() == null) {
            invoice.setInvoiceDate(LocalDateTime.now().toString());
        }
        return invoiceRepository.save(invoice.getId(), invoice);
    }

    public CompletableFuture<Void> updateInvoiceStatus(String id, String status) {
        return invoiceRepository.update(id, Map.of("status", status));
    }

    public CompletableFuture<Void> updateInvoice(String id, Invoice invoice) {
        return invoiceRepository.update(id, Map.of(
                "table_id", invoice.getTableId(),
                "items", invoice.getItems(),
                "subtotal", invoice.getSubtotal(),
                "tax", invoice.getTax(),
                "discount", invoice.getDiscount(),
                "total_amount", invoice.getTotalAmount(),
                "payment_method", invoice.getPaymentMethod(),
                "status", invoice.getStatus(),
                "notes", invoice.getNotes()));
    }

    public CompletableFuture<Void> deleteInvoice(String id) {
        return invoiceRepository.delete(id);
    }

    public CompletableFuture<List<Invoice>> getInvoicesByStatus(String status) {
        return invoiceRepository.findAll()
                .thenApply(invoices -> invoices.stream()
                        .filter(inv -> inv.getStatus().equals(status))
                        .toList());
    }
}
