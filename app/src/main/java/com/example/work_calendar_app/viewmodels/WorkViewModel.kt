package com.example.work_calendar_app.viewmodels

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.work_calendar_app.data.models.Job
import com.example.work_calendar_app.data.models.WorkEntry
import com.example.work_calendar_app.data.repositories.WorkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate


class WorkViewModel (private val workRepository: WorkRepository) : ViewModel() {

    //LiveData for the list of jobs
    private val _jobs = MutableLiveData<List<Job>>()
    val jobs: LiveData<List<Job>> get() = _jobs

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

    //State to hold loading value
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    //Properties to hold work entries and work days
    private val _workEntries = MutableStateFlow<Map<Long, WorkEntry>>(emptyMap())
    val workEntries = _workEntries.asStateFlow()

    private val _workEntry = MutableStateFlow<WorkEntry?>(null)
    val workEntry = _workEntry.asStateFlow()

    private val _workDays = mutableStateListOf<Int>()
    val workDays: List<Int> get() = _workDays

    val currentMonth = LocalDate.now().monthValue
    val currentYear = LocalDate.now().year

    init {
        //Load all work entries when the ViewModel is created

        loadAllWorkEntries(currentMonth, currentYear)
    }

    //Set selected dates
    fun setFirstSelectedDate(date: String?) {
        _firstSelectedDate.value = date
    }

    fun setSecondSelectedDate(date: String?) {
        _secondSelectedDate.value = date
    }

    //Function to load all work entries from the repository
    fun loadAllWorkEntries(currentMonth: Int, currentYear: Int) {
       viewModelScope.launch {
           _loading.value = true
           try {
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
               Log.d("WorkViewModel", "Work days added: ${_workDays.joinToString()}")
           } catch (e: Exception) {
               _errorMessage.value = "Failed to load entries"
           } finally {
               _loading.value = false
           }

       }
    }

    //Function to load a specific work entry based on date
    fun getWorkEntryForDate(formattedDate: String, onResult: (WorkEntry?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val workEntry = workRepository.getWorkEntryForDate(formattedDate)
                withContext(Dispatchers.Main) {
                    onResult(workEntry)
                }
            } catch (e: Exception) {
                Log.e("WorkViewModel", "Error fetching work entry for date: $formattedDate", e)
                withContext(Dispatchers.Main) {
                    onResult(null)
                }
            }
        }
    }

    //Function to get work entries between two dates
    fun fetchWorkEntriesBetweenDates(startDate: String, endDate: String, onResult: (List<WorkEntry>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.value = true
            try {
                val filteredEntriesMap = workRepository.getWorkEntriesBetweenDates(startDate, endDate)
                val filteredEntries = filteredEntriesMap.values.toList()
                withContext(Dispatchers.Main) {
                    _workEntries.value = filteredEntriesMap
                    onResult(filteredEntries)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load entries"
                Log.e("WorkViewModel", "Error fetching work entries between $startDate and $endDate")
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            } finally {
                _loading.value = false
            }
        }
    }


    //Function to get work entries in a particular month
    fun fetchWorkEntriesForMonth(month: Int, year: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.value = true
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
                _errorMessage.value = "Failed to load entries"
                Log.e("WorkViewModel", "Error fetching work entries for month: $month, year: $year")
            } finally {
                _loading.value = false
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
            _loading.value = true
            try {
                val isSuccess = workRepository.addOrUpdateWorkEntry(workEntry)
                withContext(Dispatchers.Main) {
                    if (isSuccess) {
                        loadAllWorkEntries(currentMonth, currentYear)
                        onSuccess()
                    } else {
                        onError()
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update entry"
                Log.e("WorkViewModel", "Error saving or updating work entry: ${e.message}")
                withContext(Dispatchers.Main) {
                    onError()
                }
            } finally {
             _loading.value = false
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


    //Function to toggle selecting range / single day
    fun toggleRangeSelection() {
        _isSelectingRange.value = !(_isSelectingRange.value ?: false)
    }

    //Function to load all jobs from the repository
    fun loadAllJobs() {
        viewModelScope.launch(Dispatchers.IO) {
            val jobList = workRepository.getAllJobs()
            withContext(Dispatchers.Main) {
                _jobs.value = jobList
            }
        }
    }

    //Function to insert a new job
    fun insertJob(job: Job) {
        viewModelScope.launch(Dispatchers.IO) {
            workRepository.insertJob(job)
            loadAllJobs()
        }
    }

    //Function to delete a job by its Id
    fun deleteJob(jobId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            workRepository.deleteJob(jobId)
            loadAllJobs()
        }
    }
}