package com.example.work_calendar_app.viewmodels.components

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.work_calendar_app.data.models.WorkEntry
import com.example.work_calendar_app.data.repositories.WorkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class WorkEntriesViewModel (
    private val workRepository: WorkRepository,
    private val viewModelScope: CoroutineScope,
    private val sharedState: SharedCalendarState
){

    //Load all entries for the calendar
    fun loadWorkEntriesForCalendar(currentMonth: Int, currentYear: Int) {
        viewModelScope.launch {
            sharedState.setLoading(true)
            try {
                val workEntriesList = workRepository.getAllWorkEntries()

                //Sort the work entries by workDate in ascending order
                val filtedAndSortedWorkEntries  = workEntriesList.values
                    .filter {workEntry ->
                        val workDate = workEntry.workDate
                        //Filter entries for the specified month and year
                        workDate != null && workDate.length >= 10 &&
                                workDate.substring(0,4).toInt() == currentYear &&
                                workDate.substring(5,7).toInt() == currentMonth
                    }
                    .sortedBy { workEntry ->
                        LocalDate.parse(workEntry.workDate)
                    }

                val newWorkEntries = mutableMapOf<Long, WorkEntry>()
                val newWorkDays = mutableListOf<Int>()

                filtedAndSortedWorkEntries.forEach { workEntry ->
                    val workDate = LocalDate.parse(workEntry.workDate)
                    newWorkEntries[workEntry.id] = workEntry
                    newWorkDays.add(workDate.dayOfMonth)
                }

                //Update calendar related state variables
                sharedState.updateWorkEntries(newWorkEntries)
                sharedState.updateWorkDays(newWorkDays)
            } catch (e: Exception) {
                sharedState.setErrorMessage("Failed to load calendar entries")
            } finally {
                sharedState.setLoading(false)
            }
        }
    }

    //Function to load all work entries into details from the repository
    fun loadAllWorkEntriesDetails(currentMonth: Int, currentYear: Int) {
        viewModelScope.launch {
            sharedState.setLoading(true)
            try {
                //Fetch all work entries from the repository
                val workEntriesList = workRepository.getAllWorkEntries()

                //Sort the work entries by workDate in ascending order
                val sortedWorkEntriesList = workEntriesList.values
                    .filter {workEntry ->
                        val workDate = workEntry.workDate
                        //Filter entries for the specified month and year
                        workDate != null && workDate.length >= 10 &&
                                workDate.substring(0,4).toInt() == currentYear &&
                                workDate.substring(5,7).toInt() == currentMonth
                    }
                    .sortedBy { workEntry ->
                        LocalDate.parse(workEntry.workDate)
                    }

                val newWorkEntries = mutableMapOf<Long, WorkEntry>()
                val newWorkDays = mutableListOf<Int>()

                //Process the list of work entries
                for (workEntry in sortedWorkEntriesList) {
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
                sharedState.updateWorkDetails(newWorkEntries)
            } catch (e: Exception) {
                sharedState.setErrorMessage("Failed to load entries")
            } finally {
                sharedState.setLoading(false)
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

    //Function to get work entries between two dates for work detail list
    fun fetchWorkEntriesBetweenDates(startDate: String, endDate: String, onResult: (List<WorkEntry>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            sharedState.setLoading(true)
            try {
                val filteredEntriesMap = workRepository.getWorkEntriesBetweenDates(startDate, endDate)
                val filteredEntries = filteredEntriesMap.values.toList()
                withContext(Dispatchers.Main) {
                    sharedState.updateWorkEntries(filteredEntriesMap)
                    onResult(filteredEntries)
                }
            } catch (e: Exception) {
                sharedState.setErrorMessage("Failed to load entries")
                Log.e("WorkViewModel", "Error fetching work entries between $startDate and $endDate")
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            } finally {
                sharedState.setLoading(false)
            }
        }
    }


    //Function to get work entries in a particular month
    fun fetchWorkEntriesForMonth(month: Int, year: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            sharedState.setLoading(true)
            try {
                //Create the start and end dates for the month
                val startOfMonth = LocalDate.of(year, month, 1)
                val endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth())

                //Format dates for the query
                val formattedStartDate = startOfMonth.toString()
                val formattedEndDate = endOfMonth.toString()

                val filteredEntries = workRepository.getWorkEntriesBetweenDates(formattedStartDate, formattedEndDate)
                withContext(Dispatchers.Main) {
                    sharedState.updateWorkDetails(filteredEntries)
                }
            } catch (e: Exception) {
                sharedState.setErrorMessage("Failed to load entries")
                Log.e("WorkViewModel", "Error fetching work entries for month: $month, year: $year")
            } finally {
                sharedState.setLoading(false)
            }
        }
    }

    //Function to add or update a work entry
    fun addOrUpdateWorkEntry(workEntry: WorkEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            workRepository.addOrUpdateWorkEntry(workEntry)
            loadAllWorkEntriesDetails(sharedState.currentMonth.value.monthValue, sharedState.currentMonth.value.year) //reload the entries after updating
        }
    }

    fun saveOrUpdateWorkEntry(workEntry: WorkEntry, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            sharedState.setLoading(true)
            try {
                val isSuccess = workRepository.addOrUpdateWorkEntry(workEntry)
                withContext(Dispatchers.Main) {
                    if (isSuccess) {
                        loadAllWorkEntriesDetails(sharedState.currentMonth.value.monthValue, sharedState.currentMonth.value.year)
                        onSuccess()
                    } else {
                        onError()
                    }
                }
            } catch (e: Exception) {
                sharedState.setErrorMessage("Failed to update entry")
                Log.e("WorkViewModel", "Error saving or updating work entry: ${e.message}")
                withContext(Dispatchers.Main) {
                    onError()
                }
            } finally {
                sharedState.setLoading(false)
            }
        }
    }

    //Function to delete a work entry
    fun deleteWorkEntry(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            workRepository.deleteWorkEntry(id)
            loadAllWorkEntriesDetails(sharedState.currentMonth.value.monthValue, sharedState.currentMonth.value.year) //reload the entries after deletion
        }
    }
}