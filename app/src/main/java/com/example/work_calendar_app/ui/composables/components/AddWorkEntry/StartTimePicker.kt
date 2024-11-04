package com.example.work_calendar_app.ui.composables.components.AddWorkEntry

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.work_calendar_app.viewmodels.WorkViewModel
import java.util.Calendar

@Composable
fun StartTimePicker(viewModel: WorkViewModel) {
    var startTime by remember { mutableStateOf("Select Start Time") }
    val context = LocalContext.current

    OutlinedTextField(
        value = startTime,
        onValueChange = {},
        label = { Text("Start Time") },
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