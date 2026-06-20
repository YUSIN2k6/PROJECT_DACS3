package com.example.CoffeeShop.repository;

import com.example.CoffeeShop.entity.Category;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Repository
public class CategoryRepository extends FirebaseRepositoryBase {
    private final DatabaseReference database;
    private static final String PATH = "categories";

    public CategoryRepository() {
        this.database = FirebaseDatabase.getInstance().getReference(PATH);
    }

    public CompletableFuture<List<Category>> findAll() {
        return listenForSingleValue(database, snapshot -> {
            List<Category> categories = new ArrayList<>();
            for (var child : snapshot.getChildren()) {
                Category category = child.getValue(Category.class);
                if (category != null) {
                    category.setId(child.getKey());
                    categories.add(category);
                }
            }
            return categories;
        });
    }

    public CompletableFuture<Category> findById(String id) {
        return listenForSingleValue(database.child(id), snapshot -> {
            Category category = snapshot.getValue(Category.class);
            if (category != null) {
                category.setId(snapshot.getKey());
            }
            return category;
        });
    }

    public CompletableFuture<Void> save(String id, Category category) {
        return writeValue(database.child(id), category);
    }

    public CompletableFuture<Void> update(String id, Map<String, Object> updates) {
        return updateValues(database.child(id), updates);
    }

    public CompletableFuture<Void> delete(String id) {
        return removeValue(database.child(id));
    }
}
