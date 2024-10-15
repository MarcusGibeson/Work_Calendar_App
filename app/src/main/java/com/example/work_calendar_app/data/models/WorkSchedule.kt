package com.example.work_calendar_app.data.models

data class WorkSchedule (
    val id: Int,
    val workDate: String,
    val startTime: String,
    val endTime: String,
    val breakTimeMinutes: Int,
    val payType: String,
    val payRate: Double,
    val overtimeRate: Double,
    val totalEarnings: Double

)