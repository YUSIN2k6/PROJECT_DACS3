package com.example.CoffeeShop.repository;

import com.example.CoffeeShop.entity.Invoice;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Repository
public class InvoiceRepository extends FirebaseRepositoryBase {
    private final DatabaseReference database;
    private static final String PATH = "invoices";

    public InvoiceRepository() {
        this.database = FirebaseDatabase.getInstance().getReference(PATH);
    }

    public CompletableFuture<List<Invoice>> findAll() {
        return listenForSingleValue(database, snapshot -> {
            List<Invoice> invoices = new ArrayList<>();
            for (var child : snapshot.getChildren()) {
                Invoice invoice = child.getValue(Invoice.class);
                if (invoice != null) {
                    invoice.setId(child.getKey());
                    invoices.add(invoice);
                }
            }
            return invoices;
        });
    }

    public CompletableFuture<Invoice> findById(String id) {
        return listenForSingleValue(database.child(id), snapshot -> {
            Invoice invoice = snapshot.getValue(Invoice.class);
            if (invoice != null) {
                invoice.setId(snapshot.getKey());
            }
            return invoice;
        });
    }

    public CompletableFuture<Void> save(String id, Invoice invoice) {
        return writeValue(database.child(id), invoice);
    }

    public CompletableFuture<Void> update(String id, Map<String, Object> updates) {
        return updateValues(database.child(id), updates);
    }

    public CompletableFuture<Void> delete(String id) {
        return removeValue(database.child(id));
    }
}
