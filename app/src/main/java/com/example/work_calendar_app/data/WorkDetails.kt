package com.example.work_calendar_app.data

data class WorkDetails (
    val id: Long,
    val workDate: String,
    val startTime: String,
    val endTime: String,
    val breakTime: String,
    val tips: Double,
    val wage: Double
)