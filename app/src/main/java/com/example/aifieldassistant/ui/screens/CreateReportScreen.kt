package com.example.aifieldassistant.ui.screens

import ReportResultScreen
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aifieldassistant.R
import com.example.aifieldassistant.ui.theme.AIFieldAssistantTheme
import com.example.aifieldassistant.ui.viewmodel.ReportUiState
import com.example.aifieldassistant.ui.viewmodel.ReportViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReportScreen(
    viewModel: ReportViewModel, // <-- THÊM VIEWMODEL VÀO ĐÂY ĐỂ ĐIỀU HƯỚNG TRẠNG THÁI
    onBackClick: () -> Unit,
    onOpenCamera: () -> Unit,
    imagePath: String?
) {
    // Lắng nghe trạng thái từ ViewModel
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is ReportUiState.Idle -> {
            // ====================================================
            // MÀN HÌNH 1: TRẠNG THÁI IDLE (FORM NHẬP LIỆU GỐC CỦA BẠN)
            // ====================================================
            val context = LocalContext.current
            var descriptionText by remember { mutableStateOf("") }
            var attachedImagePath by remember { mutableStateOf<String?>(null) }
            var isRecording by remember { mutableStateOf(false) }

            val galleryLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri: Uri? ->
                if (uri != null) {
                    val cachedFile = copyUriToCache(context, uri)
                    if (cachedFile != null) {
                        attachedImagePath = cachedFile.absolutePath
                    }
                }
            }
            val speechLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                isRecording = false
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    val spokenText = results?.getOrNull(0) ?: ""
                    if (spokenText.isNotEmpty()) {
                        descriptionText = if (descriptionText.isBlank()) spokenText else "$descriptionText $spokenText"
                    }
                }
            }
            val focusManager = LocalFocusManager.current
            LaunchedEffect(imagePath) {
                if (imagePath != null) {
                    attachedImagePath = imagePath
                }
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Báo cáo sự cố") },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(painter = painterResource(R.drawable.back), contentDescription = "Quay lại", modifier = Modifier.size(25.dp))
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
                    OutlinedTextField(
                        value = descriptionText,
                        onValueChange = { descriptionText = it },
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        placeholder = { Text("Mô tả sự cố (hoặc sử dụng ghi âm)...") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.LightGray,
                            focusedBorderColor = Color.Black
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (attachedImagePath != null) {
                        val bitmap = remember(attachedImagePath) {
                            BitmapFactory.decodeFile(attachedImagePath)?.asImageBitmap()
                        }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = "Ảnh đính kèm",
                                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            IconButton(
                                onClick = { attachedImagePath = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(bottomStart = 12.dp, topEnd = 12.dp))
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Xóa ảnh", tint = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row {
                            IconButton(onClick = {
                                Toast.makeText(context, "Mở Camera", Toast.LENGTH_SHORT).show()
                                onOpenCamera()
                            }) {
                                Icon(painter = painterResource(R.drawable.camera), contentDescription = "Chụp ảnh", tint = Color.Black, modifier = Modifier.size(24.dp))
                            }
                            IconButton(onClick = {
                                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }) {
                                Icon(painter = painterResource(R.drawable.image), contentDescription = "Chọn ảnh", tint = Color.Black, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = {
                                try {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Hãy mô tả sự cố...")
                                    }
                                    isRecording = true
                                    speechLauncher.launch(intent)
                                } catch (e: Exception) {
                                    isRecording = false
                                    Toast.makeText(context, "Thiết bị không hỗ trợ thu âm", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.sound),
                                    contentDescription = "Thu âm",
                                    tint = if (isRecording) Color.Red else Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            // THAY ĐỔI TẠI ĐÂY: Trực tiếp gọi hàm analyzeReport của ViewModel
                            viewModel.analyzeReport(descriptionText, attachedImagePath)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = descriptionText.isNotBlank() || attachedImagePath != null,
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.White,
                            containerColor = Color(0xFF00695C)
                        )
                    ) {
                        Icon(painter = painterResource(R.drawable.ai), contentDescription = null, modifier = Modifier.size(25.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Phân tích & Tạo báo cáo", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }

        is ReportUiState.Loading -> {
            // ====================================================
            // MÀN HÌNH 2: TRẠNG THÁI ĐANG TẢI (LOADING)
            // ====================================================
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF00695C))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("AI đang phân tích hiện trường...", color = Color.Gray)
                }
            }
        }

        is ReportUiState.Review -> {
            // ====================================================
            // MÀN HÌNH 3: XEM VÀ CHỈNH SỬA (KẾT NỐI VỚI ReportResultScreen)
            // ====================================================
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
    }
}

fun copyUriToCache(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.cacheDir, "gallery_report_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}