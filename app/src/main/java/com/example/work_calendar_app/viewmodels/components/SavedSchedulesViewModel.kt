package com.example.work_calendar_app.viewmodels.components

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.work_calendar_app.data.models.SavedSchedule
import com.example.work_calendar_app.data.repositories.WorkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SavedSchedulesViewModel (private val workRepository: WorkRepository, private val viewModelScope: CoroutineScope, private val sharedState: SharedCalendarState) {

    //LiveData for the list of saved schedules
    private val _savedSchedules = MutableLiveData<List<SavedSchedule>>()
    val savedSchedules: LiveData<List<SavedSchedule>> get() = _savedSchedules

    //LiveData to hold a saved schedule detail
    private val _savedSchedule = MutableLiveData<SavedSchedule?>()
    val savedSchedule: LiveData<SavedSchedule?> get() = _savedSchedule

    fun insertSavedSchedule(jobId: Long, scheduleName: String, startTime: String, endTime: String, breakTime: Int, payType: String, hourlyRate: Double, overtimePay: Double, commissionRate: Int, salaryAmount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                workRepository.insertSavedSchedule(jobId, scheduleName, startTime, endTime, breakTime, payType, hourlyRate, overtimePay, commissionRate, salaryAmount)
            } catch (e: Exception) {
                Log.e("WorkViewModel", "Failed to insert saved schedule: ${e.message}")
            }
        }
    }

    fun getAllSavedSchedules() {
        viewModelScope.launch {
            try {
                val schedules = workRepository.getAllSavedSchedules()
                Log.d("WorkViewModel", "Fetched saved schedules: ${schedules.joinToString()}")
                _savedSchedules.value = schedules
            } catch (e: Exception) {
                Log.e("WorkViewModel", "Failed to load saved schedules: ${e.message}")
            }
        }
    }

    fun getSavedSchedule(scheduleName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val savedSchedule = workRepository.getSavedScheduleByName(scheduleName)
                withContext(Dispatchers.Main) {
                    _savedSchedule.value = savedSchedule
                }
            } catch (e: Exception) {
                Log.e("WorkViewModel", "Error getting saved schedule for: $scheduleName", e)
                _savedSchedule.postValue(null)
            }
        }
    }

}