package com.example.aifieldassistant.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aifieldassistant.data.local.entity.ReportEntity
import com.example.aifieldassistant.data.repository.ReportRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReportViewModel(private val repository: ReportRepository) : ViewModel() {

    // Chuyển đổi Flow từ Room thành StateFlow để Jetpack Compose dễ dàng lắng nghe
    val allReports: StateFlow<List<ReportEntity>> = repository.allReports
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Hàm gọi khi nhân viên bấm "AI Phân tích & Tạo báo cáo"
    fun saveDraftReport(originalText: String, imagePath: String?) {
        viewModelScope.launch {
            val newReport = ReportEntity(
                originalText = originalText,
                imagePath = imagePath,
                category = null,
                location = null,
                priority = null,
                issue = null,
                suggestedAction = null,
                summary = null,
                syncStatus = "PENDING" // Đánh dấu là chưa gửi cho AI
            )
            repository.insertReport(newReport)

            // TODO: Ngay sau khi lưu nháp, chúng ta sẽ gọi Gemini API ở đây
            // hoặc kích hoạt WorkManager để chạy ngầm.
        }
    }

    // Factory giúp khởi tạo ViewModel có chứa tham số Repository
    class Factory(private val repository: ReportRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
                return ReportViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}