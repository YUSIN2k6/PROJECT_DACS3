package com.example.coffeeshopmobile.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coffeeshopmobile.ui.screens.*
import com.example.coffeeshopmobile.viewmodel.TableViewModel


@Composable
fun AppNavigation() {
    // 1. Kiểm tra trạng thái đăng nhập từ thiết bị
    var isUserLoggedIn by remember { mutableStateOf(false) }

    if (!isUserLoggedIn) {
        // --- CHƯA ĐĂNG NHẬP: Hiển thị màn hình Login ---
        LoginScreen(
            onLoginSuccess = { isUserLoggedIn = true }
        )
    } else {
        // --- ĐÃ ĐĂNG NHẬP: Hiển thị hệ thống quán cà phê ---
        val viewModel: TableViewModel = viewModel()
        val tables by viewModel.tables.collectAsState()

        // Các cờ trạng thái chuyển màn hình
        var selectedTableId by remember { mutableStateOf<String?>(null) }
        var isSelectingMerge by remember { mutableStateOf(false) }
        var isSelectingTransfer by remember { mutableStateOf(false) }
        var isEditingNotes by remember { mutableStateOf(false) }
        var isSelectingMenu by remember { mutableStateOf(false) }
        var isViewingCart by remember { mutableStateOf(false) }
        var isViewingServedOrders by remember { mutableStateOf(false) }

        val currentSelectedTable = tables.find { it.id == selectedTableId }

        when {
            // MÀN HÌNH 1: Sơ đồ bàn ngoài cùng
            currentSelectedTable == null -> {
                TableScreen(
                    viewModel = viewModel,
                    onTableClick = { table -> selectedTableId = table.id }
                )
            }
            // MÀN HÌNH 3: Chọn bàn để Gộp
            isSelectingMerge -> {
                MergeTableScreen(
                    currentTable = currentSelectedTable,
                    viewModel = viewModel,
                    onBackClick = { isSelectingMerge = false },
                    onMergeSuccess = {
                        isSelectingMerge = false
                        selectedTableId = null
                    }
                )
            }
            // MÀN HÌNH 4: Giao diện Chuyển bàn
            isSelectingTransfer -> {
                TransferTableScreen(
                    currentTable = currentSelectedTable,
                    viewModel = viewModel,
                    onBackClick = { isSelectingTransfer = false },
                    onTransferSuccess = {
                        isSelectingTransfer = false
                        selectedTableId = null
                    }
                )
            }
            // MÀN HÌNH 5: Ghi chú
            isEditingNotes -> {
                TableNoteScreen(
                    table = currentSelectedTable,
                    viewModel = viewModel,
                    onBackClick = { isEditingNotes = false }
                )
            }
            // MÀN HÌNH 7: Giỏ hàng
            isViewingCart -> {
                CartScreen(
                    table = currentSelectedTable,
                    viewModel = viewModel,
                    onBackClick = { isViewingCart = false },
                    onNoteClick = { isEditingNotes = true },
                    onServeSuccess = {
                        isViewingCart = false
                        isSelectingMenu = false
                    }
                )
            }
            // MÀN HÌNH 6: Chọn món
            isSelectingMenu -> {
                MenuScreen(
                    table = currentSelectedTable,
                    viewModel = viewModel,
                    onBackClick = { isSelectingMenu = false },
                    onCartClick = { isViewingCart = true }
                )
            }
            // MÀN HÌNH 8: Món đã phục vụ
            isViewingServedOrders -> {
                ServedOrderScreen(
                    table = currentSelectedTable,
                    onBackClick = { isViewingServedOrders = false }
                )
            }
            // MÀN HÌNH 2: Chức năng chi tiết tại bàn
            else -> {
                TableDetailScreen(
                    table = currentSelectedTable,
                    allTables = tables,
                    viewModel = viewModel,
                    onBackClick = { selectedTableId = null },
                    onMergeClick = { isSelectingMerge = true },
                    onTransferClick = { isSelectingTransfer = true },
                    onNoteClick = { isEditingNotes = true },
                    onMenuClick = { isSelectingMenu = true },
                    onServedHistoryClick = { isViewingServedOrders = true }
                )
            }
        }
    }
}