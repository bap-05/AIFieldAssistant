package com.example.aifieldassistant.ui.viewmodel
import com.example.aifieldassistant.BuildConfig
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aifieldassistant.data.local.entity.ReportEntity
import com.example.aifieldassistant.data.remote.Content
import com.example.aifieldassistant.data.remote.GeminiRequest
import com.example.aifieldassistant.data.remote.GenerationConfig
import com.example.aifieldassistant.data.remote.InlineData
import com.example.aifieldassistant.data.remote.Part
import com.example.aifieldassistant.data.remote.RetrofitClient
import com.example.aifieldassistant.data.repository.ReportRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

// 1. Data class để Gson hứng dữ liệu JSON từ Gemini trả về
data class AiAnalysisResult(
    val issue: String?,
    val priority: String?,
    val location: String?,
    val summary: String?,
    val suggestedAction: String?,
    val category: String?
)

// 2. Khai báo các trạng thái của màn hình tạo báo cáo
sealed class ReportUiState {
    object Idle : ReportUiState() // Trạng thái bình thường (Đang nhập liệu)
    object Loading : ReportUiState() // Đang xoay chờ AI
    data class Review(
        val aiResult: AiAnalysisResult,
        val originalText: String,
        val imagePath: String?,
        val reportId: Int? = null
    ) : ReportUiState() // Trạng thái hiện màn hình xem trước
    data class Error(val message: String) : ReportUiState() // Trạng thái lỗi
}

class ReportViewModel(private val repository: ReportRepository) : ViewModel() {

    private val API_KEY = BuildConfig.GEMINI_API_KEY

