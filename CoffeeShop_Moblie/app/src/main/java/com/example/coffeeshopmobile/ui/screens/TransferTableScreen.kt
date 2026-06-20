package com.example.coffeeshopmobile.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coffeeshopmobile.data.model.Table
import com.example.coffeeshopmobile.viewmodel.TableViewModel

@Composable
fun TransferTableScreen(
    currentTable: Table,
    onBackClick: () -> Unit,
    onTransferSuccess: () -> Unit,
    viewModel: TableViewModel = viewModel()
) {
    val tables by viewModel.tables.collectAsState()

    // Biến lưu ID của bàn được chọn làm bàn gốc dời đi (Nút sẽ chuyển sang Xanh nước biển)
    var selectedOriginTableId by remember { mutableStateOf<String?>(null) }

    // Thuật toán tìm số hiệu bàn chính của cụm gộp hiện tại
    val currentGroupMainNumber = if (currentTable.status == "merged") currentTable.mergedWith ?: 0 else currentTable.tableNumber

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
    ) {
        // --- HEADING ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Trở về",
                    modifier = Modifier.size(32.dp),
                    tint = Color.Black
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Chuyển bàn",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
        }

        // --- DANH SÁCH 2 CỘT SONG SONG ---
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(tables) { table ->
                // Kiểm tra xem bàn này có thuộc cụm gộp/bàn hiện tại không
                val isInGroup = table.tableNumber == currentGroupMainNumber || (table.status == "merged" && table.mergedWith == currentGroupMainNumber)
                val isSelectedOrigin = selectedOriginTableId == table.id

                // Quyết định màu sắc cho Nút Đổi (Cột 2) theo đúng yêu cầu
                val changeButtonColor = when {
                    isSelectedOrigin -> Color(0xFF03A9F4) // Được chọn dời đi: Xanh nước biển
                    isInGroup -> Color(0xFFFF9800)        // Thuộc cụm hiện tại: Màu Cam
                    else -> Color(0xFFE0E0E0)              // Các bàn khác: Màu Xám nền
                }

                // Quyết định màu sắc nền hiển thị thông tin bàn (Cột 1 - Đồng bộ trang gộp)
                val targetTableOfMerged = if (table.status == "merged") tables.find { it.tableNumber == table.mergedWith } else null
                val effectiveStatus = if (table.status == "merged") targetTableOfMerged?.status ?: "available" else table.status
                val tableBackgroundColor = when (effectiveStatus) {
                    "occupied" -> Color(0xFF81C784) // Có khách: Xanh lá
                    else -> Color(0xFFE0E0E0)       // Trống: Xám
                }

                // Chữ hiển thị tên bàn
                val tableText = when (table.status) {
                    "merged" -> "BÀN ${table.tableNumber} đang gộp ${table.mergedWith}"
                    else -> "BÀN ${table.tableNumber}"
                }

                // Vẽ 1 hàng có 2 cột bằng cấu trúc Row sạch sẽ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // CỘT 1: Ô hiển thị thông tin bàn (Chiếm 70% chiều ngang)
                    Surface(
                        modifier = Modifier.weight(0.7f).height(72.dp),
                        shape = RoundedCornerShape(50),
                        color = tableBackgroundColor,
                        border = BorderStroke(3.dp, Color.Black)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(text = tableText, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                        }
                    }

                    // CỘT 2: Nút nhỏ mang tên "Đổi" (Chiếm 30% chiều ngang)
                    // Điều kiện bấm: Thuộc nhóm màu Cam HOẶC (Đã chọn bàn đi VÀ bàn đích phải trống hoàn toàn)
                    val isClickable = isInGroup || (selectedOriginTableId != null && table.status == "available")

                    Surface(
                        modifier = Modifier
                            .weight(0.3f)
                            .height(72.dp)
                            .clickable(enabled = isClickable) {
                                if (isInGroup) {
                                    // Click nút Cam -> chuyển thành Xanh nước biển
                                    selectedOriginTableId = table.id
                                } else {
                                    // Click nút Xám của bàn trống khi đã chọn gốc -> Thực thi hoán đổi dữ liệu
                                    val originTable = tables.find { it.id == selectedOriginTableId }
                                    if (originTable != null) {
                                        viewModel.transferTable(originTable, table, tables)
                                        onTransferSuccess() // Tự đóng trang chuyển bàn
                                    }
                                }
                            },
                        shape = RoundedCornerShape(50),
                        color = changeButtonColor,
                        border = BorderStroke(3.dp, Color.Black)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "Đổi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = if (isClickable) Color.Black else Color.Gray
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}