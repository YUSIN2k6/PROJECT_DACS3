package com.example.CoffeeShop.repository;

import com.example.CoffeeShop.entity.Table;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Repository
public class TableRepository extends FirebaseRepositoryBase {
    private final DatabaseReference database;
    private static final String PATH = "tables";

    public TableRepository() {
        this.database = FirebaseDatabase.getInstance().getReference(PATH);
    }

    public CompletableFuture<List<Table>> findAll() {
        return listenForSingleValue(database, snapshot -> {
            List<Table> tables = new ArrayList<>();
            for (var child : snapshot.getChildren()) {
                Table table = child.getValue(Table.class);
                if (table != null) {
                    table.setId(child.getKey());
                    tables.add(table);
                }
            }
            return tables;
        });
    }

    public CompletableFuture<Table> findById(String id) {
        return listenForSingleValue(database.child(id), snapshot -> {
            Table table = snapshot.getValue(Table.class);
            if (table != null) {
                table.setId(snapshot.getKey());
            }
            return table;
        });
    }

    public CompletableFuture<Void> save(String id, Table table) {
        return writeValue(database.child(id), table);
    }

    public CompletableFuture<Void> update(String id, Map<String, Object> updates) {
        return updateValues(database.child(id), updates);
    }

    public CompletableFuture<Void> delete(String id) {
        return removeValue(database.child(id));
    }

    public CompletableFuture<List<Table>> findByStatus(String status) {
        return listenForQuery(database.orderByChild("status").equalTo(status), snapshot -> {
            List<Table> tables = new ArrayList<>();
            for (var child : snapshot.getChildren()) {
                Table table = child.getValue(Table.class);
                if (table != null) {
                    table.setId(child.getKey());
                    tables.add(table);
                }
            }
            return tables;
        });
    }
}
