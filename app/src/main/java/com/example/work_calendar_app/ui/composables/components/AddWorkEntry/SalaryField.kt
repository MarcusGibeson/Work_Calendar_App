package com.example.work_calendar_app.ui.composables.components.AddWorkEntry

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.work_calendar_app.viewmodels.WorkViewModel

@Composable
fun SalaryField (viewModel: WorkViewModel) {
    Box (modifier = Modifier.widthIn(max = 150.dp)
    ) {
        OutlinedTextField(
            value = viewModel.yearlySalary,
            onValueChange = { viewModel.yearlySalary = it },
            label = { Text ("Yearly Salary") }
        )
    }

}