package com.example.work_calendar_app.ui.composables.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.work_calendar_app.UserSettingsActivity
import com.example.work_calendar_app.UserSettingsActivity.*


@Composable
fun ColorPickerDisplay(
    workDay1Color: Color,
    workDay2Color: Color,
    workDay3Color: Color,
    workDay4Color: Color,
    outlineColor: Color,
    backgroundColor1: Color,
    backgroundColor2: Color,
    topBarColor: Color,
    baseTextColor: Color,
    baseButtonColor: Color,
    detailsTextColor: Color,
    detailsDateColor: Color,
    detailsWageColor: Color,
    onWorkDay1ColorSelected: (Color) -> Unit,
    onWorkDay2ColorSelected: (Color) -> Unit,
    onWorkDay3ColorSelected: (Color) -> Unit,
    onWorkDay4ColorSelected: (Color) -> Unit,
    onOutlineColorSelected: (Color) -> Unit,
    onBackgroundColor1Selected: (Color) -> Unit,
    onBackgroundColor2Selected: (Color) -> Unit,
    onTopBarColorSelected: (Color) -> Unit,
    onBaseTextColorSelected: (Color) -> Unit,
    onBaseButtonColorSelected: (Color) -> Unit,
    onDetailsTextColorSelected: (Color) -> Unit,
    onDetailsDateColorSelected: (Color) -> Unit,
    onDetailsWageColorSelected: (Color) -> Unit,
    openColorPicker: (String, (Color) -> Unit) -> Unit,
    saveColorPreference: (String, Int) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        //Work Day 1
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                openColorPicker(UserSettingsActivity.WORK_DAY_1_COLOR_KEY) { color ->
                    onWorkDay1ColorSelected(color)
                    saveColorPreference(UserSettingsActivity.WORK_DAY_1_COLOR_KEY, color.toArgb())
                }
            },
                modifier = Modifier.weight(0.9f),
                colors = ButtonDefaults.buttonColors(baseButtonColor)
            ) {
                Text("Change first job color", color = detailsTextColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(workDay1Color, RoundedCornerShape(16.dp))
                    .weight(0.2f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        //Work Day 2
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                openColorPicker(UserSettingsActivity.WORK_DAY_2_COLOR_KEY) { color ->
                    onWorkDay2ColorSelected(color)
                    saveColorPreference(UserSettingsActivity.WORK_DAY_2_COLOR_KEY, color.toArgb())
                }
            },
                modifier = Modifier.weight(0.9f),
                colors = ButtonDefaults.buttonColors(baseButtonColor)
            ) {
                Text("Change second job color", color = detailsTextColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(workDay2Color, RoundedCornerShape(16.dp))
                    .weight(0.2f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        //Work Day 3
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                openColorPicker(UserSettingsActivity.WORK_DAY_3_COLOR_KEY) { color ->
                    onWorkDay3ColorSelected(color)
                    saveColorPreference(UserSettingsActivity.WORK_DAY_3_COLOR_KEY, color.toArgb())
                }
            },
                modifier = Modifier.weight(0.9f),
                colors = ButtonDefaults.buttonColors(baseButtonColor)
            ) {
                Text("Change third job color", color = detailsTextColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(workDay3Color, RoundedCornerShape(16.dp))
                    .weight(0.2f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        //Work Day 4
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                openColorPicker(UserSettingsActivity.WORK_DAY_4_COLOR_KEY) { color ->
                    onWorkDay4ColorSelected(color)
                    saveColorPreference(UserSettingsActivity.WORK_DAY_4_COLOR_KEY, color.toArgb())
                }
            },
                modifier = Modifier.weight(0.9f),
                colors = ButtonDefaults.buttonColors(baseButtonColor)
            ) {
                Text("Change fourth job color", color = detailsTextColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(workDay4Color, RoundedCornerShape(16.dp))
                    .weight(0.2f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        //Current Day outline
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                openColorPicker(UserSettingsActivity.OUTLINE_COLOR_KEY) { color ->
                    onOutlineColorSelected(color)
                    saveColorPreference(UserSettingsActivity.OUTLINE_COLOR_KEY, color.toArgb())
                }
            },
                colors = ButtonDefaults.buttonColors(baseButtonColor),
                modifier = Modifier.weight(0.5f)
            ) {
                Text("Change current day outline color", color = detailsTextColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(outlineColor, RoundedCornerShape(16.dp))
                    .weight(0.1f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        //Background Color 1
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                openColorPicker(UserSettingsActivity.BACKGROUND_COLOR_1_KEY) { color ->
                    onBackgroundColor1Selected(color)
                    saveColorPreference(UserSettingsActivity.BACKGROUND_COLOR_1_KEY, color.toArgb())
                }
            },
                colors = ButtonDefaults.buttonColors(baseButtonColor),
                modifier = Modifier.weight(0.5f)
            ) {
                Text("Change Background 1 Color", color = detailsTextColor)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(backgroundColor1, RoundedCornerShape(16.dp))
                    .weight(0.1f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        //Background Color 2
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                openColorPicker(UserSettingsActivity.BACKGROUND_COLOR_2_KEY) { color ->
                    onBackgroundColor2Selected(color)
                    saveColorPreference(UserSettingsActivity.BACKGROUND_COLOR_2_KEY, color.toArgb())
                }
            },
                modifier = Modifier.weight(0.5f),
                colors = ButtonDefaults.buttonColors(baseButtonColor)
            ) {
                Text("Change Background 2 Color", color = detailsTextColor)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(backgroundColor2, RoundedCornerShape(16.dp))
                    .weight(0.1f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        //Top Bar Color
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                openColorPicker(UserSettingsActivity.TOP_BAR_COLOR_KEY) { color ->
                    onTopBarColorSelected(color)
                    saveColorPreference(UserSettingsActivity.TOP_BAR_COLOR_KEY, color.toArgb())
                }
            },
                modifier = Modifier.weight(0.5f),
                colors = ButtonDefaults.buttonColors(baseButtonColor)
            ) {
                Text("Change Top Bar Color", color = detailsTextColor)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(topBarColor, RoundedCornerShape(16.dp))
                    .weight(0.1f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        //Base Text Color
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                openColorPicker(UserSettingsActivity.BASE_TEXT_COLOR_KEY) { color ->
                    onBaseTextColorSelected(color)
                    saveColorPreference(UserSettingsActivity.BASE_TEXT_COLOR_KEY, color.toArgb())
                }
            },
                modifier = Modifier.weight(0.5f),
                colors = ButtonDefaults.buttonColors(baseButtonColor)
            ) {
                Text("Change base text Color", color = detailsTextColor)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(baseTextColor, RoundedCornerShape(16.dp))
                    .weight(0.1f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        //Base Button Color
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                openColorPicker(UserSettingsActivity.BASE_BUTTON_COLOR_KEY) { color ->
                    onBaseButtonColorSelected(color)
                    saveColorPreference(UserSettingsActivity.BASE_BUTTON_COLOR_KEY, color.toArgb())
                }
            },
                modifier = Modifier.weight(0.5f),
                colors = ButtonDefaults.buttonColors(baseButtonColor)
            ) {
                Text("Change base button Color", color = detailsTextColor)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(baseButtonColor, RoundedCornerShape(16.dp))
                    .weight(0.1f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        //Details Text Color
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                openColorPicker(UserSettingsActivity.DETAILS_TEXT_COLOR_KEY) { color ->
                    onDetailsTextColorSelected(color)
                    saveColorPreference(UserSettingsActivity.DETAILS_TEXT_COLOR_KEY, color.toArgb())
                }
            },
                modifier = Modifier.weight(0.5f),
                colors = ButtonDefaults.buttonColors(baseButtonColor)
            ) {
                Text("Change details text Color", color = detailsTextColor)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(detailsTextColor, RoundedCornerShape(16.dp))
                    .weight(0.1f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        //Details Date Color
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                openColorPicker(UserSettingsActivity.DETAILS_DATE_COLOR_KEY) { color ->
                    onDetailsDateColorSelected(color)
                    saveColorPreference(UserSettingsActivity.DETAILS_DATE_COLOR_KEY, color.toArgb())
                }
            },
                modifier = Modifier.weight(0.5f),
                colors = ButtonDefaults.buttonColors(baseButtonColor)
            ) {
                Text("Change details date Color", color = detailsTextColor)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(detailsDateColor, RoundedCornerShape(16.dp))
                    .weight(0.1f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        //Details Wage Color
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                openColorPicker(UserSettingsActivity.DETAILS_WAGE_COLOR_KEY) { color ->
                    onDetailsDateColorSelected(color)
                    saveColorPreference(UserSettingsActivity.DETAILS_WAGE_COLOR_KEY, color.toArgb())
                }
            },
                modifier = Modifier.weight(0.5f),
                colors = ButtonDefaults.buttonColors(baseButtonColor)
            ) {
                Text("Change details wage Color", color = detailsTextColor)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(detailsWageColor, RoundedCornerShape(16.dp))
                    .weight(0.1f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}