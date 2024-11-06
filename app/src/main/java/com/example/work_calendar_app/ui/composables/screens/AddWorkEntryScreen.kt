package com.example.work_calendar_app.ui.composables.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.work_calendar_app.ui.composables.components.AddWorkEntry.BreakTimeInput
import com.example.work_calendar_app.ui.composables.components.AddWorkEntry.EndTimePicker
import com.example.work_calendar_app.ui.composables.components.AddWorkEntry.JobSelector
import com.example.work_calendar_app.ui.composables.components.AddWorkEntry.PayTypeSelector
import com.example.work_calendar_app.ui.composables.components.AddWorkEntry.SaveButtons
import com.example.work_calendar_app.ui.composables.components.AddWorkEntry.StartTimePicker
import com.example.work_calendar_app.ui.composables.components.AddWorkEntry.WorkDatePicker
import com.example.work_calendar_app.viewmodels.WorkViewModel

@Composable
fun AddWorkEntryScreen(viewModel: WorkViewModel, onSave: () -> Unit, onSaveSchedule: () -> Unit, onFinish: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        JobSelector(viewModel)

        WorkDatePicker(viewModel)
        Row {
            StartTimePicker(viewModel)
            Spacer(modifier = Modifier.width(30.dp))
            EndTimePicker(viewModel)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row {
            BreakTimeInput(viewModel)
            Spacer(modifier = Modifier.width(30.dp))
            PayTypeSelector(viewModel)
        }

        SaveButtons(onSave, onSaveSchedule, onFinish)
    }
}