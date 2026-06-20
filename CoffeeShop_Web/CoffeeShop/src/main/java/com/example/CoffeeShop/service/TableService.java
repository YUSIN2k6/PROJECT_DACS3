package com.example.CoffeeShop.service;

import com.example.CoffeeShop.entity.Table;
import com.example.CoffeeShop.repository.TableRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class TableService {
    private final TableRepository tableRepository;

    public TableService(TableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }

    public CompletableFuture<List<Table>> getAllTables() {
        return tableRepository.findAll();
    }

    public CompletableFuture<Table> getTableById(String id) {
        return tableRepository.findById(id);
    }

    public CompletableFuture<Void> createTable(String id, int tableNumber) {
        Table table = new Table();
        table.setId(id);
        table.setTableNumber(tableNumber);
        table.setStatus("available");
        table.setCreatedAt(LocalDateTime.now().toString());
        return tableRepository.save(id, table);
    }

    public CompletableFuture<Void> updateTableStatus(String id, String status) {
        return tableRepository.update(id, Map.of("status", status));
    }

    public CompletableFuture<Void> deleteTable(String id) {
        return tableRepository.delete(id);
    }

    public CompletableFuture<List<Table>> getTablesByStatus(String status) {
        return tableRepository.findByStatus(status);
    }
}
