package com.example.work_calendar_app.viewmodels

import android.content.SharedPreferences
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.work_calendar_app.data.models.Job
import com.example.work_calendar_app.data.models.SavedSchedule
import com.example.work_calendar_app.data.models.WorkEntry
import com.example.work_calendar_app.data.repositories.WorkRepository
import com.example.work_calendar_app.viewmodels.components.JobsViewModel
import com.example.work_calendar_app.viewmodels.components.SavedSchedulesViewModel
import com.example.work_calendar_app.viewmodels.components.SharedCalendarState
import com.example.work_calendar_app.viewmodels.components.WorkEntriesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import java.time.LocalDate


class WorkViewModel (private val workRepository: WorkRepository) : ViewModel() {

    private val sharedState = SharedCalendarState()

    val jobsViewModel = JobsViewModel(workRepository, viewModelScope, sharedState, ::loadWorkEntriesForCalendar)
    val workEntriesModel = WorkEntriesViewModel(workRepository, viewModelScope, sharedState)
    val savedSchedulesModel = SavedSchedulesViewModel(workRepository, viewModelScope, sharedState)

    //Access the shared state
    val currentMonth = sharedState.currentMonth
    val workEntries = sharedState.workEntries
    val workDays = sharedState.workDays
    val loading = sharedState.loading
    val errorMessage = sharedState.errorMessage

    val jobs = jobsViewModel.jobs
    val selectedJobId = jobsViewModel.selectedJobId
    val savedSchedules = savedSchedulesModel.savedSchedules
    val savedSchedule = savedSchedulesModel.savedSchedule

    //State to toggle selecting range
    private val _isSelectingRange = MutableLiveData(false)
    val isSelectingRange: LiveData<Boolean> get() = _isSelectingRange

    //States for storing selected date range
    private val _firstSelectedDate = MutableLiveData<String?>()
    val firstSelectedDate: LiveData<String?> get() = _firstSelectedDate

    private val _secondSelectedDate = MutableLiveData<String?>()
    val secondSelectedDate: LiveData<String?> get() = _secondSelectedDate

    //State to hold the currently selected date
    private val _selectedDate = MutableStateFlow<Long?>(null)
    val selectedDate = _selectedDate.asStateFlow()

    private val _isDataLoaded = MutableStateFlow(false)
    val isDataLoaded = _isDataLoaded.asStateFlow()

    private val _jobColorMap = MutableStateFlow<Map<Long, Color>>(emptyMap())
    val jobColorMap: StateFlow<Map<Long, Color>> get() = _jobColorMap

    var selectedPayType by mutableStateOf("None")

    var hourlyRate by mutableStateOf("0")
    var overtimeRate by mutableStateOf("0")
    var tips by mutableStateOf("0")
    var yearlySalary by mutableStateOf("0")
    var commissionRate by mutableStateOf("0")
    var sales = mutableStateListOf<String>()

    init {
        //Load all work entries when the ViewModel is created
        loadWorkEntriesForCalendar(currentMonth.value.monthValue, currentMonth.value.year)
//        loadAllWorkEntriesDetails(currentMonth.value.monthValue, currentMonth.value.year)

        //Logging to verify initial state
        viewModelScope.launch {
            sharedState.workEntries.collect { entries ->
                Log.d("WorkViewModel", "Initial workEntries: ${entries.values.joinToString()}")
            }
        }

        viewModelScope.launch {
            sharedState.workDays.collect { days ->
                Log.d("WorkViewModel", "Initial workDays: $days")
            }
        }
    }

    //Set selected dates
    fun setFirstSelectedDate(date: String?) {
        _firstSelectedDate.value = date
    }

    fun setSecondSelectedDate(date: String?) {
        _secondSelectedDate.value = date
    }

    //Function to toggle selecting range / single day
    fun toggleRangeSelection() {
        _isSelectingRange.value = !(_isSelectingRange.value ?: false)
    }

    fun incrementMonth() {
        val newMonth = sharedState.currentMonth.value.plusMonths(1)
        sharedState.setCurrentMonth(newMonth)
        fetchWorkEntriesForMonth(newMonth.monthValue, newMonth.year)
    }

