package com.example.coffeeshopmobile.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun TableDetailScreen(
    table: Table,
    allTables: List<Table>,
    onBackClick: () -> Unit,
    onMergeClick: () -> Unit,
    onTransferClick: () -> Unit,
    onNoteClick: () -> Unit,
    onMenuClick: () -> Unit,
    onServedHistoryClick: () -> Unit,
    viewModel: TableViewModel = viewModel()
) {
    // 1. Phân loại quan hệ bàn
    val isMergedSource = table.status == "merged"
    val isMergeTarget = allTables.any { it.status == "merged" && it.mergedWith == table.tableNumber }

    // Định nghĩa "Có khách" cho tất cả các trạng thái đang hoạt động
    val hasCustomer = table.status in listOf("occupied", "preparing", "served", "pending") ||
            (isMergeTarget && table.status != "available")

    // 2. Logic đổi tên nút Gộp bàn
    val (mergeButtonText, onMergeAction) = when {
        isMergedSource -> Pair("Rời gộp") { viewModel.unmergeTable(table.id) }
        isMergeTarget -> Pair("Rời gộp tất cả") { viewModel.unmergeAllFromTarget(table.tableNumber) }
        else -> Pair("Gộp bàn") { onMergeClick() }
    }

    // TÍNH TOÁN TRẠNG THÁI KHÓA NÚT
    val isMergeEnabled = isMergedSource || isMergeTarget || !hasCustomer
    val hasServedItems = table.currentOrder?.items?.any { it.sentToKitchen } == true
    val isPendingPayment = table.status == "pending" // Kiểm tra bàn có đang chờ tính tiền không

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
    ) {
        // --- PHẦN HEADING ---
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
                text = if (isMergedSource) "BÀN ${table.tableNumber} GỘP ${table.mergedWith}" else "BÀN SỐ ${table.tableNumber}",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
        }

        // --- PHẦN CÁC NÚT CHỨC NĂNG CHẢY DỌC ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ActionPillButton(
                text = "Có khách",
                enabled = table.status == "available" && !isMergedSource,
                onClick = { viewModel.updateTableStatus(table.id, "occupied") }
            )

            ActionPillButton(
                text = "Chọn nước",
                enabled = hasCustomer && !isMergedSource,
                onClick = onMenuClick
            )

            ActionPillButton(
                text = "Tiến độ phục vụ",
                enabled = hasServedItems,
                onClick = onServedHistoryClick
            )

            // --- NÚT MỚI: YÊU CẦU THANH TOÁN ---
            ActionPillButton(
                text = if (isPendingPayment) "Đang chờ TT (nhấn để huỷ)" else "Yêu cầu thanh toán",
                enabled = (table.status == "served" || isPendingPayment) && !isMergedSource,
                onClick = {
                    if (isPendingPayment) {
                        // Đang chờ -> nhấn để huỷ, trở về đã phục vụ
                        viewModel.updateTableStatus(table.id, "served")
                    } else {
                        // Đã phục vụ -> nhấn để yêu cầu thanh toán
                        viewModel.updateTableStatus(table.id, "pending")
                    }
                }
            )

            ActionPillButton(
                text = mergeButtonText,
                enabled = isMergeEnabled,
                onClick = onMergeAction
            )

            ActionPillButton(
                text = "Chuyển bàn",
                enabled = !isMergedSource,
                onClick = onTransferClick
            )

            ActionPillButton(
                text = "Ghi chú",
                enabled = hasCustomer || isMergedSource || isMergeTarget,
                onClick = onNoteClick
            )

            ActionPillButton(
                text = "Hủy khách",
                enabled = hasCustomer,
                onClick = { viewModel.updateTableStatus(table.id, "available") }
            )
        }
    }
}

/**
 * Thành phần nút bấm viên thuốc viền đen dùng chung
 */
@Composable
fun ActionPillButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val backgroundColor = if (enabled) MaterialTheme.colorScheme.primary else Color(0xFFE0E0E0)
    val textColor = if (enabled) Color.White else Color.Gray

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(50),
        color = backgroundColor,
        border = BorderStroke(3.dp, Color.Black)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = textColor
            )
        }
    }
}