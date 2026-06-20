package com.example.CoffeeShop.service;

import com.example.CoffeeShop.entity.Category;
import com.example.CoffeeShop.repository.CategoryRepository;
import com.example.CoffeeShop.web.ConflictException;
import com.example.CoffeeShop.web.InternalServerException;
import com.example.CoffeeShop.web.NotFoundException;
import com.example.CoffeeShop.web.ValidationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CompletableFuture<List<Category>> getAllCategories() {
        return categoryRepository.findAll();
    }

    public CompletableFuture<Category> getCategoryById(String id) {
        return categoryRepository.findById(id);
    }

    public CompletableFuture<Void> createCategory(String id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setCreatedAt(LocalDateTime.now().toString());
        return categoryRepository.save(id, category);
    }

    public CompletableFuture<Void> updateCategory(String id, String name) {
        return categoryRepository.update(id, Map.of("name", name));
    }

    public CompletableFuture<Void> deleteCategory(String id) {
        return categoryRepository.delete(id);
    }

    public record CategoryUpsertResult(
            String id,
            String name
    ) {
    }

    public CategoryUpsertResult createCategory(String name) {
        try {
            String trimmedName = safeTrim(name);
            if (trimmedName.isEmpty()) {
                throw new ValidationException("Vui lòng nhập tên danh mục!");
            }

            List<Category> all = getAllCategories().get();
            if (all == null) all = List.of();

            if (nameExists(trimmedName, all)) {
                throw new ConflictException("Tên danh mục đã tồn tại!");
            }

            String id = nextId("CAT", all.stream().map(Category::getId).toList());
            createCategory(id, trimmedName).get();
            return new CategoryUpsertResult(id, trimmedName);
        } catch (Exception e) {
            if (e instanceof ValidationException || e instanceof ConflictException) {
                throw (RuntimeException) e;
            }
            e.printStackTrace();
            throw new InternalServerException("Không thể thêm danh mục!");
        }
    }

    public CategoryUpsertResult updateCategoryName(String id, String name) {
        try {
            String trimmedId = safeTrim(id);
            String trimmedName = safeTrim(name);
            if (trimmedId.isEmpty()) {
                throw new ValidationException("Mã danh mục không hợp lệ!");
            }
            if (trimmedName.isEmpty()) {
                throw new ValidationException("Vui lòng nhập tên danh mục!");
            }

            Category existing = getCategoryById(trimmedId).get();
            if (existing == null) {
                throw new NotFoundException("Không tìm thấy danh mục!");
            }

            List<Category> all = getAllCategories().get();
            if (all == null) all = List.of();
            for (Category c : all) {
                if (c == null) continue;
                if (c.getId() != null && c.getId().equals(trimmedId)) continue;
                if (c.getName() != null && c.getName().trim().equalsIgnoreCase(trimmedName)) {
                    throw new ConflictException("Tên danh mục đã tồn tại!");
                }
            }

            updateCategory(trimmedId, trimmedName).get();
            return new CategoryUpsertResult(trimmedId, trimmedName);
        } catch (Exception e) {
            if (e instanceof ValidationException || e instanceof NotFoundException || e instanceof ConflictException) {
                throw (RuntimeException) e;
            }
            e.printStackTrace();
            throw new InternalServerException("Không thể cập nhật danh mục!");
        }
    }

    public void deleteCategoryById(String id) {
        try {
            String trimmedId = safeTrim(id);
            if (trimmedId.isEmpty()) {
                throw new ValidationException("Mã danh mục không hợp lệ!");
            }
            Category existing = getCategoryById(trimmedId).get();
            if (existing == null) {
                throw new NotFoundException("Không tìm thấy danh mục!");
            }
            deleteCategory(trimmedId).get();
        } catch (Exception e) {
            if (e instanceof ValidationException || e instanceof NotFoundException) {
                throw (RuntimeException) e;
            }
            e.printStackTrace();
            throw new InternalServerException("Không thể xoá danh mục!");
        }
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean nameExists(String name, List<Category> categories) {
        String n = name.trim().toLowerCase(Locale.ROOT);
        for (Category c : categories) {
            if (c != null && c.getName() != null && c.getName().trim().toLowerCase(Locale.ROOT).equals(n)) {
                return true;
            }
        }
        return false;
    }

    private static String nextId(String prefix, List<String> existingIds) {
        int max = 0;
        for (String id : existingIds) {
            if (id == null || !id.startsWith(prefix)) continue;
            String numberPart = id.substring(prefix.length());
            try {
                int n = Integer.parseInt(numberPart);
                if (n > max) max = n;
            } catch (NumberFormatException ignored) {
            }
        }
        return prefix + String.format("%03d", max + 1);
    }
}