    fun decrementMonth() {
        val newMonth = sharedState.currentMonth.value.minusMonths(1)
        sharedState.setCurrentMonth(newMonth)
        fetchWorkEntriesForMonth(newMonth.monthValue, newMonth.year)
    }

    fun checkDataLoaded() {
        _isDataLoaded.value = workDays.value.isNotEmpty() && jobColorMap.value.isNotEmpty()
    }

    fun setJobColors(colors: Map<Long, Color>) {
        _jobColorMap.value = colors
    }

    fun loadJobColors(jobs: List<Job>, sharedPreferences: SharedPreferences) {
        val colorList = listOf(
            Color(sharedPreferences.getInt("workDay1Color", Color.Yellow.toArgb())),
            Color(sharedPreferences.getInt("workDay2Color", Color.Yellow.toArgb())),
            Color(sharedPreferences.getInt("workDay3Color", Color.Yellow.toArgb())),
            Color(sharedPreferences.getInt("workDay4Color", Color.Green.toArgb()))
        )

        val jobColors = jobs.take(colorList.size).associate { job ->
            job.id to colorList[jobs.indexOf(job)]
        }

        setJobColors(jobColors)
    }


    //Delegated JobsViewModel functions
    fun loadWorkEntriesByJob(currentMonth: Int, currentYear: Int, jobId: Long) {
        jobsViewModel.loadWorkEntriesByJob(currentMonth, currentYear, jobId)
    }

    fun setJobSpecificView(jobId: Long?) {
        jobsViewModel.setJobSpecificView(jobId)
    }

    fun loadAllJobs() {
        jobsViewModel.loadAllJobs()
    }

    fun insertJob(jobName: String) {
        jobsViewModel.insertJob(jobName)
    }

    fun deleteJob(jobId: Long) {
        jobsViewModel.deleteJob(jobId)
    }

    fun getJobIdForDay(day: Int): Long {
        return jobsViewModel.getJobIdForDay(day)
    }


    //Delegated WorkEntriesViewModel functions
    fun loadWorkEntriesForCalendar(currentMonth: Int, currentYear: Int) {
        workEntriesModel.loadWorkEntriesForCalendar(currentMonth, currentYear)
    }

    fun loadAllWorkEntriesDetails(currentMonth: Int, currentYear: Int) {
        workEntriesModel.loadAllWorkEntriesDetails(currentMonth, currentYear)
    }

    fun getWorkEntryForDate(formattedDate: String, onResult: (WorkEntry?) -> Unit) {
        workEntriesModel.getWorkEntryForDate(formattedDate, onResult)
    }

    fun fetchWorkEntriesBetweenDates(startDate: String, endDate: String, onResult: (List<WorkEntry>) -> Unit) {
        workEntriesModel.fetchWorkEntriesBetweenDates(startDate, endDate, onResult)
    }

    fun fetchWorkEntriesForMonth(month: Int, year: Int) {
        workEntriesModel.fetchWorkEntriesForMonth(month, year)
    }

    fun addOrUpdateWorkEntry(workEntry: WorkEntry) {
        workEntriesModel.addOrUpdateWorkEntry(workEntry)
    }

    fun saveOrUpdateWorkEntry(workEntry: WorkEntry, onSuccess: () -> Unit, onError: () -> Unit) {
        workEntriesModel.saveOrUpdateWorkEntry(workEntry, onSuccess, onError)
    }

    fun deleteWorkEntry(id: Long) {
        workEntriesModel.deleteWorkEntry(id)
    }

    //Delegate SavedSchedulesViewModel functions
    fun insertSavedSchedule(jobId: Long, scheduleName: String, startTime: String, endTime: String, breakTime: Int, payType: String, hourlyRate: Double, overtimePay: Double, commissionRate: Int, salaryAmount: Double) {
        savedSchedulesModel.insertSavedSchedule(jobId, scheduleName, startTime, endTime, breakTime, payType, hourlyRate, overtimePay, commissionRate, salaryAmount)
    }

    fun getAllSavedSchedules() {
        savedSchedulesModel.getAllSavedSchedules()
    }

    fun getSavedSchedule(scheduleName: String) {
        savedSchedulesModel.getSavedSchedule(scheduleName)
    }
}