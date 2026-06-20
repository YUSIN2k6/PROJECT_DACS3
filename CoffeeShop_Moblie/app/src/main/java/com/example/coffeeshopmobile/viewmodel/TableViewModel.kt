package com.example.coffeeshopmobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeeshopmobile.data.model.Table
import com.example.coffeeshopmobile.data.repository.TableRepository
import com.example.coffeeshopmobile.data.repository.MenuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import com.example.coffeeshopmobile.data.model.MenuItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TableViewModel : ViewModel() {
    private val repository = TableRepository()
    private val menuRepository = MenuRepository()

    private val _tables = MutableStateFlow<List<Table>>(emptyList())
    val tables: StateFlow<List<Table>> = _tables.asStateFlow()

    private val _menu = MutableStateFlow<List<MenuItem>>(emptyList())
    val menu: StateFlow<List<MenuItem>> = _menu.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchTables()
        fetchMenu()
    }

    private fun fetchTables() {
        viewModelScope.launch {
            repository.getTables()
                .catch { _isLoading.value = false }
                .collect { tableList ->
                    _tables.value = tableList
                    _isLoading.value = false
                }
        }
    }

    private fun fetchMenu() {
        viewModelScope.launch {
            menuRepository.getMenu()
                .catch { e -> e.printStackTrace() }
                .collect { menuList -> _menu.value = menuList }
        }
    }

    fun updateTableStatus(tableId: String, newStatus: String) {
        viewModelScope.launch { repository.updateTableStatus(tableId, newStatus) }
    }

    fun mergeTable(currentTableId: String, targetTableNumber: Int) {
        viewModelScope.launch { repository.mergeTable(currentTableId, targetTableNumber) }
    }

    fun unmergeTable(tableId: String) {
        viewModelScope.launch { repository.unmergeTable(tableId) }
    }

    fun unmergeAllFromTarget(targetTableNumber: Int) {
        viewModelScope.launch {
            val idsToUnmerge = _tables.value
                .filter { it.status == "merged" && it.mergedWith == targetTableNumber }
                .map { it.id }
            repository.unmergeAllFromTarget(idsToUnmerge)
        }
    }

    fun transferTable(originTable: Table, targetTable: Table, allTables: List<Table>) {
        viewModelScope.launch { repository.transferTable(originTable, targetTable, allTables) }
    }

    fun addTableNote(tableId: String, newNoteText: String, oldNotes: String) {
        if (newNoteText.isBlank()) return
        viewModelScope.launch {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val currentTime = sdf.format(Date())
            val formattedNewNote = "($currentTime) $newNoteText"
            val updatedNotes = if (oldNotes.isBlank()) formattedNewNote else "$formattedNewNote\n$oldNotes"
            repository.updateTableNotes(tableId, updatedNotes)
        }
    }

    fun clearTableNotes(tableId: String) {
        viewModelScope.launch { repository.updateTableNotes(tableId, "") }
    }

    fun addMenuItemToCart(table: Table, menuItem: MenuItem) {
        val currentOrder = table.currentOrder ?: com.example.coffeeshopmobile.data.model.ActiveOrder()
        val items = currentOrder.items.toMutableList()

        // SỬA ĐIỀU KIỆN: Chỉ tương tác với món THỰC SỰ ĐANG TRONG GIỎ (chưa bắn xuống bếp)
        val existingItemIndex = items.indexOfFirst { it.itemId == menuItem.id && !it.sentToKitchen }

        if (existingItemIndex != -1) {
            val existing = items[existingItemIndex]
            items[existingItemIndex] = existing.copy(quantity = existing.quantity + 1)
        } else {
            items.add(
                com.example.coffeeshopmobile.data.model.OrderItem(
                    itemId = menuItem.id,
                    itemName = menuItem.name,
                    price = menuItem.price,
                    quantity = 1,
                    served = false,
                    sentToKitchen = false // Món mới nhặt vào giỏ
                )
            )
        }

        val updatedOrder = currentOrder.copy(items = items)
        viewModelScope.launch { repository.updateTableOrder(table.id, updatedOrder) }
    }

    fun updateCartItemQuantity(table: Table, itemId: String, delta: Int) {
        val currentOrder = table.currentOrder ?: return
        val items = currentOrder.items.toMutableList()

        // SỬA ĐIỀU KIỆN: Tìm món trùng ID đang nằm trong giỏ
        val index = items.indexOfFirst { it.itemId == itemId && !it.sentToKitchen }
        if (index != -1) {
            val existing = items[index]
            val newQty = existing.quantity + delta
            if (newQty <= 0) {
                items.removeAt(index)
            } else {
                items[index] = existing.copy(quantity = newQty)
            }
            viewModelScope.launch { repository.updateTableOrder(table.id, currentOrder.copy(items = items)) }
        }
    }

    /**
     * GỬI ĐƠN XUỐNG PHA CHẾ VÀ LÀM TRỐNG GIỎ HÀNG TẠM
     */
    fun sendOrderToKitchen(table: Table) {
        val currentOrder = table.currentOrder ?: return
        val items = currentOrder.items.toMutableList()

        // ĐÈ CỜ: Tìm tất cả những món đang nằm trong giỏ và lật sang trạng thái đã gửi bếp
        var hasChanges = false
        for (i in items.indices) {
            if (!items[i].sentToKitchen) {
                items[i] = items[i].copy(sentToKitchen = true)
                hasChanges = true
            }
        }

        if (hasChanges) {
            viewModelScope.launch {
                // 1. Chuyển trạng thái bàn sang "preparing" cho Web nháy đèn
                repository.updateTableStatus(table.id, "preparing")
                // 2. Đẩy danh sách đơn hàng đã lật cờ lên Firebase
                repository.updateTableOrder(table.id, currentOrder.copy(items = items))
            }
        }
    }
}