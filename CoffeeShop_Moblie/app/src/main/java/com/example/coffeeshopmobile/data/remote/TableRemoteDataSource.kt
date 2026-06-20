package com.example.coffeeshopmobile.data.remote

import com.example.coffeeshopmobile.data.model.Table
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TableRemoteDataSource {
    // Trỏ vào nhánh "tables" trên Firebase
    private val databaseRef = FirebaseDatabase.getInstance().getReference("tables")

    /**
     * Lắng nghe Realtime danh sách bàn. Trả về luồng Flow liên tục.
     */
    fun listenToTables(): Flow<List<Table>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tables = mutableListOf<Table>()
                for (child in snapshot.children) {
                    val table = child.getValue(Table::class.java)
                    if (table != null) {
                        table.id = child.key ?: "" // Gắn key (Vd: TABLE001) vào biến id
                        tables.add(table)
                    }
                }
                // Gửi mảng bàn vào luồng
                trySend(tables).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        databaseRef.addValueEventListener(listener)
        // Khi ViewModel bị hủy, tự đóng luồng lắng nghe cho nhẹ máy
        awaitClose { databaseRef.removeEventListener(listener) }
    }

    /**
     * Hàm cập nhật trạng thái của bàn lên Firebase (Đã nâng cấp xóa dữ liệu khi hủy khách)
     */
    suspend fun updateTableStatus(tableId: String, newStatus: String): Boolean {
        return try {
            val updates = mutableMapOf<String, Any?>()
            updates["status"] = newStatus

            // Nếu hủy khách đưa bàn về trống -> Xóa sạch đơn hàng và lịch sử ghi chú cũ của bàn đó luôn
            if (newStatus == "available") {
                updates["current_order"] = null
            }

            databaseRef.child(tableId).updateChildren(updates).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Hàm xử lý gộp bàn lên Firebase
     */
    suspend fun mergeTable(currentTableId: String, targetTableNumber: Int): Boolean {
        return try {
            val updates = mapOf(
                "status" to "merged",
                "merged_with" to targetTableNumber
            )
            // Cập nhật đồng thời cả 2 trường của bàn hiện tại
            databaseRef.child(currentTableId).updateChildren(updates).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Rời gộp cho riêng 1 bàn phụ (Đưa về trạng thái trống available)
     */
    suspend fun unmergeTable(tableId: String): Boolean {
        return try {
            val updates = mapOf(
                "status" to "available",
                "merged_with" to null
            )
            databaseRef.child(tableId).updateChildren(updates).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Rời gộp tất cả các bàn phụ đang chỉ vào bàn chính này
     */
    suspend fun unmergeAllFromTarget(tablesToUnmerge: List<String>): Boolean {
        return try {
            val updates = mutableMapOf<String, Any?>()
            for (id in tablesToUnmerge) {
                updates["$id/status"] = "available"
                updates["$id/merged_with"] = null
            }
            if (updates.isNotEmpty()) {
                databaseRef.updateChildren(updates).await()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Hàm di chuyển toàn bộ cấu trúc dữ liệu từ bàn gốc sang bàn đích
     */
    suspend fun transferTable(originTable: Table, targetTable: Table, allTables: List<Table>): Boolean {
        return try {
            val updates = mutableMapOf<String, Any?>()

            // 1. Giải phóng hoàn toàn bàn gốc về trạng thái trống
            updates["${originTable.id}/status"] = "available"
            updates["${originTable.id}/current_order"] = null
            updates["${originTable.id}/merged_with"] = null

            // 2. Sao chép toàn bộ "ruột" dữ liệu sang bàn đích mới
            updates["${targetTable.id}/status"] = originTable.status
            updates["${targetTable.id}/current_order"] = originTable.currentOrder
            updates["${targetTable.id}/merged_with"] = originTable.mergedWith

            // 3. BẢO VỆ ĐỒNG BỘ GỘP: Nếu bàn gốc là bàn chính có các bàn khác đang gộp vào,
            // ta phải dò tìm và đổi merged_with của các bàn phụ đó sang số bàn mới.
            val subTables = allTables.filter { it.status == "merged" && it.mergedWith == originTable.tableNumber }
            for (sub in subTables) {
                updates["${sub.id}/merged_with"] = targetTable.tableNumber
            }

            // Đẩy một lệnh duy nhất lên mạng để cập nhật đồng loạt các nhánh
            databaseRef.updateChildren(updates).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Cập nhật chuỗi ghi chú tổng hợp vào cục current_order trên Firebase
     */
    suspend fun updateTableNotes(tableId: String, notes: String): Boolean {
        return try {
            databaseRef.child(tableId).child("current_order").child("notes").setValue(notes).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Cập nhật toàn bộ giỏ hàng (ActiveOrder) lên Firebase
     */
    suspend fun updateTableOrder(tableId: String, order: com.example.coffeeshopmobile.data.model.ActiveOrder?): Boolean {
        return try {
            databaseRef.child(tableId).child("current_order").setValue(order).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}