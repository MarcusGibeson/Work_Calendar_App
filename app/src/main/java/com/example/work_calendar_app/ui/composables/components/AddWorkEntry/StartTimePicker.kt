package com.example.work_calendar_app.ui.composables.components.AddWorkEntry

import android.app.TimePickerDialog
import android.graphics.drawable.Icon
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.work_calendar_app.viewmodels.WorkViewModel
import java.util.Calendar

@Composable
fun StartTimePicker(viewModel: WorkViewModel) {
    var startTime by remember { mutableStateOf(" - -  :  - - ") }
    val context = LocalContext.current

    Box (modifier = Modifier.widthIn(max = 150.dp)
    ) {

        OutlinedTextField(
            value = startTime,
            onValueChange = {},
            label = { Text("Start Time") },
            readOnly = true,
            trailingIcon = {
                           Icon(
                               imageVector = Icons.Filled.AccessTime,
                               contentDescription = "Select Time",
                               modifier = Modifier.clickable {
                                   val calendar = Calendar.getInstance()
                                   val hour = calendar.get(Calendar.HOUR_OF_DAY)
                                   val minute = calendar.get(Calendar.MINUTE)
                                   TimePickerDialog(context, { _, selectedHour, selectedMinute ->
                                       val amPm = if (selectedHour >= 12) "PM" else "AM"
                                       val newHour = if (selectedHour % 12 == 0) 12 else selectedHour % 12
                                       startTime = String.format("%02d:%02d %s", newHour, selectedMinute, amPm)
                                   }, hour, minute, false).show()
                               }
                           )
            },
            modifier = Modifier.clickable {
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                TimePickerDialog(context, { _, selectedHour, selectedMinute ->
                    startTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                }, hour, minute, false).show()

            }
        )
    }


}