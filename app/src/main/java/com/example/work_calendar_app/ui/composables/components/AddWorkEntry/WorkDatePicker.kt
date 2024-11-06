package com.example.work_calendar_app.ui.composables.components.AddWorkEntry

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
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

    Box (modifier = Modifier.widthIn(max = 150.dp)
    ) {
        OutlinedTextField(
            value = selectedDate,
            onValueChange = {},
            label = { Text("Work Date") },
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = "Select Date",
                    modifier = Modifier.clickable {showDialog = true }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable{ showDialog = true }
        )
    }

    if (showDialog) {
        DatePickerDialog(context, { _, newYear, newMonth, newDay ->
            selectedDate = "$newYear/${newMonth + 1}/$newDay"
            showDialog = false
        }, year, month, day).apply {
            setOnDismissListener { showDialog = false }
        }.show()
    }
}