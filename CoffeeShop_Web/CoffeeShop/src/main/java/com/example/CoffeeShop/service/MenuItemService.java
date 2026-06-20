package com.example.CoffeeShop.service;

import com.example.CoffeeShop.entity.MenuItem;
import com.example.CoffeeShop.repository.MenuItemRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class MenuItemService {
    private final MenuItemRepository menuItemRepository;

    public MenuItemService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    public CompletableFuture<List<MenuItem>> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    public CompletableFuture<MenuItem> getMenuItemById(String id) {
        return menuItemRepository.findById(id);
    }

    public CompletableFuture<Void> createMenuItem(MenuItem item) {
        item.setCreatedAt(LocalDateTime.now().toString());
        return menuItemRepository.save(item.getId(), item);
    }

    public CompletableFuture<Void> updateMenuItem(String id, MenuItem item) {
        return menuItemRepository.update(id, Map.of(
                "name", item.getName(),
                "category_id", item.getCategoryId(),
                "price", item.getPrice(),
                "image_url", item.getImageUrl(),
                "status", item.getStatus()));
    }

    public CompletableFuture<Void> deleteMenuItem(String id) {
        return menuItemRepository.delete(id);
    }
}
