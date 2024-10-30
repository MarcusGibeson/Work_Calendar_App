package com.example.work_calendar_app.viewmodels.components

import androidx.compose.runtime.mutableStateListOf
import com.example.work_calendar_app.data.models.WorkEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

class SharedCalendarState {


    //State to hold loading value
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    //Properties to hold work entries and work days for calendar
    private val _workEntries = MutableStateFlow<Map<Long, WorkEntry>>(emptyMap())
    val workEntries = _workEntries.asStateFlow()

    private val _workEntry = MutableStateFlow<WorkEntry?>(null)
    val workEntry = _workEntry.asStateFlow()

    private val _workDays = mutableStateListOf<Int>()
    val workDays: List<Int> get() = _workDays

    //Properties to hold entries for detail list
    private val _workDetailsEntries = MutableStateFlow<Map<Long, WorkEntry>>(emptyMap())
    val workDetailsEntries = _workDetailsEntries.asStateFlow()

    val currentMonth = LocalDate.now().monthValue
    val currentYear = LocalDate.now().year

    //Update functions
    fun updateWorkEntries(newEntries: Map<Long, WorkEntry>) {
        _workEntries.value = newEntries
    }

    fun updateWorkDays(newDays: List<Int>) {
        _workDays.clear()
        _workDays.addAll(newDays)
    }

    fun updateWorkDetails(newDetails: Map<Long, WorkEntry>) {
        _workDetailsEntries.value = newDetails
    }

    fun setLoading(isLoading: Boolean) {
        _loading.value = isLoading
    }

    fun setErrorMessage(message: String?) {
        _errorMessage.value = message
    }
}