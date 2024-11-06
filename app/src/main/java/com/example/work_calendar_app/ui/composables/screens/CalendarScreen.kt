package com.example.work_calendar_app.ui.composables.screens

import JobSelectionBar
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.work_calendar_app.R
import com.example.work_calendar_app.UserSettingsActivity
import com.example.work_calendar_app.data.models.WorkEntry
import com.example.work_calendar_app.ui.composables.calendar.CalendarContent
import com.example.work_calendar_app.ui.composables.components.JobManagementDialog
import com.example.work_calendar_app.viewmodels.WorkViewModel
import java.time.LocalDate

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: WorkViewModel, launchAddWorkActivity: (Int?, Int?, Int?) -> Unit) {

    Log.d("Recomposition", "CalendarScreen recomposed")

    val jobs by viewModel.jobs.observeAsState(emptyList())
    val selectedJobId by viewModel.selectedJobId.collectAsState()
    val workDays by viewModel.workDays.collectAsState()
    val workEntries by viewModel.workEntries.collectAsState()

    var showJobDialog by remember { mutableStateOf(false) }
    val isJobLimitReached = jobs.size >= 4

    var entryEdited by remember { mutableStateOf(false) }
    var workEntriesChanged by remember { mutableIntStateOf(0) }
    var selectedDay by remember { mutableIntStateOf(-1) }
    var firstSelectedDate: String? by remember { mutableStateOf(null) }
    var secondSelectedDate: String? by remember { mutableStateOf(null) }

    val isDataLoaded by viewModel.isDataLoaded.collectAsState()

    val jobColorMap by viewModel.jobColorMap.collectAsState()


    var currentMonth by remember { mutableStateOf(LocalDate.now()) }
    val context = LocalContext.current
    val sharedPreferences =
        context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

    var topBarColor by remember { mutableStateOf(Color(sharedPreferences.getInt("topBarColor", Color.Blue.toArgb()))) }

    var baseTextColor by remember { mutableStateOf(Color(sharedPreferences.getInt("baseTextColor", Color.Black.toArgb()))) }

    var workDay1Color by remember { mutableStateOf(Color(sharedPreferences.getInt("workDay1Color", Color.Yellow.toArgb()))) }
    var workDay2Color by remember { mutableStateOf(Color(sharedPreferences.getInt("workDay2Color", Color.Yellow.toArgb()))) }
    var workDay3Color by remember { mutableStateOf(Color(sharedPreferences.getInt("workDay3Color", Color.Yellow.toArgb()))) }
    var workDay4Color by remember { mutableStateOf(Color(sharedPreferences.getInt("workDay4Color", Color.Green.toArgb()))) }


    //Use isSelectingRange from the viewModel
    val isSelectingRange = viewModel.isSelectingRange.observeAsState(initial = false)
    val updatedIsSelectingRange by rememberUpdatedState(isSelectingRange)

    //Function to toggle selecting range using viewModel
    val onToggleSelectingRange = {
        viewModel.toggleRangeSelection()
        //Reset other states
        viewModel.setFirstSelectedDate(null)
        viewModel.setSecondSelectedDate(null)
        selectedDay = -1
    }


    fun refreshWorkEntries() {
        workEntriesChanged++
    }

    fun onAddOrUpdateOrDeleteEntry() {
        refreshWorkEntries()
        entryEdited = true
    }



    //Load colors from shared preferences
    LaunchedEffect(jobs) {
        viewModel.loadJobColors(jobs, sharedPreferences)
        Log.d("CalendarScreen", "Job colors updated: $jobColorMap")
    }

    //Fetch jobs when the screen is composed
    LaunchedEffect(Unit) {
        viewModel.loadAllJobs()
    }

    //Log to monitor selecting range change
    LaunchedEffect(isSelectingRange) {
        Log.d("CalendarScreen", "isSelectingRange toggled to: $isSelectingRange.value")
    }

    //Whenever the screen is recomposed, ensure it checks if the preferences have changed
    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                "topBarColor" -> {
                    topBarColor = Color(sharedPreferences.getInt("topBarColor", Color.Blue.toArgb()))
                }
                "baseTextColor" -> {
                    baseTextColor = Color(sharedPreferences.getInt("baseTextColor", Color.Black.toArgb()))
                }
                "workDay1Color" -> {
                    workDay1Color = Color(sharedPreferences.getInt("workDay1Color", Color.Yellow.toArgb()))
                }
                "workDay2Color" -> {
                    workDay2Color = Color(sharedPreferences.getInt("workDay2Color", Color.Red.toArgb()))
                }
                "workDay3Color" -> {
                    workDay3Color = Color(sharedPreferences.getInt("workDay3Color", Color.Green.toArgb()))
                }
                "workDay4Color" -> {
                    workDay4Color = Color(sharedPreferences.getInt("workDay4Color", Color.White.toArgb()))
                }
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        //Clean up listener when composable leaves the composition
        onDispose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    Log.d("CalendarScreen", "isSelectingRange in CalendarScreen: $updatedIsSelectingRange")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "Work Calendar",
                            color = baseTextColor
                        )
                        Spacer(Modifier.weight(1f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onToggleSelectingRange()
                    }, modifier = Modifier.width(80.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = if (updatedIsSelectingRange.value) R.drawable.ic_selecting_range_mode else R.drawable.ic_single_day_mode),
                                contentDescription = "Toggle mode"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSelectingRange.value) "Select Range" else "Single",
                                fontSize = 12.sp,
                                color = baseTextColor,
                                maxLines = 2,
                                modifier = Modifier.width(80.dp),
                                textAlign = TextAlign.Start
                            )
                        }

                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (!isJobLimitReached) showJobDialog = true
                    },
                        enabled = !isJobLimitReached
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Manage Jobs",
                            tint = if (isJobLimitReached) Color.Red else Color.White
                        )
                    }
                    IconButton(onClick = { onSettingsClicked(context) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(topBarColor.toArgb())
                )
            )
        },
        content = { innerPadding ->
            Column() {
                JobSelectionBar(
                    jobs = jobs,
                    jobColors = jobColorMap,
                    viewModel = viewModel,
                    selectedJobId = selectedJobId,
                    currentMonth = currentMonth.monthValue,
                    currentYear = currentMonth.year,
                    baseTextColor = baseTextColor,
                    innerPadding = innerPadding
                )
                CalendarContent(
                    viewModel,
                    jobColorMap = jobColorMap,
                    modifier = Modifier.padding(innerPadding),
                    isSelectingRange = isSelectingRange.value,
                    firstSelectedDate = firstSelectedDate,
                    secondSelectedDate = secondSelectedDate,
                    onFirstSelectedDateChange = { newDate -> firstSelectedDate = newDate },
                    onSecondSelectedDateChange = { newDate -> secondSelectedDate = newDate },
                    onMonthChanged = { newMonth ->
                        currentMonth = currentMonth.withMonth(newMonth.value)
                        viewModel.fetchWorkEntriesForMonth(
                            currentMonth.month.value,
                            currentMonth.year
                        )
                    },
                    entryEdited = entryEdited,
                    onEntryEditedChange = { isEdited -> entryEdited = isEdited },
                    launchAddWorkActivity = launchAddWorkActivity
                ) { refreshWorkEntries() }

            }
        }

    )

    //Show the Job Management Dialog conditionally
    if (showJobDialog) {
        JobManagementDialog(
            onDismiss = { showJobDialog = false },
            onJobAdded = { jobName ->
                //Insert the new job into the database
                viewModel.insertJob(jobName)
                Toast.makeText(context, "Job '$jobName' added successfully!", Toast.LENGTH_SHORT).show()

            }
        )
    }
}

private fun onSettingsClicked(context: Context) {
    val intent = Intent(context, UserSettingsActivity::class.java)
    context.startActivity(intent)
}



