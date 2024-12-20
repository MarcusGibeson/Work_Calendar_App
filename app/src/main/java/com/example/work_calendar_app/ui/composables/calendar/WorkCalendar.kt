package com.example.work_calendar_app.ui.composables.calendar

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.work_calendar_app.AddWorkActivity
import com.example.work_calendar_app.MainActivity
import com.example.work_calendar_app.viewmodels.WorkViewModel
import java.time.LocalDate

@Composable
fun WorkCalendar(
    viewModel: WorkViewModel,
    jobColorMap: Map<Long, Color>,
    currentMonth: LocalDate,
    daysInMonth: Int,
    workDays: List<Int>,
    isSelectingRange: Boolean,
    onDaySelected: (Int) -> Unit
) {

    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

    val updatedIsSelectingRange by rememberUpdatedState(isSelectingRange)

    Log.d("Recomposition", "WorkCalendar recomposed")
    Log.d("WorkCalendar", "is Selecting Range? : $updatedIsSelectingRange")

    //Retrieve colors from preferences, with default fallback values
    var workDay1Color by remember { mutableStateOf(Color(sharedPreferences.getInt("workDay1Color", Color.Yellow.toArgb()))) }

    var outlineColor by remember {mutableStateOf(Color(sharedPreferences.getInt("outlineColor", Color.Blue.toArgb()))) }

    val firstDayOfMonth = (currentMonth.withDayOfMonth(1).dayOfWeek.value % 7)

    val totalDaysToRender = firstDayOfMonth + daysInMonth
    val numberOfWeeks = (totalDaysToRender / 7) + if (totalDaysToRender % 7 != 0) 1 else 0

    val currentDay = if (currentMonth.month == LocalDate.now().month && currentMonth.year == LocalDate.now().year) {
        LocalDate.now().dayOfMonth
    } else null

    Log.d("WorkCalendar", "Current workDays: $workDays")

    //Whenever the screen is recomposed, ensure it checks if the preferences have changed
    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "workDay1Color") {
                //Update workDay2Color when the preference changes
                workDay1Color =
                    Color(sharedPreferences.getInt("workDay1Color", Color.Green.toArgb()))
            } else if (key == "outlineColor") {
                //Update outlineColor when the preference changes
                outlineColor =
                    Color(sharedPreferences.getInt("outlineColor", Color.Green.toArgb()))
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        //Clean up listener when composable leaves the composition
        onDispose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column (
            modifier = Modifier.align(Alignment.Center)
        )
        {
            //Calendar grid
            for (week in 0 until numberOfWeeks) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    for (dayOfWeek in 0..6) {
                        val day = (week * 7 + dayOfWeek) - firstDayOfMonth + 1

                        if (day in 1..daysInMonth) {
                            val jobId = viewModel.getJobIdForDay(day)

                            val currentDayColor =  if (day == currentDay) Color(outlineColor.toArgb()) else Color.Transparent
                            val borderColor = Color.Black
                            val backgroundColor = jobColorMap[jobId] ?: Color.LightGray
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .border(
                                        BorderStroke(8.dp, currentDayColor),
                                        shape = CircleShape
                                    )
                                    .border(
                                        BorderStroke(1.dp, borderColor),
                                    )
                                    .background(backgroundColor)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onTap = {
                                                Log.d("Calendar", "Day tapped: $day")
                                                onDaySelected(day)
                                            },
                                            onLongPress = {
                                                //handle the long press and launch AddActivity
                                                val intent =
                                                    Intent(context, AddWorkActivity::class.java)
                                                intent.putExtra("selectedDay", day)
                                                intent.putExtra(
                                                    "selectedMonth",
                                                    currentMonth.monthValue
                                                )
                                                intent.putExtra("selectedYear", currentMonth.year)
                                                (context as MainActivity).addWorkActivityLauncher.launch(
                                                    intent
                                                )
                                            }
                                        )

                                    },
                                contentAlignment = Alignment.Center,

                                ) {
                                Text(
                                    text = day.toString(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Black
                                )
                            }
                        } else {
                            //Empty Box for days outside the current month
                            Box(
                                modifier = Modifier
                                    .size(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "")
                            }
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun DayDetailsPopup(selectedDay: Int, startTime: String, endTime: String, breakTime: String, payType: String, payRate: Double, commissionSales: Double, dailySalary: Double, tips: Double, netEarnings: Double, onDismiss: () -> Unit) {
    val hasWorkSchedule = startTime.isNotEmpty() && endTime.isNotEmpty() && breakTime.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Details for ${getDayWithOrdinalSuffix(selectedDay)}") },
        text = {
            if (hasWorkSchedule) {
                Text(
                    "Work from $startTime to $endTime" +
                            if (breakTime > "0") {
                                "\n$breakTime minute break"
                            }else {
                                "\nNo break"
                            } +
                            when(payType) {
                                "Hourly" -> {
                                    "\n$payType : Earn $$payRate/hr"
                                }
                                "Salary" -> {
                                    "\n$payType : Made $$dailySalary this day"
                                }
                                "Commission" -> {
                                    "\n$payType : Made $$commissionSales in sales"
                                }

                                else -> {}
                            } +
                            "\nTips: $$tips" +
                            "\nNet Earnings: $$netEarnings")
            } else {
                Text("No work schedule found for this day.")
            }

        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

fun getDayWithOrdinalSuffix(day: Int): String {
    return when {
        day in 11..13 -> "${day}th"
        day % 10 == 1 -> "${day}st"
        day % 10 == 2 -> "${day}nd"
        day % 10 == 3 -> "${day}rd"
        else -> "${day}th"
    }
}