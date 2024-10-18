package com.example.work_calendar_app.ui.composables.calendar

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.work_calendar_app.AddWorkActivity
import com.example.work_calendar_app.UserSettingsActivity
import com.example.work_calendar_app.data.models.WorkDetails
import com.example.work_calendar_app.data.models.WorkEntry
import com.example.work_calendar_app.ui.composables.workdetails.WorkEntriesList
import com.example.work_calendar_app.viewmodels.WorkViewModel
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter

@Composable
fun CalendarContent(viewModel: WorkViewModel, modifier: Modifier, workEntries: StateFlow<Map<Long, WorkEntry>>, onMonthChanged: (Month) -> Unit, entryEdited: Boolean, onEntryEditedChange: (Boolean) -> Unit, onWorkEntriesChanged: () -> Unit) {


    Box(modifier = Modifier) {
        val isLoading by viewModel.loading.collectAsState()
        val errorMessage by viewModel.errorMessage.collectAsState()

        if (isLoading) {
            CircularProgressIndicator()
        }

        errorMessage?.let {
            Text(text = it, color = Color.Red)
        }

        var currentMonth by remember { mutableStateOf(LocalDate.now()) }
        var showPopup by remember { mutableStateOf(false) }
        var selectedDay by remember { mutableStateOf(-1) }
        var firstSelectedDate by remember { mutableStateOf<String?>(null) }
        var secondSelectedDate by remember { mutableStateOf<String?>(null) }
        var isSelectingRange by remember { mutableStateOf(false) }

        var workEntriesChanged by remember { mutableStateOf(0) }

        //Store fetched work details for the selected day
        var workEntryForPopup by remember { mutableStateOf(WorkEntry(0,"","","",0, "",0.0, 0.0,0,listOf(0.0), 0.0, 0.0, 0.0, 0.0, 0.0)) }

        //Store fetched work details for the selected range
        val workEntriesList = remember { mutableStateListOf<WorkEntry>() }

        //Fetch context and database-related work entries
        val context = LocalContext.current
        val daysInMonth = currentMonth.lengthOfMonth()
        val firstDayOfMonth = currentMonth.withDayOfMonth(1).dayOfWeek.value

        //Dummy data for workDays and workEntries
        val workDays by remember { derivedStateOf { viewModel.workDays } }
        val workEntries by viewModel.workEntries.collectAsState()
        val sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

        //Retrieve colors from preferences, with default fallback values
        var backgroundColor1 by remember { mutableStateOf(Color(sharedPreferences.getInt("backgroundColor1", Color(143, 216, 230).toArgb()))) }
        var backgroundColor2 by remember { mutableStateOf(Color(sharedPreferences.getInt("backgroundColor2", Color.White.toArgb()))) }

        var baseTextColor by remember { mutableStateOf(Color(sharedPreferences.getInt("baseTextColor", Color.Black.toArgb()))) }

        var baseButtonColor by remember { mutableStateOf(Color(sharedPreferences.getInt("baseButtonColor", Color(204, 153, 255).toArgb()))) }

        var detailsTextColor by remember { mutableStateOf(Color(sharedPreferences.getInt("detailsTextColor", Color.Black.toArgb()))) }

        //Whenever the screen is recomposed, ensure it checks if the preferences have changed
        DisposableEffect(Unit) {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                when (key) {
                    "backgroundColor1" -> {
                        backgroundColor1 = Color(sharedPreferences.getInt("backgroundColor1", Color.Blue.toArgb()))
                    }
                    "backgroundColor2" -> {
                        backgroundColor2 = Color(sharedPreferences.getInt("backgroundColor2", Color.White.toArgb()))
                    }
                    "baseTextColor" -> {
                        baseTextColor = Color(sharedPreferences.getInt("baseTextColor", Color.Black.toArgb()))
                    }
                    "baseButtonColor" -> {
                        baseButtonColor = Color(sharedPreferences.getInt("baseButtonColor", Color.Black.toArgb()))
                    }
                    "detailsTextColor" -> {
                        detailsTextColor = Color(sharedPreferences.getInt("detailsTextColor", Color.Black.toArgb()))
                    }
                }
            }
            sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

            //Clean up listener when composable leaves the composition
            onDispose {
                sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
            }
        }

        //Initial load
        LaunchedEffect(Unit) {
            viewModel.loadAllWorkEntries(
                currentMonth = currentMonth.monthValue,
                currentYear = currentMonth.year
            )
            Log.d("Workdays", "Initial load - Work Days: $workDays")
        }

        //Fetch data from database in a LaunchedEffect
        LaunchedEffect(currentMonth, workEntriesChanged) {
            viewModel.loadAllWorkEntries(
                currentMonth = currentMonth.monthValue,
                currentYear = currentMonth.year
            )
            Log.d("WorkDays", "Current month: ${currentMonth.month} ${currentMonth.year}, Work Days: $workDays")
        }

        //Reset SelectedDay when switching modes
        LaunchedEffect(isSelectingRange) {
            selectedDay = -1
        }

        //Fetch the data for the selected day
        LaunchedEffect(selectedDay, entryEdited) {
            if (selectedDay != -1 && !isSelectingRange) {
                workEntryForPopup = WorkEntry(0,"","","",0, "",0.0, 0.0,0,listOf(0.0), 0.0, 0.0, 0.0, 0.0, 0.0)

                val formattedDate = String.format("%04d-%02d-%02d", currentMonth.year, currentMonth.monthValue, selectedDay)
                Log.d("MainActivity","formatted date: $formattedDate")

                viewModel.getWorkEntryForDate(formattedDate) { workEntry ->
                    if (workEntry != null) {
                        workEntryForPopup = workEntry
                        Log.d("Popup", "Work details: $workEntry")
                        showPopup = true
                    } else {
                        Log.d("CalendarContent", "No valid work details for popup")
                    }
                }
            }
            onEntryEditedChange(false)
        }


        //Box around calendar to detect swipe gestures
        val screenWidth = LocalConfiguration.current.screenWidthDp
        var accumulatedDrag = 0f
        val swipeThreshold = screenWidth * 1f

        //Define subtle gradient
        val gradientBrush = Brush.linearGradient(
            colors = listOf(
                Color(backgroundColor1.toArgb()),
                Color(backgroundColor2.toArgb())
            ),
            start = Offset(0f, 0f),
            end = Offset(1000f, 1000f)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = gradientBrush)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            accumulatedDrag += dragAmount //Accumulate drag distance

                            //Check if the accumulated drag surpasses the swipe threshold
                            if (accumulatedDrag > swipeThreshold) {
                                //Swipe right (previous month)
                                accumulatedDrag = 0f
                                currentMonth = currentMonth.minusMonths(1)
                                onMonthChanged(currentMonth.month)
                            } else if (accumulatedDrag < -swipeThreshold) {
                                //Swipe left (next month)
                                accumulatedDrag = 0f
                                currentMonth = currentMonth.plusMonths(1)
                                onMonthChanged(currentMonth.month)
                            }
                        },
                        onDragEnd = {
                            //Reset the accumulated drag when the gesture ends
                            accumulatedDrag = 0f
                        }
                    )
                }
        ) {
            //Main Layout of Calendar
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp, bottom = 0.dp)
            ) {
                Spacer(modifier = Modifier.height(70.dp)) //spacer between TopBar and prev/next buttons

                //Row for previous and next buttons with Month title in between
                Row(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = {
                        currentMonth = currentMonth.minusMonths(1)
                        onMonthChanged(currentMonth.month)
                    },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = baseButtonColor,
                            contentColor = baseTextColor,
                        )
                    ) {
                        Text("Previous", color = detailsTextColor)
                    }
                    Text(
                        text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                        color = baseTextColor,
                        modifier = Modifier.padding(
                            PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp)
                        ),
                        fontSize = 16.sp
                    )
                    Button(onClick = {
                        currentMonth = currentMonth.plusMonths(1)
                        onMonthChanged(currentMonth.month)
                    },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = baseButtonColor,
                            contentColor = baseTextColor,
                        )
                    ) {
                        Text("Next", color = detailsTextColor)
                    }
                }

                //Row for weekday headers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Spacer(modifier = Modifier.weight(0.3f))
                    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                    for (day in daysOfWeek) {
                        Text (
                            text = day,
                            color = baseTextColor,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                //Custom Work Calendar
                WorkCalendar(viewModel, currentMonth, daysInMonth = daysInMonth, workDays = workDays.toList()) { day ->
                    if (isSelectingRange) {
                        val monthValue = currentMonth.monthValue
                        val yearValue = currentMonth.year

                        if (firstSelectedDate == null) {
                            firstSelectedDate = "$monthValue/$day/$yearValue"
                            Toast.makeText(context, "First date selected: $firstSelectedDate", Toast.LENGTH_SHORT).show()
                        } else if (secondSelectedDate == null) {
                            secondSelectedDate = "$monthValue/$day/$yearValue"
                            Toast.makeText(context, "Second date selected: $secondSelectedDate", Toast.LENGTH_SHORT).show()

                            viewModel.fetchWorkEntriesBetweenDates(firstSelectedDate!!, secondSelectedDate!!) {  workEntries ->

                            }
                            isSelectingRange = false
                        }
                    } else {
                        selectedDay = day
                        showPopup = true
                    }
                }

                Row(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(16.dp))

                    //Add work schedule button
                    Button(
                        onClick = {
                            onAddWorkActivityClicked(context)
                        },
                        modifier = Modifier
                            .padding(start = 0.dp, end = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = baseButtonColor,
                            contentColor = baseTextColor,
                        )
                    ){
                        Text("Add Work Entry", color = detailsTextColor)
                    }
                }


                //Composable view for Work Details
                Text(
                    text = "  Work Date  |  Work Time  ",
                    modifier = Modifier.padding(vertical = 0.dp),
                    color = baseTextColor
                )
                WorkEntriesList(viewModel, workEntries, currentMonth)

                //Popup for displaying details
                if(isLoading) {
                    CircularProgressIndicator()
                } else if (showPopup && selectedDay != -1 && !isSelectingRange) {
                    Log.d("DayDetailsPopup", "Selected Day: $selectedDay")
                    Log.d("DayDetailsPopup", "Start Time: ${workEntryForPopup.startTime}")
                    Log.d("DayDetailsPopup", "End Time: ${workEntryForPopup.endTime}")
                    Log.d("DayDetailsPopup", "Break Time: ${workEntryForPopup.breakTime}")
                    Log.d("DayDetailsPopup", "Pay Type: ${workEntryForPopup.payType}")
                    Log.d("DayDetailsPopup", "Pay Rate: ${workEntryForPopup.payRate}")
                    Log.d("DayDetailsPopup", "Commission Sales: ${workEntryForPopup.totalCommissionAmount}")
                    Log.d("DayDetailsPopup", "Daily Salary: ${workEntryForPopup.dailySalary}")
                    Log.d("DayDetailsPopup", "Tips: ${workEntryForPopup.tips}")
                    Log.d("DayDetailsPopup", "Net Earnings: ${workEntryForPopup.netEarnings}")
                    DayDetailsPopup(
                        selectedDay = selectedDay,
                        startTime = workEntryForPopup.startTime,
                        endTime = workEntryForPopup.endTime,
                        breakTime = workEntryForPopup.breakTime.toString(),
                        payType = workEntryForPopup.payType,
                        payRate = workEntryForPopup.payRate,
                        commissionSales = workEntryForPopup.totalCommissionAmount,
                        dailySalary = workEntryForPopup.dailySalary,
                        tips = workEntryForPopup.tips,
                        netEarnings = workEntryForPopup.netEarnings
                    ) { showPopup = false }
                }
            }
        }
    }
}

private fun onAddWorkActivityClicked(context: Context) {
    val intent = Intent(context, AddWorkActivity::class.java)
    context.startActivity(intent)
}
