package com.example.aifieldassistant.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Dữ liệu đầu vào
    val originalText: String,
    val imagePath: String?,
    val timestamp: Long = System.currentTimeMillis(),

    // Dữ liệu sau khi AI phân tích (Có thể null nếu chưa phân tích)
    val category: String?,
    val location: String?,
    val priority: String?,
    val issue: String?,
    val suggestedAction: String?,
    val summary: String?,

    // TRẠNG THÁI ĐỒNG BỘ (Cực kỳ quan trọng cho Offline Mode)
    // "PENDING": Mất mạng, đang chờ gửi lên AI/Server
    // "SYNCED": Đã xử lý thành công
    // "FAILED": Lỗi, cần thử lại
    val syncStatus: String = "PENDING"
)