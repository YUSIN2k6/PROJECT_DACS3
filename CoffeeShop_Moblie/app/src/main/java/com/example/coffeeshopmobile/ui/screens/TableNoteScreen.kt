package com.example.coffeeshopmobile.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
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
fun TableNoteScreen(
    table: Table,
    onBackClick: () -> Unit,
    viewModel: TableViewModel = viewModel() // Đã gỡ bỏ tham số onUpdateSuccess không dùng đến để hết cảnh báo vàng
) {
    // Lấy chuỗi ghi chú từ Firebase
    val oldNotesHistory = table.currentOrder?.notes ?: ""
    var noteInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // --- HEADING (Nút lùi & Tiêu đề) ---
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
                text = "Ghi chú (BÀN ${table.tableNumber})",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
        }

        // --- TEXTAREA: Ô NHẬP LIỆU NHIỀU DÒNG ---
        OutlinedTextField(
            value = noteInput,
            onValueChange = { noteInput = it },
            placeholder = { Text("Nhập thông tin ghi chú ở đây...", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- NÚT CẬP NHẬT ---
        ActionPillButton(
            text = "Cập nhật",
            enabled = noteInput.isNotBlank(),
            onClick = {
                viewModel.addTableNote(table.id, noteInput, oldNotesHistory)
                noteInput = ""
            }
        )

        // --- HỘP LỊCH SỬ GHI CHÚ NỀN XANH LỤC NHẠT ---
        if (oldNotesHistory.isNotBlank()) {
            Spacer(modifier = Modifier.height(32.dp))

            // Hàng tiêu đề lịch sử kết hợp bộ đôi nút bên phải
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lịch sử ghi chú:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Cụm nút bấm Chỉnh sửa & Xóa nằm sát cạnh nhau
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. NÚT CHỈNH SỬA: Nền Coffee chủ đạo, chữ trắng
                    Button(
                        onClick = {
                            val timeRegex = Regex("\\(\\d{2}:\\d{2}\\)\\s*")
                            val cleanedText = oldNotesHistory.replace(timeRegex, "")
                            noteInput = cleanedText
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Chỉnh sửa", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    // 2. NÚT XÓA LỊCH SỬ: Nền đỏ rực, chữ trắng
                    Button(
                        onClick = {
                            viewModel.clearTableNotes(table.id)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Xoá lịch sử", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE8F5E9),
                border = BorderStroke(1.dp, Color(0xFFC8E6C9))
            ) {
                Text(
                    text = oldNotesHistory,
                    fontSize = 20.sp,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(16.dp),
                    lineHeight = 28.sp
                )
            }
        }
    }
}