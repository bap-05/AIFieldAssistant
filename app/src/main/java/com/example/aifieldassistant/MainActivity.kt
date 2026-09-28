package com.example.aifieldassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aifieldassistant.data.local.AppDatabase
import com.example.aifieldassistant.data.repository.ReportRepository
import com.example.aifieldassistant.ui.screens.CaptureScreen
import com.example.aifieldassistant.ui.screens.CreateReportScreen
import com.example.aifieldassistant.ui.screens.HistoryScreen
import com.example.aifieldassistant.ui.theme.AIFieldAssistantTheme
import com.example.aifieldassistant.ui.viewmodel.ReportViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = AppDatabase.getDatabase(this)
        val repository = ReportRepository(database.reportDao())

        // Khởi tạo ViewModel bằng Factory
        val viewModel: ReportViewModel by viewModels {
            ReportViewModel.Factory(repository)
        }
        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "history") {
                // Màn hình 1: Lịch sử báo cáo
                composable("history") {
                    HistoryScreen(
                        viewModel = viewModel,
                        onFabClick = { navController.navigate("create") },
                        onItemClick = { reportId ->
                            navController.navigate("detail/$reportId") // Bấm vào 1 item sang màn hình Chi tiết
                        }
                    )
                }

                // Màn hình 2: Tạo báo cáo mới
                composable("create") {backStackEntry ->
                    // Lấy đường dẫn ảnh từ Camera trả về (nếu có)
                    val capturedImagePath = backStackEntry.savedStateHandle.get<String>("imagePath")
                    CreateReportScreen(
                        imagePath = capturedImagePath,
                        onBackClick = { navController.popBackStack() },
                        onSubmit = { text, imagePath ->
                            // Lưu nháp vào Room
                            viewModel.saveDraftReport(text, imagePath)

                            // Quay về màn hình chính
                            navController.popBackStack()
                        },
                        onOpenCamera = {
                            navController.navigate("capture")
                        }
                    )

                }
                composable("capture") {
                    CaptureScreen(
                        onImageCaptured = { imagePath ->
                            // Trả đường dẫn ảnh về cho màn hình "create"
                            navController.previousBackStackEntry?.savedStateHandle?.set("imagePath", imagePath)
                            navController.popBackStack() // Tự động đóng camera
                        },
                        onBackClick = {
                            navController.popBackStack() // Đóng camera khi bấm nút Back
                        }
                    )
                }
            }
        }
    }
}



