package com.example.work_calendar_app.viewmodels.components

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.work_calendar_app.data.models.Job
import com.example.work_calendar_app.data.models.WorkEntry
import com.example.work_calendar_app.data.repositories.WorkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class JobsViewModel (
    private val workRepository: WorkRepository,
    private val viewModelScope: CoroutineScope,
    private val sharedState: SharedCalendarState,
    private val loadWorkEntriesForCalendar: (Int, Int) -> Unit
) {

    //LiveData for the list of jobs
    private val _jobs = MutableLiveData<List<Job>>()
    val jobs: LiveData<List<Job>> get() = _jobs

    //Boolean to track whether a specific job is being filtered
    private val _isJobSpecificView = MutableStateFlow(false)
    val isJobSpecificView = _isJobSpecificView.asStateFlow()

    private val _selectedJobId = MutableStateFlow<Long?>(null)
    val selectedJobId = _selectedJobId.asStateFlow()

    val currentMonth = sharedState.currentMonth.value.monthValue
    val currentYear = sharedState.currentMonth.value.year


    //Toggle between showing all work Entries or job-specific entries
    fun setJobSpecificView(jobId: Long?) {
        _isJobSpecificView.value = jobId != null
        _selectedJobId.value = jobId
        if (jobId != null) {
            loadWorkEntriesByJob(currentMonth, currentYear, jobId)
        } else {
            loadWorkEntriesForCalendar(currentMonth, currentYear)
        }
    }


    //Load all of job on calendar
    fun loadWorkEntriesByJob(currentMonth: Int, currentYear: Int, jobId: Long) {
        viewModelScope.launch {
            sharedState.setLoading(true)
            try {
                val workEntriesList = workRepository.getAllWorkEntriesByJob(jobId)

                //Filter and sort work entries for the current month and year
                val newWorkEntries = mutableMapOf<Long, WorkEntry>()
                val newWorkDays = mutableListOf<Int>()
                workEntriesList.values.filter {
                    val workDate = LocalDate.parse(it.workDate)
                    workDate.monthValue == currentMonth && workDate.year == currentYear
                }.forEach {
                    newWorkEntries[it.id] = it
                    newWorkDays.add(LocalDate.parse(it.workDate).dayOfMonth)
                }

                //Update calendar related state variables
                sharedState.updateWorkEntries(newWorkEntries)
                sharedState.updateWorkDays(newWorkDays)

                //Update work details list
                sharedState.updateWorkDetails(newWorkEntries)
            } catch (e: Exception) {
                sharedState.setErrorMessage("Failed to load calendar entries")
            } finally {
                sharedState.setLoading(false)
            }
        }
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
    fun insertJob(jobName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            workRepository.insertJob(jobName)
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

    fun getJobIdForDay(day: Int): Long {
        //Find a work entry where the workDate matches the specified day with the current month
        val matchingEntry = sharedState.workEntries.value.values.find { entry ->
            val workDate = LocalDate.parse(entry.workDate)
            workDate.dayOfMonth == day && workDate.monthValue == sharedState.currentMonth.value.monthValue && workDate.year == sharedState.currentMonth.value.year
        }
        return matchingEntry?.jobId ?: -1
    }

}