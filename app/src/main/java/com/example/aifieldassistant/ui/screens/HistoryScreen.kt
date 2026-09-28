@file:Suppress("DEPRECATION")

package com.example.aifieldassistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aifieldassistant.data.local.entity.ReportEntity
import com.example.aifieldassistant.ui.theme.AIFieldAssistantTheme

import com.example.aifieldassistant.ui.viewmodel.ReportViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: ReportViewModel,
    onFabClick: () -> Unit,
    onItemClick: (Int) -> Unit
) {
    // Lắng nghe dữ liệu thật từ Room Database
    val reportList by viewModel.allReports.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Field Assistant", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF00695C),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onFabClick,
                containerColor = Color(0xFF00695C),
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Tạo báo cáo")
            }
        }
    ) { paddingValues ->
        if (reportList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Chưa có báo cáo nào. Nhấn dấu + để thêm mới.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reportList) { report ->
                    ReportCard(report = report, onClick = { onItemClick(report.id) })
                }
            }
        }
    }
}

@Composable
fun ReportCard(report: ReportEntity, onClick: () -> Unit) {
    // Xử lý màu sắc dựa trên mức độ ưu tiên
    val priorityColor = when (report.priority?.uppercase()) {
        "HIGH", "CAO" -> Color(0xFFD32F2F)
        "MEDIUM", "TRUNG BÌNH" -> Color(0xFFF57C00)
        "LOW", "THẤP" -> Color(0xFF388E3C)
        else -> Color.Gray
    }

    // Chuyển đổi Timestamp thành chuỗi ngày giờ
    val dateFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale("vi", "VN"))
    val dateString = dateFormat.format(Date(report.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),

        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Nếu chưa có category từ AI, hiển thị tạm nội dung gốc
                Text(
                    text = report.category ?: "Đang chờ AI phân tích...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Vị trí: ${report.location ?: "Chưa xác định"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = dateString, style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                    if (report.priority != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = report.priority,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier
                                .background(priorityColor, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if (report.syncStatus == "SYNCED") {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Đã lưu", tint = Color(0xFF388E3C))
                    Text("Đã lưu", style = MaterialTheme.typography.labelSmall, color = Color(0xFF388E3C))
                } else {
                    // Trạng thái PENDING hoặc FAILED
                    Icon(Icons.Filled.Warning, contentDescription = "Chờ mạng", tint = Color(0xFFF57C00))
                    Text("Chờ xử lý", style = MaterialTheme.typography.labelSmall, color = Color(0xFFF57C00))
                }
            }
        }
    }
}
