package com.example.CoffeeShop.service;

import com.example.CoffeeShop.entity.User;
import com.example.CoffeeShop.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public CompletableFuture<List<User>> getAllUsers() {
        return userRepository.findAll();
    }

    public CompletableFuture<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public CompletableFuture<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public CompletableFuture<User> getUserByStaffId(String staffId) {
        return userRepository.findByStaffId(staffId);
    }

    public CompletableFuture<Void> createUser(User user) {
        user.setCreatedAt(LocalDateTime.now().toString());
        user.setStatus("active");
        return userRepository.save(user.getId(), user);
    }

    public CompletableFuture<Void> updateUser(String id, User user) {
        return userRepository.update(id, Map.of(
                "username", user.getUsername(),
                "email", user.getEmail(),
                "role", user.getRole(),
                "status", user.getStatus(),
                "last_login", user.getLastLogin()));
    }

    public CompletableFuture<Void> updateUserFields(String id, Map<String, Object> updates) {
        return userRepository.update(id, updates);
    }

    public CompletableFuture<Void> updateLastLogin(String id) {
        return userRepository.update(id, Map.of("last_login", LocalDateTime.now().toString()));
    }

    public CompletableFuture<Void> deleteUser(String id) {
        return userRepository.delete(id);
    }

    public CompletableFuture<Boolean> authenticate(String username, String passwordHash) {
        return userRepository.findByUsername(username)
                .thenApply(user -> user != null && user.getPasswordHash().equals(passwordHash));
    }
}
