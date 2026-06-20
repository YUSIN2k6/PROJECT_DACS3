package com.example.CoffeeShop.repository;

import com.example.CoffeeShop.entity.Staff;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Repository
public class StaffRepository extends FirebaseRepositoryBase {
    private final DatabaseReference database;
    private static final String PATH = "staff";

    public StaffRepository() {
        this.database = FirebaseDatabase.getInstance().getReference(PATH);
    }

    public CompletableFuture<List<Staff>> findAll() {
        return listenForSingleValue(database, snapshot -> {
            List<Staff> staffList = new ArrayList<>();
            for (var child : snapshot.getChildren()) {
                Staff staff = child.getValue(Staff.class);
                if (staff != null) {
                    staff.setId(child.getKey());
                    staffList.add(staff);
                }
            }
            return staffList;
        });
    }

    public CompletableFuture<Staff> findById(String id) {
        return listenForSingleValue(database.child(id), snapshot -> {
            Staff staff = snapshot.getValue(Staff.class);
            if (staff != null) {
                staff.setId(snapshot.getKey());
            }
            return staff;
        });
    }

    public CompletableFuture<Void> save(String id, Staff staff) {
        return writeValue(database.child(id), staff);
    }

    public CompletableFuture<Void> update(String id, Map<String, Object> updates) {
        return updateValues(database.child(id), updates);
    }

    public CompletableFuture<Void> delete(String id) {
        return removeValue(database.child(id));
    }
}
