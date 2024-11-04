package com.example.work_calendar_app.ui.composables.components.AddWorkEntry

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.work_calendar_app.viewmodels.WorkViewModel

@Composable
fun JobSelector(viewModel: WorkViewModel) {
    val jobs by viewModel.jobs.observeAsState(emptyList())
    var selectedJob by remember { mutableStateOf(jobs.firstOrNull()) }
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = selectedJob?.name ?: "Select Job",
            onValueChange = {},
            label = { Text("Job") },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )


        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            jobs.forEach { job ->
                DropdownMenuItem(
                    onClick = {
                        selectedJob = job
                        expanded = false
                    },
                    text = { Text(text = job.name) }
                )
            }
        }
    }
}