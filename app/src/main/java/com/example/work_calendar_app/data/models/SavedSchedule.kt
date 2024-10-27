package com.example.work_calendar_app.data.models

data class SavedSchedule(
    val id: Long,
    val jobId: Long,
    val scheduleName: String,
    val startTime: String,
    val endTime: String,
    val breakTime: Int,
    val payType: String,
    val hourlyRate: Double,
    val overtimeRate: Double,
    val commissionRate: Int,
    val salaryAmount: Double
)