    // Biến lưu trữ trạng thái màn hình để Compose lắng nghe
    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Idle)
    val uiState: StateFlow<ReportUiState> = _uiState

    val allReports: StateFlow<List<ReportEntity>> = repository.allReports
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 3. Hàm gọi AI (Không lưu DB ở bước này nữa)
    fun analyzeReport(originalText: String, imagePath: String?) {
        _uiState.value = ReportUiState.Loading // Bật màn hình Loading chờ AI

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val parts = mutableListOf<Part>()

                val prompt = """
                            Bạn là một trợ lý kiểm tra hiện trường chuyên nghiệp. Hãy phân tích mô tả và hình ảnh.
                            Trả về kết quả TUYỆT ĐỐI dưới định dạng JSON với các trường sau:
                            {
                                "issue": "Tên sự cố ngắn gọn",
                                "location": "Vị trí xảy ra sự cố (Ví dụ: Phòng họp A, Sảnh chính, Tầng 2... nếu không có thì ghi 'Chưa xác định')",
                                "priority": "Mức độ ưu tiên (Thấp/Trung bình/Cao)",
                                "summary": "Tóm tắt chi tiết sự việc",
                                "suggestedAction": "Hành động khắc phục đề xuất",
                                "category": "Phân loại (Ví dụ: Thiết bị, An ninh, Thời tiết...)"
                            }
                            Thông tin từ người dùng: $originalText
                            """.trimIndent()

                parts.add(Part(text = prompt))

                if (imagePath != null) {
                    val base64Image = encodeImageToBase64(imagePath)
                    if (base64Image != null) {
                        parts.add(Part(inlineData = InlineData("image/jpeg", base64Image)))
                    }
                }

                val request = GeminiRequest(
                    contents = listOf(Content(parts = parts)),
                    generationConfig = GenerationConfig(responseMimeType = "application/json")
                )

                val response = RetrofitClient.apiService.analyzeFieldReport(API_KEY, request)
                val jsonResult = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                if (jsonResult != null) {
                    // Dọn dẹp JSON phòng trường hợp AI trả về mã markdown
                    val cleanJson = jsonResult.replace("```json", "").replace("```", "").trim()
                    val aiResult = Gson().fromJson(cleanJson, AiAnalysisResult::class.java)

                    // Bắn dữ liệu sang màn hình Review để người dùng xem trước
                    _uiState.value = ReportUiState.Review(aiResult, originalText, imagePath)
                }

            } catch (e: Exception) {
                Log.e("API_ERROR", "Lỗi nguyên nhân gốc rễ là: ", e)
                e.printStackTrace()
                val offlineReport = ReportEntity(
                    originalText = originalText,
                    imagePath = imagePath,
                    category = null,
                    location = null,
                    priority = null,
                    issue = "Đang chờ phân tích...",
                    suggestedAction = null,
                    summary = null,
                    syncStatus = "PENDING" // Đánh dấu chờ đồng bộ
                )

                // 2. Lưu ngầm vào Database
                viewModelScope.launch(Dispatchers.IO) {
                    repository.insertReport(offlineReport)
                }

                // 3. Báo cho UI biết để hiển thị thông báo
                _uiState.value = ReportUiState.Error("Mất mạng hoặc máy chủ AI đang quá tải (Hết hạn mức). Báo cáo đã được lưu nháp thành công!")
            }
        }
    }
    fun retryAnalyzeReport(report: ReportEntity) {
        _uiState.value = ReportUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val parts = mutableListOf<Part>()
                val prompt = """
                Bạn là một trợ lý kiểm tra hiện trường chuyên nghiệp. Hãy phân tích mô tả và hình ảnh.
                Trả về kết quả TUYỆT ĐỐI dưới định dạng JSON với các trường sau:
                {
                    "issue": "Tên sự cố ngắn gọn",
                    "location": "Vị trí xảy ra sự cố (Ví dụ: Phòng họp A, Sảnh chính, Tầng 2... nếu không có thì ghi 'Chưa xác định')",
                    "priority": "Mức độ ưu tiên (Thấp/Trung bình/Cao)",
                    "summary": "Tóm tắt chi tiết sự việc",
                    "suggestedAction": "Hành động khắc phục đề xuất",
                    "category": "Phân loại (Ví dụ: Thiết bị, An ninh, Thời tiết...)"
                }
                Thông tin từ người dùng: ${report.originalText}
            """.trimIndent()

                parts.add(Part(text = prompt))

                if (report.imagePath != null) {
                    val base64Image = encodeImageToBase64(report.imagePath)
                    if (base64Image != null) {
                        parts.add(Part(inlineData = InlineData("image/jpeg", base64Image)))
                    }
                }

                val request = GeminiRequest(
                    contents = listOf(Content(parts = parts)),
                    generationConfig = GenerationConfig(responseMimeType = "application/json")
                )

                val response = RetrofitClient.apiService.analyzeFieldReport(API_KEY, request)
                val jsonResult = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                if (jsonResult != null) {
                    val cleanJson = jsonResult.replace("```json", "").replace("```", "").trim()
                    val aiResult = Gson().fromJson(cleanJson, AiAnalysisResult::class.java)
                    _uiState.value = ReportUiState.Review(aiResult,report.originalText , report.imagePath)
                    // 🌟 Ghi đè kết quả AI vào bản nháp và đổi trạng thái
                    _uiState.value = ReportUiState.Review(
                        aiResult = aiResult,
                        originalText = report.originalText,
                        imagePath = report.imagePath,
                        reportId = report.id
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Nếu vẫn lỗi mạng thì giữ nguyên trạng thái PENDING, không làm gì cả
                _uiState.value = ReportUiState.Error("Tải lại thất bại: Mất mạng hoặc máy chủ AI đang quá tải.")
            }
        }
    }
    // 4. Hàm lưu chính thức (Được gọi SAU KHI người dùng bấm "Xác nhận Lưu" trên màn hình Review)
    fun confirmAndSaveReport(finalResult: AiAnalysisResult, originalText: String, imagePath: String?, reportId: Int? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            if (reportId != null) {
                // NẾU TẢI LẠI: Ghi đè lên bản ghi nháp cũ bằng updateReport
                val updatedReport = ReportEntity(
                    id = reportId, // Truyền đúng ID cũ
                    originalText = originalText,
                    imagePath = imagePath,
                    category = finalResult.category,
                    location = finalResult.location ?: "Chưa xác định",
                    priority = finalResult.priority,
                    issue = finalResult.issue,
                    suggestedAction = finalResult.suggestedAction,
                    summary = finalResult.summary,
                    syncStatus = "COMPLETED"
                )
                repository.updateReport(updatedReport)
            } else {
                // NẾU TẠO MỚI HOÀN TOÀN: Thêm bản ghi mới bằng insertReport
                val finalReport = ReportEntity(
                    originalText = originalText,
                    imagePath = imagePath,
                    category = finalResult.category,
                    location = finalResult.location ?: "Chưa xác định",
                    priority = finalResult.priority,
                    issue = finalResult.issue,
                    suggestedAction = finalResult.suggestedAction,
                    summary = finalResult.summary,
                    syncStatus = "COMPLETED"
                )
                repository.insertReport(finalReport)
            }

            // Xong xuôi thì reset lại trạng thái
            _uiState.value = ReportUiState.Idle
        }
    }

    // Reset lại màn hình nếu người dùng bấm "Hủy" ở màn hình Review
    fun resetState() {
        _uiState.value = ReportUiState.Idle
    }

    private fun encodeImageToBase64(path: String): String? {
        return try {
            val bytes = File(path).readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    class Factory(private val repository: ReportRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
                return ReportViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
    fun deleteReport(report: ReportEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteReport(report)
        }
    }
    fun getReportById(id: Int): Flow<ReportEntity?> {
        return repository.getReportById(id)
    }
}