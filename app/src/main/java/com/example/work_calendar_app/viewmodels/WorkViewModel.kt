package com.example.work_calendar_app.viewmodels

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.work_calendar_app.data.models.WorkEntry
import com.example.work_calendar_app.data.repositories.WorkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class WorkViewModel (private val workRepository: WorkRepository) : ViewModel() {

    //State to hold the currently selected date
    private val _selectedDate = MutableStateFlow<Long?>(null)
    val selectedDate = _selectedDate.asStateFlow()

    //Properties to hold work entries and work days
    private val _workEntries = MutableStateFlow<Map<Long, WorkEntry>>(emptyMap())
    val workEntries = _workEntries.asStateFlow()

    private val _workEntry = MutableLiveData<WorkEntry>()

    private val _workDays = mutableStateListOf<Int>()
    val workDays: List<Int> get() = _workDays

    val currentMonth = LocalDate.now().monthValue
    val currentYear = LocalDate.now().year

    init {
        //Load all work entries when the ViewModel is created

        loadAllWorkEntries(currentMonth, currentYear)
    }

    //Function to load all work entries from the repository
    fun loadAllWorkEntries(currentMonth: Int, currentYear: Int) {
       viewModelScope.launch {
           //Fetch all work entries from the repository
           val workEntriesList = workRepository.getAllWorkEntries()

           val newWorkEntries = mutableMapOf<Long, WorkEntry>()
           val newWorkDays = mutableListOf<Int>()

           //Process the list of work entries
           for ((_, workEntry) in workEntriesList) {
               val workDate = workEntry.workDate
               if (workDate != null && workDate.length >= 10) {
                   val workYear = workDate.substring(0, 4).toInt()
                   val workMonth = workDate.substring(5, 7).toInt()
                   val workDay = workDate.substring(8, 10).toInt()

                   //Check if the workDate belong to the current month and year in calendar
                   if (workMonth == currentMonth && workYear == currentYear) {
                       //Add the work entry to the list only if it's in the current month and year
                       newWorkEntries[workEntry.id] = workEntry
                       newWorkDays.add(workDay)
                   }
               } else {
                   Log.e("WorkViewModel", "Invalid work date format: $workDate")
               }
           }

           //Update the state
           _workEntries.value = newWorkEntries
           _workDays.clear()
           _workDays.addAll(newWorkDays)
       }
    }

    //Function to load a specific work entry based on date
    fun fetchWorkEntryForDate(date: String) {
        viewModelScope.launch {
            //Call the repository to get the work entry for the specific date
            val workEntry = workRepository.getWorkEntryForDate(date)
            _workEntry.postValue(workEntry)
        }
    }

    //Function to get work entries between two dates
    fun fetchWorkEntriesBetweenDates(startDate: String, endDate: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val filteredEntries = workRepository.getWorkEntriesBetweenDates(startDate, endDate)
                withContext(Dispatchers.Main) {
                    _workEntries.value = filteredEntries
                }
            } catch (e: Exception) {
                Log.e("WorkViewModel", "Error fetching work entries between $startDate and $endDate")
            }
        }
    }

    //Function to get work entries in a particular month
    fun fetchWorkEntriesForMonth(month: Int, year: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                //Create the start and end dates for the month
                val startOfMonth = LocalDate.of(year, month, 1)
                val endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth())

                //Format dates for the query
                val formattedStartDate = startOfMonth.toString()
                val formattedEndDate = endOfMonth.toString()

                val filteredEntries = workRepository.getWorkEntriesBetweenDates(formattedStartDate, formattedEndDate)
                withContext(Dispatchers.Main) {
                    _workEntries.value = filteredEntries
                }
            } catch (e: Exception) {
                Log.e("WorkViewModel", "Error fetching work entries for month: $month, year: $year")
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

    fun saveOrUpdateWorkEntry(workEntry: WorkEntry, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val isSuccess = workRepository.addOrUpdateWorkEntry(workEntry)
                withContext(Dispatchers.Main) {
                    if (isSuccess) {
                        onSuccess()
                    } else {
                        onError()
                    }
                }
            } catch (e: Exception) {
                Log.e("WorkViewModel", "Error saving or updating work entry: ${e.message}")
                withContext(Dispatchers.Main) {
                    onError()
                }
            }
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


}