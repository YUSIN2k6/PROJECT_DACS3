package com.example.CoffeeShop.repository;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Base class để giảm boilerplate Firebase code
 * Cung cấp helper methods chung cho tất cả Repository classes
 */
public abstract class FirebaseRepositoryBase {

    /**
     * Lắng nghe một giá trị từ Firebase - chỉ lấy 1 lần
     * 
     * @param ref    DatabaseReference để lắng nghe
     * @param mapper Function để chuyển DataSnapshot thành object kiểu T
     * @return CompletableFuture<T>
     */
    protected <T> CompletableFuture<T> listenForSingleValue(DatabaseReference ref, Function<DataSnapshot, T> mapper) {
        CompletableFuture<T> future = new CompletableFuture<>();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    T result = mapper.apply(snapshot);
                    future.complete(result);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });
        return future;
    }

    /**
     * Lắng nghe một query từ Firebase
     * @param query Query để lắng nghe
     * @param mapper Function để chuyển DataSnapshot thành object kiểu T
     * @return CompletableFuture<T>
     */
    protected <T> CompletableFuture<T> listenForQuery(com.google.firebase.database.Query query, Function<DataSnapshot, T> mapper) {
        CompletableFuture<T> future = new CompletableFuture<>();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    T result = mapper.apply(snapshot);
                    future.complete(result);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });
        return future;
    }

    /**
     * Ghi dữ liệu vào Firebase
     * 
     * @param ref   DatabaseReference để ghi
     * @param value Object cần ghi
     * @return CompletableFuture<Void>
     */
    protected CompletableFuture<Void> writeValue(DatabaseReference ref, Object value) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        ref.setValue(value, (error, fbRef) -> {
            if (error != null) {
                future.completeExceptionally(error.toException());
            } else {
                future.complete(null);
            }
        });
        return future;
    }

    /**
     * Cập nhật các trường cụ thể
     * 
     * @param ref     DatabaseReference để cập nhật
     * @param updates Map các trường cần cập nhật
     * @return CompletableFuture<Void>
     */
    protected CompletableFuture<Void> updateValues(DatabaseReference ref, java.util.Map<String, Object> updates) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        ref.updateChildren(updates, (error, fbRef) -> {
            if (error != null) {
                future.completeExceptionally(error.toException());
            } else {
                future.complete(null);
            }
        });
        return future;
    }

    /**
     * Xóa dữ liệu từ Firebase
     * 
     * @param ref DatabaseReference để xóa
     * @return CompletableFuture<Void>
     */
    protected CompletableFuture<Void> removeValue(DatabaseReference ref) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        ref.removeValue((error, fbRef) -> {
            if (error != null) {
                future.completeExceptionally(error.toException());
            } else {
                future.complete(null);
            }
        });
        return future;
    }
}
