package com.example.coffeeshopmobile.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coffeeshopmobile.data.model.OrderItem
import com.example.coffeeshopmobile.data.model.Table
import com.example.coffeeshopmobile.utils.formatPrice
import com.example.coffeeshopmobile.viewmodel.TableViewModel

@Composable
fun CartScreen(
    table: Table,
    onBackClick: () -> Unit,
    onNoteClick: () -> Unit,
    onServeSuccess: () -> Unit,
    viewModel: TableViewModel = viewModel()
) {
    val order = table.currentOrder

    // ĐÃ SỬA: Giỏ hàng chỉ lọc hiển thị những món CHƯA GỬI XUỐNG PHA CHẾ
    val unservedItems = order?.items?.filter { !it.sentToKitchen } ?: emptyList()
    val totalQuantity = unservedItems.sumOf { it.quantity }
    val totalPrice = unservedItems.sumOf { it.price * it.quantity }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // === HEADER ===
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Trở về", modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Giỏ hàng (BÀN ${table.tableNumber})",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
        }

        // === DANH SÁCH MÓN TRONG GIỎ HÀNG ===
        if (unservedItems.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Giỏ hàng đang trống", fontSize = 18.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(unservedItems) { item ->
                    CartItemRow(
                        item = item,
                        onIncrease = { viewModel.updateCartItemQuantity(table, item.itemId, 1) },
                        onDecrease = { viewModel.updateCartItemQuantity(table, item.itemId, -1) }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }

        // === KHU VỰC BOTTOM BÁM ĐÁY ===
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF9F9F9))
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Tổng số lượng: $totalQuantity món", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Text("Thành tiền: ${totalPrice.formatPrice()}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }

                Button(
                    onClick = onNoteClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "Ghi chú", tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ghi chú", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            // NÚT GỬI YÊU CẦU XUỐNG PHA CHẾ
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clickable(enabled = unservedItems.isNotEmpty()) {
                        viewModel.sendOrderToKitchen(table)
                        onServeSuccess()
                    },
                shape = RoundedCornerShape(50),
                color = if (unservedItems.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.LightGray,
                border = BorderStroke(3.dp, if (unservedItems.isNotEmpty()) Color.Black else Color.Gray)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("Gửi yêu cầu pha chế", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun CartItemRow(item: OrderItem, onIncrease: () -> Unit, onDecrease: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.itemName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(item.price.formatPrice(), fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        }

        // Cụm Tăng giảm số lượng
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(36.dp).clickable { onDecrease() },
                shape = RoundedCornerShape(8.dp), color = Color.LightGray
            ) {
                Box(contentAlignment = Alignment.Center) { Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            }
            Text(
                text = "${item.quantity}",
                fontSize = 18.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.width(40.dp), textAlign = TextAlign.Center
            )
            Surface(
                modifier = Modifier.size(36.dp).clickable { onIncrease() },
                shape = RoundedCornerShape(8.dp), color = Color.Black
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}