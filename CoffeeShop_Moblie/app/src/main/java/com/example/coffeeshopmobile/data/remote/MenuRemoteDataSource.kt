package com.example.coffeeshopmobile.data.remote

import com.example.coffeeshopmobile.data.model.MenuItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MenuRemoteDataSource {
    /**
     * Lắng nghe Realtime danh sách thực đơn món nước từ nhánh "menu_items" trên Firebase
     */
    fun listenToMenu(): Flow<List<MenuItem>> = callbackFlow {
        val menuRef = FirebaseDatabase.getInstance().getReference("menu_items")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val menuItems = mutableListOf<MenuItem>()
                for (child in snapshot.children) {
                    val item = child.getValue(MenuItem::class.java)
                    if (item != null) {
                        if (item.status == "available") {
                            item.id = child.key ?: ""
                            menuItems.add(item)
                        }
                    }
                }
                trySend(menuItems).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        menuRef.addValueEventListener(listener)
        awaitClose { menuRef.removeEventListener(listener) }
    }
}