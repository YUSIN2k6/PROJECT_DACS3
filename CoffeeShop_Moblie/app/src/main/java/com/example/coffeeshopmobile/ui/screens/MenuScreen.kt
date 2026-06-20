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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.coffeeshopmobile.data.model.MenuItem
import com.example.coffeeshopmobile.data.model.Table
import com.example.coffeeshopmobile.utils.formatPrice
import com.example.coffeeshopmobile.viewmodel.TableViewModel

@Composable
fun MenuScreen(
    table: Table,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    viewModel: TableViewModel = viewModel()
) {
    val menuItems by viewModel.menu.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // === 1. PHẦN HEADER (CỐ ĐỊNH PHÍA TRÊN) ===
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .padding(top = 16.dp),
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
                text = "Chọn nước (BÀN ${table.tableNumber})",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
        }

        // === 2. PHẦN DANH SÁCH MÓN (CUỘN ĐƯỢC) ===
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(menuItems) { item ->
                MenuItemRow(item = item, onAddClick = { viewModel.addMenuItemToCart(table, item) })
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // === 3. PHẦN BOTTOM BAR (NÚT XEM GIỎ HÀNG CỐ ĐỊNH ĐÁY) ===
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 56.dp),
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primary,
            border = BorderStroke(3.dp, Color.Black)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clickable { onCartClick() }
            ) {
                Text(
                    text = "Xem giỏ hàng",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun MenuItemRow(item: MenuItem, onAddClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF5F5F5),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.price.formatPrice(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // -- ĐÃ SỬA CỘT PHẢI: Nút dấu CỘNG (+) CÓ HIỆU ỨNG CHỚP SÁNG --
            FilledIconButton(
                onClick = onAddClick,
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp), // Hoàn toàn hợp lệ với FilledIconButton
                colors = IconButtonDefaults.filledIconButtonColors( // Đổi thành filledIconButtonColors
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Thêm món",
                    tint = Color.White
                )
            }
        }
    }
}