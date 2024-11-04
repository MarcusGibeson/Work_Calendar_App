package com.example.work_calendar_app.ui.composables.components.AddWorkEntry

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.work_calendar_app.viewmodels.WorkViewModel
import java.util.Calendar

@Composable
fun WorkDatePicker(viewModel: WorkViewModel) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    //State for selected date
    var selectedDate by remember { mutableStateOf("$year/${month + 1}/$day")}
    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedDate,
        onValueChange = {},
        label = { Text("Work Date") },
        modifier = Modifier.clickable{ showDialog = true }
    )

    if (showDialog) {
        LaunchedEffect(showDialog) {
            DatePickerDialog(context, { _, newYear, newMonth, newDay ->
                selectedDate = "$newYear/${newMonth + 1}/$newDay"
                showDialog = false
            }, year, month, day).apply {
                setOnDismissListener { showDialog = false }
            }.show()
        }
    }
}