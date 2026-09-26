package com.example.aifieldassistant.data.repository

import com.example.aifieldassistant.data.local.dao.ReportDao
import com.example.aifieldassistant.data.local.entity.ReportEntity
import kotlinx.coroutines.flow.Flow

class ReportRepository(private val reportDao: ReportDao) {

    // Luồng dữ liệu tự động cập nhật cho màn hình Lịch sử
    val allReports: Flow<List<ReportEntity>> = reportDao.getAllReports()

    suspend fun insertReport(report: ReportEntity): Long {
        return reportDao.insertReport(report)
    }

    suspend fun updateReport(report: ReportEntity) {
        reportDao.updateReport(report)
    }

    suspend fun getPendingReports(): List<ReportEntity> {
        return reportDao.getPendingReports()
    }
}