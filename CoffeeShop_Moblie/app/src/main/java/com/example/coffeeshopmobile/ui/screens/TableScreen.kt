package com.example.coffeeshopmobile.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coffeeshopmobile.data.model.Table
import com.example.coffeeshopmobile.viewmodel.TableViewModel

@Composable
fun TableScreen(
    viewModel: TableViewModel = viewModel(),
    onTableClick: (Table) -> Unit
) {
    val tables by viewModel.tables.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Danh sách bàn",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 40.dp, bottom = 32.dp)
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(top = 50.dp),
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(tables) { table ->
                    val targetTable = if (table.status == "merged") tables.find { it.tableNumber == table.mergedWith } else null

                    TableItem(
                        table = table,
                        targetTable = targetTable,
                        onClick = { onTableClick(table) }
                    )
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun TableItem(table: Table, targetTable: Table?, onClick: () -> Unit) {
    // Trạng thái thực tế dùng để tô màu (Nếu gộp bàn, lấy màu của bàn chính)
    val effectiveStatus = if (table.status == "merged") targetTable?.status ?: "available" else table.status

    // ĐÃ SỬA: Gom tất cả trạng thái có khách thành MÀU XANH LỤC, chỉ giữ bàn trống là MÀU XÁM
    val backgroundColor = if (effectiveStatus in listOf("occupied", "preparing", "served", "pending")) {
        Color(0xFF81C784) // Xanh lục (Có khách / Đang xử lý)
    } else {
        Color(0xFFE0E0E0) // Xám (Bàn trống)
    }

    // Hiển thị thêm dòng chữ nhỏ phụ trợ để nhân viên biết bàn đang ở giai đoạn nào
    val statusText = when (table.status) {
        "occupied" -> " (Có khách)"
        "preparing" -> " (Đang chuẩn bị)"
        "served" -> " (Đã phục vụ)"
        "pending" -> " (Chờ thanh toán)"
        "merged" -> " (Đang gộp bàn ${table.mergedWith ?: ""})"
        else -> ""
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(50),
        color = backgroundColor,
        border = BorderStroke(3.dp, Color.Black)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp)) {
                        append("BÀN ${table.tableNumber}")
                    }
                    if (statusText.isNotEmpty()) {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Normal, fontSize = 18.sp)) {
                            append(statusText)
                        }
                    }
                },
                color = Color.Black
            )
        }
    }
}