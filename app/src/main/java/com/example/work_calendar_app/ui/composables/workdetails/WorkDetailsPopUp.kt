package com.example.work_calendar_app.ui.composables.workdetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.work_calendar_app.data.models.WorkEntry
import com.example.work_calendar_app.viewmodels.WorkViewModel

@Composable
fun WorkDetailsPopUp(viewModel: WorkViewModel, workEntry: WorkEntry, onClose: () -> Unit,
                     onSave: (WorkEntry) -> Unit, onDelete: () -> Unit) {
    var isEditable by remember { mutableStateOf(false) }

    //State variables for editing work entry details
    var startTime by remember { mutableStateOf(workEntry.startTime) }
    var endTime by remember { mutableStateOf(workEntry.endTime) }
    var breakTime by remember { mutableStateOf(workEntry.breakTime) }
    var payType by remember { mutableStateOf(workEntry.payType) }
    var payRate by remember { mutableStateOf(workEntry.payRate) }
    var overtimeRate by remember { mutableStateOf(workEntry.overtimeRate) }
    var salaryAmount by remember { mutableStateOf(workEntry.salaryAmount) }
    var dailySalary by remember { mutableStateOf(workEntry.dailySalary) }
    var commissionRate by remember { mutableStateOf(workEntry.commissionRate) }
    var commissionDetails by remember { mutableStateOf(workEntry.commissionDetails) }

    Dialog(onDismissRequest = { onClose() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Work Details", style = MaterialTheme.typography.headlineSmall)

                Spacer(modifier = Modifier.height(8.dp))

                //Conditionally render fields based on isEditable
                if (isEditable) {
                    //Editable Textfields
                    TextField(value = startTime, onValueChange = { startTime = it }, label = { Text("Start Time") })
                    TextField(value = endTime, onValueChange = { endTime = it }, label = { Text("End Time") })
                    TextField(
                        value = breakTime.toString(),
                        onValueChange = {
                            val newValue = it.toIntOrNull()
                            if (newValue != null) {
                                breakTime = newValue
                            }
                        },
                        label = { Text("Break Time(minutes)") }
                    )

                    //Pay type specific fields
                    when (payType) {
                        "Hourly" -> {
                            TextField(
                                value = payRate.toString(),
                                onValueChange = {
                                    val newValue = it.toDoubleOrNull()
                                    if (newValue != null) {
                                        payRate = newValue
                                    }
                                },
                                label = { Text ("Pay Rate") }
                            )
                            TextField(
                                value = overtimeRate.toString(),
                                onValueChange = {
                                    val newValue = it.toDoubleOrNull()
                                    if (newValue != null) {
                                        overtimeRate = newValue
                                    }
                                },
                                label = { Text ("Overtime Rate") }
                            )
                        }
                        "Salary" -> {
                            TextField(
                                value = salaryAmount.toString(),
                                onValueChange = {
                                    val newValue = it.toDoubleOrNull()
                                    if (newValue != null) {
                                        salaryAmount = newValue
                                    }
                                },
                                label = { Text ("Yearly Salary") }
                            )
                        }
                        "Commission" -> {
                            TextField(
                                value = commissionRate.toString(),
                                onValueChange = {
                                    val newValue = it.toIntOrNull()
                                    if (newValue != null) {
                                        commissionRate = newValue
                                    }
                                },
                                label = { Text ("Commission Rate") }
                            )
                            LazyColumn(
                                modifier = Modifier
                                    .height(150.dp)
                                    .fillMaxWidth()
                            ){
                                items(commissionDetails) {detail ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        TextField(
                                            value = detail.toString(),
                                            onValueChange = {
                                                val newValue = it.toDoubleOrNull()
                                                if (newValue != null) {
                                                    val index = commissionDetails.indexOf(detail)
                                                    commissionDetails = commissionDetails.toMutableList().apply {
                                                        set(index, newValue)
                                                    }
                                                }
                                            },
                                            label = { Text("Sale amount") },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    //Delete button
                                    IconButton(
                                        onClick = {
                                            val index = commissionDetails.indexOf(detail)
                                            if (index != -1) {
                                                commissionDetails = commissionDetails.toMutableList().apply {
                                                    removeAt(index)
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Sale")
                                    }
                                }
                            }

                            Button(onClick = {
                                //Add new sale to the list
                                commissionDetails = commissionDetails.toMutableList().apply { add (0.0) }
                            }) {
                                Text("Add sale")
                            }
                        }
                    }
                } else {
                    //Non-editable texts
                    Text(text = "Start Time: $startTime")
                    Text(text = "End Time: $endTime")
                    Text(text = "Break Time: $breakTime minutes")
                    Text(text = "Pay Type: $payType")
                    when (payType) {
                        "Hourly" -> {
                            Text(text = "Pay Rate: $payRate")
                            Text(text = "Overtime Rate: $overtimeRate")
                        }
                        "Salary" -> {
                            Text(text = "Total salary: $salaryAmount")
                            Text(text = "Amount earned today: $dailySalary")
                        }
                        "Commission" -> {
                            Text(text = "Commission rate: $commissionRate")
                            Text(text = "Sale totals: $commissionDetails")
                        }
                    }


                }

                Spacer(modifier = Modifier.height(16.dp))

                Row (
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    //Toggle between Edit and Save
                    Button(onClick = {
                        if (isEditable) {
                            onSave(
                                workEntry.copy(
                                    startTime = startTime,
                                    endTime = endTime,
                                    breakTime = breakTime,
                                    payRate = payRate,
                                    payType = payType,
                                    overtimeRate = overtimeRate,
                                    commissionRate = commissionRate,
                                    commissionDetails = commissionDetails,
                                    salaryAmount = salaryAmount
                                )
                            )
                        }
                        isEditable = !isEditable
                    }) {
                        Text(if (isEditable) "Save" else "Edit")
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))

                //Delete button
                Button(onClick = { onDelete() }) {
                    Text("Delete")
                }

                Spacer(modifier = Modifier.width(8.dp))

                //Close button
                Button(onClick = { onClose() }) {
                    Text("Close")
                }
            }
        }
    }
}
