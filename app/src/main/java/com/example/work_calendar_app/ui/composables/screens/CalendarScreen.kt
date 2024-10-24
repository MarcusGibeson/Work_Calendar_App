package com.example.work_calendar_app.ui.composables.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
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
import com.example.work_calendar_app.viewmodels.WorkViewModel
import java.time.LocalDate

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: WorkViewModel) {

    Log.d("Recomposition", "CalendarScreen recomposed")

    var jobName by remember { mutableStateOf("") }

    var workEntries by remember { mutableStateOf(mutableMapOf<Long, WorkEntry>()) }
    var entryEdited by remember { mutableStateOf(false) }
    var workEntriesChanged by remember { mutableIntStateOf(0) }
    var selectedDay by remember { mutableIntStateOf(-1) }
    var firstSelectedDate: String? by remember { mutableStateOf(null) }
    var secondSelectedDate: String? by remember { mutableStateOf(null) }

    var currentMonth by remember { mutableStateOf(LocalDate.now()) }
    val context = LocalContext.current
    val sharedPreferences =
        context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

    var topBarColor by remember { mutableStateOf(Color(sharedPreferences.getInt("topBarColor", Color.Blue.toArgb()))) }

    var baseTextColor by remember { mutableStateOf(Color(sharedPreferences.getInt("baseTextColor", Color.Black.toArgb()))) }

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


    val jobColorMap = mutableMapOf<Long, Color>()

    //Load colors from shared preferences
    LaunchedEffect(Unit) {
        jobColorMap[1] = workDay1Color
        jobColorMap[2] = workDay2Color
        jobColorMap[3] = workDay3Color
        jobColorMap[4] = workDay4Color
    }

    //Log to monitor selecting range change
    LaunchedEffect(isSelectingRange) {
        Log.d("CalendarScreen", "isSelectingRange toggled to: $isSelectingRange.value")
    }

    //Whenever the screen is recomposed, ensure it checks if the preferences have changed
    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "topBarColor") {
                //Update topBarColor when the preference changes
                topBarColor =
                    Color(sharedPreferences.getInt("topBarColor", Color.Blue.toArgb()))
            } else if (key == "baseTextColor") {
                baseTextColor =
                    Color(sharedPreferences.getInt("baseTextColor", Color.Black.toArgb()))
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
                    IconButton(onClick = { onSettingsClicked(context) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(topBarColor.toArgb())
                )
            )
        },
        content = {innerPadding ->
            CalendarContent(
                viewModel,
                modifier = Modifier.padding(innerPadding),
                isSelectingRange = isSelectingRange.value,
                firstSelectedDate = firstSelectedDate,
                secondSelectedDate = secondSelectedDate,
                onFirstSelectedDateChange = { newDate -> firstSelectedDate = newDate },
                onSecondSelectedDateChange = { newDate -> secondSelectedDate = newDate },
                onMonthChanged = { newMonth ->
                    currentMonth = currentMonth.withMonth(newMonth.value)
                    viewModel.fetchWorkEntriesForMonth(currentMonth.month.value, currentMonth.year)
                },
                entryEdited = entryEdited,
                onEntryEditedChange = { isEdited -> entryEdited = isEdited }
            ) { refreshWorkEntries() }

        }
    )
}

private fun onSettingsClicked(context: Context) {
    val intent = Intent(context, UserSettingsActivity::class.java)
    context.startActivity(intent)
}



