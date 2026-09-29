import android.R
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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aifieldassistant.ui.viewmodel.AiAnalysisResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportResultScreen(
    report: AiAnalysisResult, // Sử dụng data class từ ViewModel
    onBackClick: () -> Unit,
    onSaveClick: (AiAnalysisResult) -> Unit
) {
    // Quản lý trạng thái xem/sửa
    var isEditing by remember { mutableStateOf(false) }

    // Lưu trữ dữ liệu chỉnh sửa tạm thời
    var category by remember { mutableStateOf(report.category ?: "") }
    var priority by remember { mutableStateOf(report.priority ?: "") }
    var location by remember { mutableStateOf(report.location ?: "") }
    var issue by remember { mutableStateOf(report.issue ?: "") }
    var suggestedAction by remember { mutableStateOf(report.suggestedAction ?: "") }
    var summary by remember { mutableStateOf(report.summary ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Chỉnh sửa báo cáo" else "Kết quả phân tích") },
                navigationIcon = {
                    IconButton(onClick = onBackClick, colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF388E3C),
                    titleContentColor = Color.White
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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                if (!isEditing) {
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
                }

                if (isEditing) {
                    // CHẾ ĐỘ CHỈNH SỬA
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Danh mục sự cố") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = priority, onValueChange = { priority = it }, label = { Text("Mức độ ưu tiên (Thấp/Trung bình/Cao)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Vị trí") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = issue, onValueChange = { issue = it }, label = { Text("Vấn đề") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = suggestedAction, onValueChange = { suggestedAction = it }, label = { Text("Hành động đề xuất") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    OutlinedTextField(value = summary, onValueChange = { summary = it }, label = { Text("Tóm tắt tình hình") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                } else {
                    // CHẾ ĐỘ XEM (Giao diện gốc của bạn)
                    ResultField(label = "Danh mục sự cố (Category)", value = category)
                    ResultPriorityField(priority = priority)
                    ResultField(label = "Vị trí", value = location)
                    ResultField(label = "Vấn đề (Issue)", value = issue)
                    ResultField(label = "Hành động đề xuất", value = suggestedAction)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "Tóm tắt tình hình", style = MaterialTheme.typography.labelMedium, color = Color(0xFF388E3C))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = summary, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!isEditing) {
                    OutlinedButton(
                        onClick = { isEditing = true },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.Black,
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Chỉnh sửa")
                    }
                } else {
                    OutlinedButton(
                        onClick = { isEditing = false },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.Black,
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Hủy sửa")
                    }
                }

                Button(
                    onClick = {
                        // Đóng gói dữ liệu đã chỉnh sửa và trả về CreateReportScreen để lưu
                        val finalData = report.copy(
                            category = category,
                            priority = priority,
                            issue = issue,
                            suggestedAction = suggestedAction,
                            summary = summary
                        )
                        onSaveClick(finalData)
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00695C))
                ) {
                    Icon(Icons.Filled.Done, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lưu Báo cáo")
                }
            }
        }
    }
}

@Composable
fun ResultField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value.ifBlank { "N/A" }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))
    }
}

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
            Text(text = priority.ifBlank { "N/A" }, color = textColor, fontWeight = FontWeight.Bold)
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))
    }
}