package com.example.CoffeeShop.service;

import com.example.CoffeeShop.entity.Staff;
import com.example.CoffeeShop.repository.StaffRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class StaffService {
    private final StaffRepository staffRepository;

    public StaffService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    public CompletableFuture<List<Staff>> getAllStaff() {
        return staffRepository.findAll();
    }

    public CompletableFuture<Staff> getStaffById(String id) {
        return staffRepository.findById(id);
    }

    public CompletableFuture<Void> createStaff(Staff staff) {
        staff.setCreatedAt(LocalDateTime.now().toString());
        return staffRepository.save(staff.getId(), staff);
    }

    public CompletableFuture<Void> updateStaff(String id, Staff staff) {
        return staffRepository.update(id, Map.of(
                "name", staff.getName(),
                "email", staff.getEmail(),
                "phone", staff.getPhone(),
                "position", staff.getPosition(),
                "status", staff.getStatus()));
    }

    public CompletableFuture<Void> deleteStaff(String id) {
        return staffRepository.delete(id);
    }
}
