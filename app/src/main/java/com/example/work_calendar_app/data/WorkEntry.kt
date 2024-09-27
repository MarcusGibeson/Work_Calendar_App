package com.example.work_calendar_app.data

data class WorkEntry(
    val id: Long,
    val workDate: String,
    val startTime: String,
    val endTime: String,
    val breakTime: Int,
    val payType: String,
    val payRate: Double,
    val overtimeRate: Double,
    val netEarnings: Double
)
