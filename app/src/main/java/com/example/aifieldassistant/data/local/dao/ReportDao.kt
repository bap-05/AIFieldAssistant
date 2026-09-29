package com.example.aifieldassistant.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.aifieldassistant.data.local.entity.ReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    // 1. Thêm báo cáo mới vào DB
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity): Long

    // 2. Cập nhật báo cáo (ví dụ: đổi trạng thái từ PENDING sang SYNCED)
    @Update
    suspend fun updateReport(report: ReportEntity)

    // 3. Lấy toàn bộ lịch sử báo cáo để hiển thị lên màn hình chính (HistoryScreen)
    // Dùng Flow để giao diện tự động cập nhật khi DB thay đổi
    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    // 4. Lấy các báo cáo đang chờ đồng bộ khi thiết bị ngoại tuyến
    @Query("SELECT * FROM reports WHERE syncStatus = 'PENDING'")
    suspend fun getPendingReports(): List<ReportEntity>
    @Query("SELECT * FROM reports WHERE id = :id") fun getReportById(id: Int): Flow<ReportEntity?>
    @Delete
    suspend fun deleteReport(report: ReportEntity)
}