package com.example.aifieldassistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aifieldassistant.ui.theme.AIFieldAssistantTheme

// Model tạm thời đại diện cho kết quả JSON trả về từ AI
data class AiReportResult(
    val category: String,
    val location: String,
    val priority: String,
    val issue: String,
    val suggestedAction: String,
    val summary: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportResultScreen(
    report: AiReportResult, // Truyền kết quả AI vào đây
//    onBackClick: () -> Unit,
//    onEditClick: () -> Unit, // Mở form để sửa tay
//    onSaveClick: () -> Unit  // Lưu vào DB (Offline/Online)
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kết quả phân tích") },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Khu vực nội dung cuộn được
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header cảnh báo người dùng kiểm tra lại
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Vui lòng kiểm tra lại độ chính xác của báo cáo do AI tự động tạo trước khi lưu.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                // Các khối thông tin chi tiết
                ResultField(label = "Danh mục sự cố (Category)", value = report.category)
                ResultField(label = "Vị trí (Location)", value = report.location)

                // Hiển thị mức độ ưu tiên với màu sắc nổi bật
                ResultPriorityField(priority = report.priority)

                ResultField(label = "Vấn đề (Issue)", value = report.issue)
                ResultField(label = "Hành động đề xuất", value = report.suggestedAction)

                // Khối Summary có thể dài nên dùng diện tích lớn hơn
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Tóm tắt tình hình", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = report.summary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Khu vực 2 nút hành động
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Nút Chỉnh sửa
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chỉnh sửa")
                }

                // Nút Lưu
                Button(
                    onClick = {},
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Done, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lưu Báo cáo")
                }
            }
        }
    }
}

// Hàm hỗ trợ vẽ một trường thông tin tiêu chuẩn
@Composable
fun ResultField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Divider(modifier = Modifier.padding(top = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))
    }
}

// Hàm hỗ trợ vẽ trường Mức độ ưu tiên có màu
@Composable
fun ResultPriorityField(priority: String) {
    val (bgColor, textColor) = when (priority.uppercase()) {
        "HIGH", "CAO" -> Color(0xFFFFEBEE) to Color(0xFFD32F2F)
        "MEDIUM", "TRUNG BÌNH" -> Color(0xFFFFF3E0) to Color(0xFFF57C00)
        else -> Color(0xFFE8F5E9) to Color(0xFF388E3C)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Mức độ ưu tiên", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .background(color = bgColor, shape = RoundedCornerShape(6.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(text = priority, color = textColor, fontWeight = FontWeight.Bold)
        }
        Divider(modifier = Modifier.padding(top = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))
    }
}
val mockAiReportResult = AiReportResult(
    category = "Sự cố thiết bị (Equipment failure)",
    location = "Khu vực Lễ tân (Reception)",
    priority = "High",
    issue = "Máy điều hòa không hoạt động.",
    suggestedAction = "Cử nhân viên bảo trì đến kiểm tra ngay lập tức.",
    summary = "Máy điều hòa tại khu vực lễ tân đang bị hỏng, dẫn đến việc phòng rất nóng và có nhiều khách hàng phàn nàn. Cần xử lý gấp để đảm bảo trải nghiệm khách hàng."
)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ReportResultScreenPreview() {
    // Tạm thời dùng MaterialTheme mặc định nếu bạn chưa cấu hình xong ViVuTheme
    AIFieldAssistantTheme {
        ReportResultScreen(mockAiReportResult)// Gọi hàm giao diện chính của bạn vào đây
    }
}