package com.example.aifieldassistant.domain.model

data class MockReport(
    val id: String,
    val title: String,
    val location: String,
    val priority: String,
    val date: String,
    val isSynced: Boolean
)

// Dữ liệu giả định
val dummyReports = listOf(
    MockReport("1", "Điều hòa hỏng", "Khu vực Lễ tân", "High", "25/09/2026 - 10:30", true),
    MockReport("2", "Rò rỉ ống nước", "Nhà vệ sinh tầng 2", "Medium", "25/09/2026 - 11:15", false),
    MockReport("3", "Đèn hành lang tắt", "Hành lang khu A", "Low", "24/09/2026 - 16:45", true)
)