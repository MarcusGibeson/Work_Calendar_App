package com.example.work_calendar_app.ui.composables.components.AddWorkEntry

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.work_calendar_app.viewmodels.WorkViewModel


@Composable
fun PayTypeSelector(viewModel: WorkViewModel) {
    val payTypes = listOf("None", "Hourly", "Salary", "Commission")
    var selectedPayType by remember { mutableStateOf(payTypes[0]) }
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.widthIn(max = 150.dp)) {
        OutlinedTextField(
            value = selectedPayType,
            onValueChange = {},
            label = {Text("Pay Type") },
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
                .clickable { expanded = true }
        )


        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false}
        ) {
            payTypes.forEach { payType ->
                DropdownMenuItem(
                    onClick = {
                        selectedPayType = payType
                        expanded = false
                    },
                    text = { Text(text = payType) }
                )
            }
        }
    }
}