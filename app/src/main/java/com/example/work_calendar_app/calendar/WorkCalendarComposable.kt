package com.example.work_calendar_app.calendar

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable
fun WorkCalendar(currentMonth: LocalDate, daysInMonth: Int, workDays: List<Int>, onDaySelected: (Int) -> Unit) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

    //Retrieve colors from preferences, with default fallback values
    val workDay1Color = sharedPreferences.getInt("workDay1Color", Color.Green.toArgb())
    val workDay2Color = sharedPreferences.getInt("workDay2Color", Color.Magenta.toArgb())
    val workDay3Color = sharedPreferences.getInt("workDay3Color", Color.Yellow.toArgb())
    val outlineColor = sharedPreferences.getInt("outlineColor", Color.Cyan.toArgb())

    val firstDayOfMonth = (currentMonth.withDayOfMonth(1).dayOfWeek.value % 7)

    val totalDaysToRender = firstDayOfMonth + daysInMonth
    val numberOfWeeks = (totalDaysToRender / 7) + if (totalDaysToRender % 7 != 0) 1 else 0

    val currentDay = if (currentMonth.month == LocalDate.now().month && currentMonth.year == LocalDate.now().year) {
        LocalDate.now().dayOfMonth
    } else null
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
                            val currentDayColor =  if (day == currentDay) Color(outlineColor) else Color.Transparent
                            val borderColor = Color.Black
                            val backgroundColor = when {
                                workDays.contains(day) -> Color(workDay1Color)//WorkDays
                                //work2Days.contains(day) -> Color(workDay2Color)
                                //work3Days.contains(day) -> Color(workDay3Color)
                                else -> Color.White
                            }
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
                                    .clickable { onDaySelected(day) },
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
fun DayDetailsPopup(selectedDay: Int, startTime: String, endTime: String, breakTime: String, wage: Double, onDismiss: () -> Unit) {
    val hasWorkSchedule = !startTime.isNullOrEmpty() && !endTime.isNullOrEmpty() && !breakTime.isNullOrEmpty() && wage != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Details for ${getDayWithOrdinalSuffix(selectedDay)}") },
        text = {
            if (hasWorkSchedule) {
                Text("Work from $startTime to $endTime\n$breakTime minute break\nWage: $$wage")
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