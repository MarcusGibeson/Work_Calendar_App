package com.example.work_calendar_app.ui.composables.screens

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.core.content.ContextCompat.startActivity
import com.example.work_calendar_app.R
import com.example.work_calendar_app.UserSettingsActivity
import com.example.work_calendar_app.data.models.WorkEntry
import com.example.work_calendar_app.ui.composables.calendar.CalendarContent
import com.example.work_calendar_app.viewmodels.WorkViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: WorkViewModel) {
    var workEntries by remember { mutableStateOf(mutableMapOf<Long, WorkEntry>()) }
    var selectedDay by remember { mutableStateOf(-1) }
    var firstSelectedDate by remember { mutableStateOf<String?>(null) }
    var secondSelectedDate by remember { mutableStateOf<String?>(null) }
    var isSelectingRange by remember { mutableStateOf(false) }
    var currentMonth by remember { mutableStateOf(LocalDate.now()) }
    val context = LocalContext.current
    val sharedPreferences =
        context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

    var topBarColor by remember {
        mutableStateOf(
            Color(
                sharedPreferences.getInt(
                    "topBarColor",
                    Color.Blue.toArgb()
                )
            )
        )
    }

    var baseTextColor by remember {
        mutableStateOf(
            Color(
                sharedPreferences.getInt(
                    "baseTextColor",
                    Color.Black.toArgb()
                )
            )
        )
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
                        isSelectingRange = !isSelectingRange
                        firstSelectedDate = null
                        secondSelectedDate = null
                        selectedDay = -1
                    }, modifier = Modifier.width(80.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = if (isSelectingRange) R.drawable.ic_selecting_range_mode else R.drawable.ic_single_day_mode),
                                contentDescription = "Toggle mode"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSelectingRange) "Select Range" else "Single",
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
                    IconButton(onClick = { onSettingsClicked() }) {
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
                modifier = Modifier.padding(innerPadding),
                workEntries = workEntries,
                onMonthChanged = { newMonth ->
                    currentMonth = currentMonth.withMonth(newMonth.value)
                    workEntries = fetchWorkEntriesForMonth(currentMonth.month.value, currentMonth.year)
                },
                entryEdited = entryEdited,
                onEntryEditedChange = { isEdited -> entryEdited = isEdited },
                onWorkEntriesChanged = { refreshWorkEntries() }
            )
        }
    )
}

