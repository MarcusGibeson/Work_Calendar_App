package com.example.work_calendar_app.ui.composables.components.AddWorkEntry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SaveButtons(onSave: () -> Unit, onSaveSchedule: () -> Unit, onFinish: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =  Arrangement.SpaceBetween
    ) {
        Button(onClick = onSave) { Text("Save") }
        Button(onClick = onSaveSchedule) { Text("Save Schedule") }
        Button(onClick = onFinish) { Text("Cancel") }
    }
}