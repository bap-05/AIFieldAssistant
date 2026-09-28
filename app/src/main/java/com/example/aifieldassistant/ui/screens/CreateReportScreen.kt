package com.example.aifieldassistant.ui.screens


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
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReportScreen(
    onBackClick: () -> Unit,
    onOpenCamera: () -> Unit,
    onSubmit: (String, String?) -> Unit,
    imagePath: String?) {
    val context = LocalContext.current
    var descriptionText by remember { mutableStateOf("") }
    var attachedImagePath by remember { mutableStateOf<String?>(null) }
    var isRecording by remember { mutableStateOf(false)
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            // Khi người dùng chọn ảnh xong, copy ảnh đó vào Cache và lấy đường dẫn gán lên giao diện
            val cachedFile = copyUriToCache(context, uri)
            if (cachedFile != null) {
                attachedImagePath = cachedFile.absolutePath // Gán vào state hiển thị ảnh của bạn
            }
        }
    }
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isRecording = false // Tắt icon đỏ khi thu âm xong

        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.getOrNull(0) ?: ""

            if (spokenText.isNotEmpty()) {
                // Nối văn bản vừa đọc vào text hiện tại
                descriptionText = if (descriptionText.isBlank()) {
                    spokenText
                } else {
                    "$descriptionText $spokenText"
                }
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
                        Icon(painter = painterResource(R.drawable.back), contentDescription = "Quay lại",
                            modifier = Modifier.size(25.dp))
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
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color.Black
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                // 2. Bắt sự kiện khi nút đó được bấm
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus() // Bỏ focus khỏi ô nhập liệu -> Tự động ẩn bàn phím
                    })
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Khu vực hiển thị tệp đính kèm (Ảnh)
            if (attachedImagePath != null) {
                val bitmap = remember(attachedImagePath) {
                    BitmapFactory.decodeFile(attachedImagePath)?.asImageBitmap()
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Ảnh đính kèm",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Nút xóa ảnh góc trên cùng bên phải
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
                            onOpenCamera()
                        }
                    ) {
                        Icon(painter = painterResource(R.drawable.camera), contentDescription = "Chụp ảnh", tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                            )
                    }

                    // Nút mở Thư viện ảnh (Tùy chọn thêm)
                    IconButton(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Icon(painter = painterResource(R.drawable.image), contentDescription = "Chọn ảnh", tint = Color.Black,
                            modifier = Modifier.size(20.dp))
                    }

                    // Nút Thu âm (Speech to Text)
                    IconButton(
                        onClick = {
                            try {
                                // Cấu hình Intent nhận diện giọng nói
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN") // Bắt buộc nhận diện Tiếng Việt
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Hãy mô tả sự cố...")
                                }

                                isRecording = true // Đổi màu icon thành đỏ
                                speechLauncher.launch(intent) // Hiển thị bảng thu âm của Google

                            } catch (e: Exception) {
                                isRecording = false
                                Toast.makeText(context, "Thiết bị không hỗ trợ thu âm", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
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

            // 4. Nút Gửi cho AI phân tích
            Button(
                onClick = {  },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = descriptionText.isNotBlank() || attachedImagePath != null,
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.White,
                    containerColor = Color(0xFF00695C),

                )
            ) {
                Icon(painter = painterResource(R.drawable.ai), contentDescription = null, modifier = Modifier.size(25.dp)) // Icon AI
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI Phân tích & Tạo báo cáo", style = MaterialTheme.typography.titleMedium)
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
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun preview(){
    AIFieldAssistantTheme {
        CreateReportScreen(
            imagePath = null, // <-- THÊM DÒNG NÀY
            onBackClick = {},
            onOpenCamera = {},
            onSubmit = { _, _ -> }
        )
    }
}
