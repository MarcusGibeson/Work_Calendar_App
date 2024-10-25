package com.example.work_calendar_app.ui.composables.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog


@Composable
fun ColorPickerDialog(
    backgroundColor1: Color,
    backgroundColor2: Color,
    baseButtonColor: Color,
    detailsTextColor: Color,
    onDismissRequest: () -> Unit,
    onColorSelected: (Color) -> Unit,
    colorKey: String
) {
    //RGB State
    var red by remember { mutableStateOf(0f) }
    var green by remember { mutableStateOf(0f) }
    var blue by remember { mutableStateOf (0f) }

    //Define subtle gradient
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(backgroundColor1.toArgb()),
            Color(backgroundColor2.toArgb())
        ),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
    )

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
                .padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Pick a color", style = MaterialTheme.typography.headlineMedium)

                Spacer(modifier = Modifier.height(16.dp))

                //Show a preview of the color
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(red, green, blue)),
                )

                Spacer(modifier = Modifier.height(16.dp))

                //Red slider
                Text("Red")
                Slider(
                    value = red,
                    onValueChange = { red = it },
                    valueRange = 0f..1f,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = baseButtonColor,
                        activeTrackColor = detailsTextColor
                    )

                )

                //Green slider
                Text("Green")
                Slider(
                    value = green,
                    onValueChange = { green = it },
                    valueRange = 0f..1f,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = baseButtonColor,
                        activeTrackColor = detailsTextColor
                    )
                )

                //Blue slider
                Text("Blue")
                Slider(
                    value = blue,
                    onValueChange = { blue = it },
                    valueRange = 0f..1f,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = baseButtonColor,
                        activeTrackColor = detailsTextColor
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                //Select and Cancel buttons
                Row (
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = { onDismissRequest() },
                        colors = ButtonDefaults.buttonColors(baseButtonColor)
                    ) {
                        Text("Cancel")
                    }
                    Button(onClick = {
                        //Pass the selected color via the callback
                        val selectedColor = Color(red, green, blue)
                        onColorSelected(selectedColor)
                        onDismissRequest()
                    },
                        colors = ButtonDefaults.buttonColors(baseButtonColor)
                    ) {
                        Text("Select")
                    }
                }

            }
        }

    }
}