package com.example.work_calendar_app.ui.composables.components.AddWorkEntry

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.work_calendar_app.viewmodels.WorkViewModel


@Composable
fun PayTypeSelector(viewModel: WorkViewModel) {
    val payTypes = listOf("Hourly", "Salary", "Commission")
    var selectedPayType by remember { mutableStateOf(payTypes[0]) }

    DropdownMenu(
        expanded = /*State to toggle dropdown*/,
        onDismissRequest = {}
    ) {
        payTypes.forEach { payType ->
            DropdownMenuItem(
                onClick = {
                    selectedPayType = payType
                }
            ) {
                Text(text = payType)
            }
        }
    }
}