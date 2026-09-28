package com.example.aifieldassistant.ui.screens

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
import androidx.compose.material.icons.filled.ArrowBack

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.aifieldassistant.R
import com.example.aifieldassistant.ui.theme.AIFieldAssistantTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(
    onImageCaptured: (String) -> Unit, // Callback trả đường dẫn ảnh
    onBackClick: () -> Unit            // Callback đóng màn hình
) {
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

    val imageCapture = remember { ImageCapture.Builder().build() }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chụp ảnh hiện trường") },
                navigationIcon = {
                    // Thêm nút Back để người dùng có thể hủy chụp ảnh
                    IconButton(onClick = onBackClick) {
                        Icon(painter = painterResource(R.drawable.back), contentDescription = "Quay lại",Modifier.size(25.dp))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (hasCameraPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                ) {
                    CameraPreview(imageCapture = imageCapture)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val executor = ContextCompat.getMainExecutor(context)
                        takePhoto(imageCapture, context, executor) { file ->
                            Toast.makeText(context, "Đã lưu ảnh", Toast.LENGTH_SHORT).show()
                            // Trả đường dẫn tuyệt đối về cho NavHost
                            onImageCaptured(file.absolutePath)
                        }
                    },
                    modifier = Modifier.size(72.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(painter = painterResource(R.drawable.camera), contentDescription = "Chụp ảnh", modifier = Modifier.size(32.dp))
                }
            } else {
                Text("Cần cấp quyền camera để tiếp tục.")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Cấp quyền")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CaptureScreenPreview() {
    AIFieldAssistantTheme {
        // Truyền callback rỗng vào Preview để không bị báo đỏ
        CaptureScreen(
            onImageCaptured = {},
            onBackClick = {}
        )
    }
}