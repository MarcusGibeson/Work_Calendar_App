package com.example.work_calendar_app.ui.composables.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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

        StartTimePicker(viewModel)
        EndTimePicker(viewModel)
        BreakTimeInput(viewModel)

        PayTypeSelector(viewModel)
        SaveButtons(onSave, onSaveSchedule, onFinish)
    }
}