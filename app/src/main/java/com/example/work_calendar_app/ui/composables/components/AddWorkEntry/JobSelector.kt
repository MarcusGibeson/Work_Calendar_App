package com.example.work_calendar_app.ui.composables.components.AddWorkEntry

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.example.work_calendar_app.viewmodels.WorkViewModel

@Composable
fun JobSelector(viewModel: WorkViewModel) {

    LaunchedEffect (Unit) {
        viewModel.loadAllJobs()
    }

    val jobs by viewModel.jobs.observeAsState(emptyList())
    var selectedJob by remember { mutableStateOf(jobs.firstOrNull()) }
    var expanded by remember { mutableStateOf(false) }

    //Update selectedJob when jobs list updates
    LaunchedEffect(jobs) {
        if (jobs.isNotEmpty() && selectedJob == null) {
            selectedJob = jobs.firstOrNull()
        }
    }

    println("Jobs list: $jobs")

    Box (modifier = Modifier.widthIn(max = 150.dp)
    ) {
        OutlinedTextField(
            value = selectedJob?.name ?: "Select Job",
            onValueChange = {},
            label = { Text("Job") },
            readOnly = true,
            trailingIcon = {
                Icon(
                   imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Dropdown Arrow",
                    modifier = Modifier.clickable {
                        expanded = !expanded
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    expanded = !expanded
                    println("Dropdown expanded: $expanded")
                }
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