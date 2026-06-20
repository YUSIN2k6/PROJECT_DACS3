package com.example.coffeeshopmobile.data.model

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName

@IgnoreExtraProperties // <-- Lá chắn bảo vệ App không bị văng khi Firebase có dữ liệu lạ
data class Table(
    var id: String = "",

    @get:PropertyName("table_number")
    @set:PropertyName("table_number")
    var tableNumber: Int = 0,

    var status: String = "available",

    @get:PropertyName("merged_with")
    @set:PropertyName("merged_with")
    var mergedWith: Int? = null,

    var createdAt: String? = null,

    @get:PropertyName("current_order")
    @set:PropertyName("current_order")
    var currentOrder: ActiveOrder? = null
) {
    // ==========================================
    // CÁC HÀM PHỤ TRỢ (Giúp code UI cực kỳ nhàn)
    // ==========================================

    /**
     * Tự động cộng dồn tổng tiền của các món trong bàn
     */
    fun getTotalPrice(): Double {
        // Nếu currentOrder null thì trả về 0.0, nếu có thì tính tổng (giá * số lượng)
        return currentOrder?.items?.sumOf { it.price * it.quantity } ?: 0.0
    }

    /**
     * Lấy tổng số lượng ly nước khách đang gọi
     */
    fun getTotalItemCount(): Int {
        return currentOrder?.items?.sumOf { it.quantity } ?: 0
    }
}