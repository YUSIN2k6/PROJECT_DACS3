package com.example.CoffeeShop.repository;

import com.example.CoffeeShop.entity.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Repository
public class UserRepository extends FirebaseRepositoryBase {
    private final DatabaseReference database;
    private static final String PATH = "users";

    public UserRepository() {
        this.database = FirebaseDatabase.getInstance().getReference(PATH);
    }

    public CompletableFuture<List<User>> findAll() {
        return listenForSingleValue(database, snapshot -> {
            List<User> users = new ArrayList<>();
            for (var child : snapshot.getChildren()) {
                User user = child.getValue(User.class);
                if (user != null) {
                    user.setId(child.getKey());
                    users.add(user);
                }
            }
            return users;
        });
    }

    public CompletableFuture<User> findById(String id) {
        return listenForSingleValue(database.child(id), snapshot -> {
            User user = snapshot.getValue(User.class);
            if (user != null) {
                user.setId(snapshot.getKey());
            }
            return user;
        });
    }

    public CompletableFuture<User> findByUsername(String username) {
        return listenForQuery(database.orderByChild("username").equalTo(username), snapshot -> {
            for (var child : snapshot.getChildren()) {
                User user = child.getValue(User.class);
                if (user != null) {
                    user.setId(child.getKey());
                    return user;
                }
            }
            return null;
        });
    }

    public CompletableFuture<User> findByStaffId(String staffId) {
        return listenForQuery(database.orderByChild("staff_id").equalTo(staffId), snapshot -> {
            for (var child : snapshot.getChildren()) {
                User user = child.getValue(User.class);
                if (user != null) {
                    user.setId(child.getKey());
                    return user;
                }
            }
            return null;
        });
    }

    public CompletableFuture<Void> save(String id, User user) {
        return writeValue(database.child(id), user);
    }

    public CompletableFuture<Void> update(String id, Map<String, Object> updates) {
        return updateValues(database.child(id), updates);
    }

    public CompletableFuture<Void> delete(String id) {
        return removeValue(database.child(id));
    }
}
