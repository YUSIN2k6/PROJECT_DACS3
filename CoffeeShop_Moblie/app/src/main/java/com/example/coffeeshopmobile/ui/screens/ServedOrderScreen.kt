package com.example.coffeeshopmobile.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.coffeeshopmobile.data.model.Table

@Composable
fun ServedOrderScreen(
    table: Table,
    onBackClick: () -> Unit
) {
    // ĐÃ SỬA: Lấy tất cả các món ĐÃ GỬI XUỐNG BẾP (sentToKitchen == true) để theo dõi tiến độ
    val kitchenItems = table.currentOrder?.items?.filter { it.sentToKitchen }?.reversed() ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack, "Trở về", modifier = Modifier.size(32.dp), tint = Color.Black)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Tiến độ món (BÀN ${table.tableNumber})",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
        }

        // --- DANH SÁCH MÓN ĐANG/ĐÃ PHA CHẾ ---
        if (kitchenItems.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Chưa có yêu cầu pha chế nào", fontSize = 18.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(kitchenItems) { item ->
                    // ĐỔI MÀU ĐỘNG: Nếu quầy bar tích "Xong" trên Web (served == true) -> Đổi sang nền xanh lá
                    // Nếu quầy bar đang làm (served == false) -> Để nền xám chờ đợi
                    val itemBgColor = if (item.served) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
                    val itemBorderColor = if (item.served) Color(0xFF81C784) else Color(0xFFE0E0E0)
                    val itemTextColor = if (item.served) Color(0xFF2E7D32) else Color.Black
                    val displayText = if (item.served) "(${item.timestamp}) ${item.itemName}" else "⏳ ${item.itemName} (Đang làm...)"

                    Surface(
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = itemBgColor,
                        border = BorderStroke(1.dp, itemBorderColor)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = displayText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = itemTextColor
                            )
                            Text(
                                text = "x${item.quantity}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (item.served) itemTextColor else Color.DarkGray
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}