package com.example.work_calendar_app.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.work_calendar_app.data.models.WorkEntry
import com.example.work_calendar_app.data.repositories.WorkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class WorkViewModel (private val workRepository: WorkRepository) : ViewModel() {

    //State to hold the currently selected date
    private val _selectedDate = MutableStateFlow<Long?>(null)
    val selectedDate = _selectedDate.asStateFlow()

    //Properties to hold work entries and work days
    private val _workEntries = mutableStateMapOf<Long, WorkEntry>()
    val workEntries: Map<Long, WorkEntry> get() = _workEntries

    private val _workDays = mutableStateListOf<Int>()
    val workDays: List<Int> get() = _workDays

    val currentMonth = LocalDate.now().monthValue
    val currentYear = LocalDate.now().year
    init {
        //Load all work entries when the ViewModel is created

        loadAllWorkEntries(currentMonth, currentYear)
    }

    //Function to load all work entries from the repository
    private fun loadAllWorkEntries(currentMonth: Int, currentYear: Int) {
       viewModelScope.launch {
           //Clear existing data
           _workEntries.clear()
           _workDays.clear()

           //Fetch all work entries from the repository
           val workEntriesList = workRepository.getAllWorkEntries()

           //Process the list of work entries
           for (workEntry in workEntriesList) {
               val workDate = workEntry.workDate
               if (workDate != null && workDate.length >= 10) {
                   val workYear = workDate.substring(0, 4).toInt()
                   val workMonth = workDate.substring(5, 7).toInt()
                   val workDay = workDate.substring(8, 10).toInt()

                   //Check if the workDate belong to the current month and year in calendar
                   if (workMonth == currentMonth && workYear == currentYear) {
                       //Add the work entry to the list only if it's in the current month and year
                       _workEntries[workEntry.id] = workEntry
                       _workDays.add(workDay)
                   }
               } else {
                   Log.e("WorkViewModel", "Invalid work date format: $workDate")
               }
           }
    }

    //Function to add or update a work entry
    fun addOrUpdateWorkEntry(workEntry: WorkEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            workRepository.addOrUpdateWorkEntry(workEntry)
            loadAllWorkEntries(currentMonth, currentYear) //reload the entries after updating
        }
    }

    //Function to delete a work entry
    fun deleteWorkEntry(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            workRepository.deleteWorkEntry(id)
            loadAllWorkEntries(currentMonth, currentYear) //reload the entries after deletion
        }
    }

    //Function to set the selected date
    fun selectDate(date: Long) {
        _selectedDate.value = date
    }

    //Function to get work entries between two dates
    fun fetchWorkEntriesBetweenDates(startDate: String, endDate: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val filteredEntries = workRepository.getWorkEntriesBetweenDates(startDate, endDate)
            _workEntries.value = filteredEntries
        }
    }

}