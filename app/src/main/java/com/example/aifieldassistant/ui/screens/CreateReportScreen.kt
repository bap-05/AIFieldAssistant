package com.example.aifieldassistant.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReportScreen(onBackClick: () -> Boolean,onSubmit: (String, String?) -> Unit) {
    val context = LocalContext.current
    var descriptionText by remember { mutableStateOf("") }
    var attachedImagePath by remember { mutableStateOf<String?>(null) }
    var isRecording by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Báo cáo sự cố") },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 1. Ô nhập liệu đa năng (Nhập tay hoặc kết quả của Speech-to-text hiện ở đây)
            OutlinedTextField(
                value = descriptionText,
                onValueChange = { descriptionText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Chiếm phần lớn không gian màn hình
                placeholder = { Text("Mô tả sự cố (hoặc sử dụng ghi âm)...") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Khu vực hiển thị tệp đính kèm (Ảnh)
            if (attachedImagePath != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Đã đính kèm 1 hình ảnh", fontWeight = FontWeight.Medium)
                        }
                        IconButton(onClick = { attachedImagePath = null }) { // Nút xóa ảnh đính kèm
                            Icon(Icons.Filled.Close, contentDescription = "Xóa ảnh")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 3. Thanh công cụ đính kèm (Ảnh, Mic)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    // Nút mở Camera
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Mở Camera", Toast.LENGTH_SHORT).show()
                            // TODO: Gọi hàm mở Camera ở đây
                        }
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = "Chụp ảnh", tint = MaterialTheme.colorScheme.primary)
                    }

                    // Nút mở Thư viện ảnh (Tùy chọn thêm)
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Mở Thư viện", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = "Chọn ảnh", tint = MaterialTheme.colorScheme.primary)
                    }

                    // Nút Thu âm (Speech to Text)
                    IconButton(
                        onClick = {
                            isRecording = !isRecording
                            Toast.makeText(context, if (isRecording) "Đang thu âm..." else "Dừng thu âm", Toast.LENGTH_SHORT).show()
                            // TODO: Gọi API SpeechRecognizer ở đây
                        }
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = "Thu âm",
                            tint = if (isRecording) Color.Red else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Nút Gửi cho AI phân tích
            Button(
                onClick = {  },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = descriptionText.isNotBlank() || attachedImagePath != null
            ) {
                Icon(Icons.Filled.Warning, contentDescription = null) // Icon AI
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI Phân tích & Tạo báo cáo", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
