package com.example.aifieldassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aifieldassistant.data.local.AppDatabase
import com.example.aifieldassistant.data.repository.ReportRepository
import com.example.aifieldassistant.ui.screens.CaptureScreen
import com.example.aifieldassistant.ui.screens.CreateReportScreen
import com.example.aifieldassistant.ui.screens.HistoryScreen
import com.example.aifieldassistant.ui.screens.ReportDetailScreen
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
                        viewModel = viewModel, // TRUYỀN VIEWMODEL VÀO ĐÂY
                        onBackClick = { navController.navigate("history") },
                        onOpenCamera = {navController.navigate("capture")},
                        imagePath = capturedImagePath // Nếu bạn đang có
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
                composable("detail/{reportId}") { backStackEntry ->
                    val reportIdString = backStackEntry.arguments?.getString("reportId")
                    val reportId = reportIdString?.toIntOrNull()

                    if (reportId != null) {
                        // Gọi ViewModel để lắng nghe dữ liệu của report này
                        val report by viewModel.getReportById(reportId)
                            .collectAsState(initial = null)

                        // Khi load được report từ DB ra thì vẽ UI
                        if (report != null) {
                            ReportDetailScreen(
                                report = report!!,
                                onBackClick = { navController.popBackStack() },
                                viewModel = viewModel
                            )
                        } else {
                            // Hiển thị vòng xoay đang tải (Loading) nếu chưa load xong
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}



