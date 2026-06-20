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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun MergeTableScreen(
    currentTable: Table,
    onBackClick: () -> Unit,
    onMergeSuccess: () -> Unit,
    viewModel: TableViewModel = viewModel()
) {
    // Lắng nghe danh sách tất cả các bàn realtime từ Firebase
    val tables by viewModel.tables.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
    ) {
        // --- HEADING (Nút lùi & Tiêu đề Gộp) ---
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
                text = "Gộp (BÀN ${currentTable.tableNumber})",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
        }

        // --- DANH SÁCH BÀN ĐỂ CHỌN GỘP ---
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(tables) { table ->
                val isSelf = table.id == currentTable.id

                // Xác định màu nền theo đúng yêu cầu bài toán của bạn
                val backgroundColor = when {
                    isSelf -> Color(0xFFE0E0E0) // Bàn hiện tại: Màu xám
                    table.status == "available" -> MaterialTheme.colorScheme.primary // Bàn trống: Màu nâu coffee chủ đạo
                    else -> Color(0xFF81C784) // Bàn có khách: Màu xanh lá
                }

                // Cấu hình chữ hiển thị
                val displayText = if (isSelf) {
                    "BÀN ${table.tableNumber} (đang lựa chọn)"
                } else {
                    "BÀN ${table.tableNumber}"
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clickable(enabled = !isSelf) {
                            // Thực hiện lệnh gộp bàn lên Firebase
                            viewModel.mergeTable(currentTable.id, table.tableNumber)
                            // Kích hoạt hàm báo thành công để tự động đóng trang gộp về màn hình chính
                            onMergeSuccess()
                        },
                    shape = RoundedCornerShape(50),
                    color = backgroundColor,
                    border = BorderStroke(3.dp, Color.Black)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = displayText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = if (isSelf) Color.Gray else Color.Black
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}