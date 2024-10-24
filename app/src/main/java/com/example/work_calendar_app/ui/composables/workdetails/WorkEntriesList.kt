package com.example.work_calendar_app.ui.composables.workdetails

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.work_calendar_app.data.models.WorkEntry
import com.example.work_calendar_app.viewmodels.WorkViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Composable
fun WorkEntriesList(viewModel: WorkViewModel, workEntries: Map<Long, WorkEntry>, currentMonth: LocalDate) {
    //Dialog state to show or hide the details dialog
    var showDialog by remember { mutableStateOf(false) }
    var selectedWorkEntry by remember { mutableStateOf<WorkEntry?>(null) }

    var workEntriesChanged by remember { mutableStateOf(0) }
    var entryEdited by remember { mutableStateOf(false) }


    //Extracting the current year and month for date formatting
    val currentYear = currentMonth.year
    val currentMonthValue = currentMonth.monthValue

    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

    var baseTextColor by remember {
        mutableStateOf(Color(sharedPreferences.getInt("baseTextColor", Color.Black.toArgb())))
    }

    var detailsTextColor by remember {
        mutableStateOf(Color(sharedPreferences.getInt("detailsTextColor", Color.Black.toArgb())))
    }
    var detailsDateColor by remember {
        mutableStateOf(Color(sharedPreferences.getInt("detailsDateColor", Color.Blue.toArgb())))
    }
    var detailsWageColor by remember {
        mutableStateOf(Color(sharedPreferences.getInt("detailsWageColor", Color.Red.toArgb())))
    }


    //Refresh on color preference change
    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                "baseTextColor" -> {
                    baseTextColor = Color(sharedPreferences.getInt("baseTextColor", Color.Black.toArgb()))
                }
                "detailsTextColor" -> {
                    detailsTextColor = Color(sharedPreferences.getInt("detailsTextColor", Color.Black.toArgb()))
                }
                "detailsDateColor" -> {
                    detailsDateColor = Color(sharedPreferences.getInt("detailsDateColor", Color.Blue.toArgb()))
                }
                "detailsWageColor" -> {
                    detailsWageColor = Color(sharedPreferences.getInt("detailsWageColor", Color.Red.toArgb()))
                }
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        //Clean up listener when composable leaves the composition
        onDispose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    fun refreshWorkEntries() {
        workEntriesChanged++
    }

    fun onAddOrUpdateOrDeleteEntry() {
        refreshWorkEntries()
        entryEdited = true
    }

    // Log current month and year
    Log.d("WorkDetailsList", "Current year: $currentYear, Current month: $currentMonthValue")


    //Function to update the workEntries when saving a new or modified entry
    val onSaveEntry: (WorkEntry) -> Unit ={ updatedEntry ->
        Log.d("onSave", "updatedEntry.id: ${updatedEntry.id}")
        Log.d("onSave", "updatedEntry.workDate: ${updatedEntry.workDate}")
        Log.d("onSave", "updatedEntry.startTime: ${updatedEntry.startTime}")
        Log.d("onSave", "updatedEntry.endTime: ${updatedEntry.endTime}")
        Log.d("onSave", "updatedEntry.breakTime: ${updatedEntry.breakTime}")
        Log.d("onSave", "updatedEntry.payType: ${updatedEntry.payType}")
        Log.d("onSave", "updatedEntry.payRate: ${updatedEntry.payRate}")
        Log.d("onSave", "updatedEntry.overtimeRate: ${updatedEntry.overtimeRate}")
        Log.d("onSave", "updatedEntry.salaryAmount: ${updatedEntry.salaryAmount}")
        Log.d("onSave", "updatedEntry.commissionRate: ${updatedEntry.commissionRate}")
        Log.d("onSave", "updatedEntry.commissionDetails: ${updatedEntry.commissionDetails}")
        Log.d("onSave", "updatedEntry.tips: ${updatedEntry.tips}")
        viewModel.saveOrUpdateWorkEntry(updatedEntry, onSuccess = {
            showDialog = false
            onAddOrUpdateOrDeleteEntry()
            Log.d("onSave", "Successfully updated entry")
        }, onError = {
            Log.e("onSave", "Failed to update entry")
        })
    }

    val onDeleteEntry: () -> Unit = {
        selectedWorkEntry?.let { entryToDelete ->
            viewModel.deleteWorkEntry(entryToDelete.id)
            showDialog = false
            Log.d("WorkEntries", "Successfully deleted entry")
            onAddOrUpdateOrDeleteEntry()
        } ?: Log.e("WorkEntries", "Failed to delete entry")
    }

    //Generate work entries for the current month
    val workEntryList: List<WorkEntry> = workEntries.mapNotNull { (id, workEntry) ->
        try {
            val workDate = workEntry.workDate
            val day = workDate.substring(8, 10).toInt()
            Log.d("WorkDetailsList", "Checking workEntry: ID: $id, Date: $workDate")

            //Create a Localdate for the current year, current month, and specific day
            val workDateLocal = LocalDate.parse(workEntry.workDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val formattedWorkDate = workDateLocal.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
            Log.d("WorkDetailsList", "Valid workEntry: ID: $id, Date: $workDateLocal")
            Log.d("WorkDetailsList", "Verifying date: ID: $id, Date: $formattedWorkDate")
            WorkEntry(
                id = workEntry.id,
                jobId = workEntry.jobId,
                workDate = formattedWorkDate,
                startTime = workEntry.startTime,
                endTime = workEntry.endTime,
                breakTime = workEntry.breakTime,
                payType = workEntry.payType,
                payRate = workEntry.payRate,
                overtimeRate = workEntry.overtimeRate,
                commissionRate = workEntry.commissionRate,
                totalCommissionAmount = workEntry.totalCommissionAmount,
                commissionDetails = workEntry.commissionDetails,
                salaryAmount = workEntry.salaryAmount,
                dailySalary = workEntry.dailySalary,
                tips = workEntry.tips,
                netEarnings = workEntry.netEarnings
            ).also { Log.d("WorkDetailsList", "Created WorkDetails: ID: $id, Date: $workDateLocal") }
        } catch (e: Exception) {
            Log.e("WorkEntryList", "Error parsing work entry date for ID: $id")
            null
        }
    }

    //Calculate total wage
    val totalWage = workEntryList.sumOf {it.netEarnings}

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(end = 8.dp) //padding for scrollbar
        ) {
            items(workEntryList) { workEntry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(75.dp)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${workEntry.workDate}:",
                            modifier = Modifier.weight(1f),
                            color = detailsDateColor
                        )
                        Text(
                            text = " ${workEntry.startTime} - ${workEntry.endTime}",
                            color = detailsTextColor
                        )
                    }
                    Text(
                        text = "$${workEntry.netEarnings}",
                        modifier = Modifier.padding(start = 8.dp, end = 16.dp),
                        color = detailsWageColor
                    )

                    Text(
                        text = "Details",
                        modifier = Modifier
                            .clickable {
                                val workDateKey = workEntry.id
                                selectedWorkEntry = workEntries[workDateKey]
                                showDialog = true
                                // Log when a work entry is selected for details
                                Log.d("WorkDetailsList", "Selected WorkEntry ID: $workDateKey for details")
                            },
                        color = detailsTextColor
                    )
                }
            }
        }

        Row (
            modifier = Modifier
                .align(Alignment.End)
                .padding(16.dp)
                .border(
                    width = 2.dp,
                    color = detailsDateColor,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(8.dp)
        ){

            Text(
                text = "Total Earned: ",
                style = MaterialTheme.typography.headlineMedium,
                color = detailsTextColor
            )

            //Display total wage amount
            Text(
                text = "$${String.format("%.2f", totalWage)}",
                style = MaterialTheme.typography.headlineMedium,
                color = detailsWageColor
            )
        }



    }

    //Show Dialog if the state is true
    if (showDialog && selectedWorkEntry != null) {
        Log.d("WorkDetailsList", "Showing dialog for selected entry ID: ${selectedWorkEntry!!.id}")
        WorkDetailsPopUp(
            viewModel,
            workEntry = selectedWorkEntry!!,
            onClose = { showDialog = false },
            onSave = onSaveEntry,
            onDelete = onDeleteEntry
        )
    }



}
