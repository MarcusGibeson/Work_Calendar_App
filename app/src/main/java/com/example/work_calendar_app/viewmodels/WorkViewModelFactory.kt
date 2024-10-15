package com.example.work_calendar_app.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.work_calendar_app.data.repositories.WorkRepository

class WorkViewModelFactory(private val workRepository: WorkRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(WorkViewModel::class.java) -> {
                WorkViewModel(workRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}