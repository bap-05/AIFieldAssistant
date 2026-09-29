package com.example.aifieldassistant.ui.screens

import ReportResultScreen
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.example.aifieldassistant.data.local.entity.ReportEntity
import com.example.aifieldassistant.data.remote.Content
import com.example.aifieldassistant.data.remote.GeminiRequest
import com.example.aifieldassistant.data.remote.GenerationConfig
import com.example.aifieldassistant.data.remote.InlineData
import com.example.aifieldassistant.data.remote.Part
import com.example.aifieldassistant.data.remote.RetrofitClient
import com.example.aifieldassistant.ui.viewmodel.AiAnalysisResult
import com.example.aifieldassistant.ui.viewmodel.ReportUiState
import com.example.aifieldassistant.ui.viewmodel.ReportViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.text.replace

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    report: ReportEntity,
    viewModel: ReportViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    val reportList by viewModel.allReports.collectAsState()
    // 🌟 Đổi tên biến để tránh che khuất (shadowing) tham số đầu vào `report`
    val currentReport = reportList.find { it.id == report.id } ?: report
    var showDeleteDialog by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    when (val state = uiState) {
        is ReportUiState.Loading -> {
            // Hiển thị vòng xoay chờ khi đang gọi API
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF00695C))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("AI đang phân tích lại...", color = Color.Gray)
                }
            }
        }

        is ReportUiState.Review -> {
            ReportResultScreen(
                report = state.aiResult,
                onBackClick = { viewModel.resetState() }, // Hủy và trở lại màn hình nhập liệu
                onSaveClick = { editedResult ->
                    // Lưu dữ liệu đã chỉnh sửa vào Database
                    viewModel.confirmAndSaveReport(editedResult, state.originalText, state.imagePath, state.reportId)
                    onBackClick() // Thoát về màn hình danh sách bên ngoài
                }
            )

        }

        is ReportUiState.Error -> {
            // Báo lỗi và quay về trạng thái Idle
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        tint = Color(0xFFE65100), // Màu cam cảnh báo
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.message,
                        color = Color.Black,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 32.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Nút thoát ra ngoài màn hình danh sách (History)
                    Button(
                        onClick = {
                            viewModel.resetState()
                            onBackClick() // Quay về màn hình trước đó
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00695C))
                    ) {
                        Text("Về màn hình chính")
                    }
                }
            }
        }

        is ReportUiState.Idle -> {
            // 🌟 3. TOÀN BỘ CODE GIAO DIỆN CHI TIẾT CŨ SẼ ĐẶT Ở ĐÂY
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Chi tiết sự cố") },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Quay lại")
                            }
                        },
                        actions = {
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Xóa báo cáo",
                                    tint = Color(0xFFD32F2F)
                                )
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
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // 🌟 1. HIỂN THỊ CẢNH BÁO VÀ NÚT TẢI LẠI NẾU CHƯA ĐỒNG BỘ HOẶC LỖI
                    if (currentReport.syncStatus != "COMPLETED" && currentReport.syncStatus != "SYNCED") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Filled.Warning,
                                        contentDescription = "Chờ mạng",
                                        tint = Color(0xFFF57C00)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Báo cáo chưa được phân tích xong hoặc đang chờ mạng.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFE65100)
                                    )
                                }

                                Button(
                                    onClick = {
                                        // Chỉ hiện thông báo báo cho người dùng biết đang xử lý
                                        Toast.makeText(context, "Đang gửi dữ liệu lên AI...", Toast.LENGTH_SHORT).show()

                                        // 🌟 Gọi hàm đúng với 1 tham số như ViewModel đã định nghĩa
                                        viewModel.retryAnalyzeReport(currentReport)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00695C))
                                ) {
                                    Icon(Icons.Filled.Refresh, contentDescription = "Tải lại", tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tải lại", color = Color.White)
                                }
                            }
                        }
                    }

                    // 2. Hiển thị Ảnh hiện trường (Nếu có)
                    if (currentReport.imagePath != null) {
                        val bitmap = remember(currentReport.imagePath) {
                            BitmapFactory.decodeFile(currentReport.imagePath)?.asImageBitmap()
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = "Ảnh sự cố",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // 3. Tên sự cố & Mức độ ưu tiên
                    Text(
                        text = currentReport.issue ?: "Chưa xác định",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF388E3C)
                    )

                    Surface(
                        color = if (currentReport.priority == "Cao") Color(0xFFE65100) else Color(0xB0388E3C),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Mức độ: ${currentReport.priority ?: "N/A"}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Các thẻ thông tin phân tích từ AI
                    DetailCard(title = "Phân loại", content = currentReport.category)
                    DetailCard(title = "Vị trí", content = currentReport.location)
                    DetailCard(title = "Tóm tắt sự việc", content = currentReport.summary)
                    DetailCard(title = "Hành động đề xuất", content = currentReport.suggestedAction)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nội dung gốc: ${currentReport.originalText}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // 5. Hộp thoại xác nhận xóa
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Xác nhận xóa") },
                    text = { Text("Bạn có chắc chắn muốn xóa báo cáo này không? Hành động này không thể hoàn tác.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                viewModel.deleteReport(currentReport)
                                Toast.makeText(context, "Đã xóa báo cáo thành công", Toast.LENGTH_SHORT).show()
                                onBackClick()
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD32F2F))
                        ) {
                            Text("Xóa")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Hủy", color = Color.Black)
                        }
                    }
                )
            }
        }
    }


}

@Composable
fun DetailCard(title: String, content: String?) {
    if (!content.isNullOrBlank()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = title, fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = content, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}