package com.example.work_calendar_app.ui.composables.components.AddWorkEntry

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.work_calendar_app.viewmodels.WorkViewModel

@Composable
fun BreakTimeInput(viewModel: WorkViewModel) {
    var breakTime by remember { mutableStateOf("0") }

    Box (modifier = Modifier.widthIn(max = 100.dp)
    ) {
        OutlinedTextField(
            value = breakTime,
            onValueChange = { breakTime = it },
            label = { Text("Break Time (mins) ") },

            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }

}