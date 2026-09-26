import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.aifieldassistant.ui.screens.CameraPreview
import com.example.aifieldassistant.ui.screens.HistoryScreen
import com.example.aifieldassistant.ui.screens.takePhoto
import com.example.aifieldassistant.ui.theme.AIFieldAssistantTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen() {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    // Khởi tạo đối tượng ImageCapture để chụp ảnh
    val imageCapture = remember { ImageCapture.Builder().build() }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Chụp ảnh hiện trường") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (hasCameraPermission) {
                // Khung chứa Camera thật
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Chiếm phần lớn màn hình
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                ) {
                    CameraPreview(imageCapture = imageCapture)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Nút chụp ảnh
                Button(
                    onClick = {
                        val executor = ContextCompat.getMainExecutor(context)
                        takePhoto(imageCapture, context, executor) { file ->
                            Toast.makeText(context, "Đã lưu ảnh: ${file.name}", Toast.LENGTH_SHORT).show()
                            // TODO: Lưu đường dẫn file này vào ViewModel để lát gửi cho AI
                        }
                    },
                    modifier = Modifier.size(72.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Chụp ảnh", modifier = Modifier.size(32.dp))
                }
            } else {
                Text("Cần cấp quyền camera để tiếp tục.")
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Cấp quyền")
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CaptureScreenPreview() {
    // Tạm thời dùng MaterialTheme mặc định nếu bạn chưa cấu hình xong ViVuTheme
    AIFieldAssistantTheme {
        CaptureScreen()// Gọi hàm giao diện chính của bạn vào đây
    }
}