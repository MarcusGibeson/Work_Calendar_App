package com.example.work_calendar_app.ui.composables.components.AddWorkEntry

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.work_calendar_app.viewmodels.WorkViewModel

@Composable
fun HourlyFields(viewModel: WorkViewModel) {
    Column {
        Row {
            Box (modifier = Modifier.widthIn(max = 150.dp)
            ) {
                OutlinedTextField(
                    value = viewModel.hourlyRate,
                    onValueChange = { viewModel.hourlyRate = it },
                    label = { Text ("Hourly Rate") }
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Box (modifier = Modifier.widthIn(max = 150.dp)
            ) {
                OutlinedTextField(
                    value = viewModel.overtimeRate,
                    onValueChange = { viewModel.overtimeRate = it },
                    label = { Text ("Overtime Rate") }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box (modifier = Modifier.widthIn(max = 150.dp)
        ) {
            OutlinedTextField(
                value = viewModel.tips,
                onValueChange = { viewModel.tips = it },
                label = { Text ("Tips") }
            )
        }

    }

}