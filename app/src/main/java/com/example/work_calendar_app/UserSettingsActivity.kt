package com.example.work_calendar_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog


class UserSettingsActivity : AppCompatActivity() {

    //State variables for colors
    //Calendar
    private var workDay1Color by mutableStateOf(Color.Gray)
//    private var workDay2Color by mutableStateOf(Color.Gray)
//    private var workDay3Color by mutableStateOf(Color.Gray)
    private var outlineColor by mutableStateOf(Color.Gray)

    //App
    private var backgroundColor1 by mutableStateOf(Color.Blue)
    private var backgroundColor2 by mutableStateOf(Color.White)
    private var topBarColor by mutableStateOf(Color.Blue)
    private var baseTextColor by mutableStateOf(Color.Black)
    private var baseButtonColor by mutableStateOf(Color.Green)


    //Details
    private var detailsTextColor by mutableStateOf(Color.Black)
    private var detailsDateColor by mutableStateOf(Color.Blue)
    private var detailsWageColor by mutableStateOf(Color.Red)


    //State variable to control dialog visibility and the current color to edit
    private var showColorPicker by mutableStateOf(false)
    private var selectedColor: ((Color) -> Unit)? = null
    private var colorKey: String? = null


    companion object {
        const val WORK_DAY_1_COLOR_KEY = "workDay1Color"
        const val WORK_DAY_2_COLOR_KEY = "workDay2Color"
        const val WORK_DAY_3_COLOR_KEY = "workDay3Color"
        const val OUTLINE_COLOR_KEY = "outlineColor"
        const val BACKGROUND_COLOR_1_KEY = "backgroundColor1"
        const val BACKGROUND_COLOR_2_KEY = "backgroundColor2"
        const val TOP_BAR_COLOR_KEY = "topBarColor"
        const val BASE_TEXT_COLOR_KEY = "baseTextColor"
        const val BASE_BUTTON_COLOR_KEY = "baseButtonColor"
        const val DETAILS_TEXT_COLOR_KEY = "detailsTextColor"
        const val DETAILS_DATE_COLOR_KEY = "detailsDateColor"
        const val DETAILS_WAGE_COLOR_KEY = "detailsWageColor"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_settings)

        //Load color preferences
        loadColorPreferences()

        //Set up the action bar or toolbar
        supportActionBar?.title = "Settings"

        //Set up compose View for displaying color boxes
        val composeView = findViewById<ComposeView>(R.id.compose_view)
        composeView.setContent {
            val context = LocalContext.current
            if (showColorPicker) {
                ColorPickerDialog(
                    onDismissRequest = { showColorPicker = false },
                    onColorSelected = { color ->
                        selectedColor?.invoke(color)
                        saveColorPreference(colorKey!!, color.toArgb())
                        showColorPicker = false
                    },
                    colorKey = "color_key"
                )
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    ColorPickerDisplay(
                        workDay1Color,
//                      workDay2Color,
//                      workDay3Color,
                        outlineColor,
                        backgroundColor1,
                        backgroundColor2,
                        topBarColor,
                        baseTextColor,
                        baseButtonColor,
                        detailsTextColor,
                        detailsDateColor,
                        detailsWageColor,
                        onWorkDay1ColorSelected = { color -> openColorPicker(WORK_DAY_1_COLOR_KEY) { workDay1Color = color } },
//                      onWorkDay2ColorSelected = { color -> openColorPicker { workDay2Color = color } },
//                      onWorkDay3ColorSelected = { color -> openColorPicker { workDay3Color = color } },
                        onOutlineColorSelected = { color -> openColorPicker(OUTLINE_COLOR_KEY) { outlineColor = color } },
                        onBackgroundColor1Selected = { color -> openColorPicker(BACKGROUND_COLOR_1_KEY) { backgroundColor1 = color } },
                        onBackgroundColor2Selected = { color -> openColorPicker(BACKGROUND_COLOR_2_KEY) { backgroundColor2 = color } },
                        onTopBarColorSelected = { color -> openColorPicker(TOP_BAR_COLOR_KEY) { topBarColor = color } },
                        onBaseTextColorSelected = { color -> openColorPicker(BASE_TEXT_COLOR_KEY) { baseTextColor = color } },
                        onBaseButtonColorSelected = { color -> openColorPicker(BASE_BUTTON_COLOR_KEY) { baseButtonColor = color } },
                        onDetailsTextColorSelected = { color -> openColorPicker(DETAILS_TEXT_COLOR_KEY) { detailsTextColor = color } },
                        onDetailsDateColorSelected = { color -> openColorPicker(DETAILS_DATE_COLOR_KEY) { detailsDateColor = color } },
                        onDetailsWageColorSelected = { color -> openColorPicker(DETAILS_WAGE_COLOR_KEY) { detailsWageColor = color } },

                    )
                    Spacer (modifier = Modifier.height(16.dp))

                    Row {
                        //Add Reset defaults button
                        Button (
                            onClick = {
                                resetUserPreferences(context, ::updateUIState)
                            }
                        ) {
                            Text("Reset default")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        //Add finished button
                        Button(
                            onClick = {
                                finish()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Okay")
                        }
                    }




                }
            }
        }
    }

    private fun loadColorPreferences() {
        val sharedPreferences = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        workDay1Color = Color(sharedPreferences.getInt("workDay1Color", Color.Green.toArgb()))
//      workDay2Color = Color(sharedPreferences.getInt("workDay2Color", Color.Yellow.toArgb()))
//      workDay3Color = Color(sharedPreferences.getInt("workDay3Color", Color.Red.toArgb()))
        outlineColor = Color(sharedPreferences.getInt("outlineColor", Color.Blue.toArgb()))
        backgroundColor1 = Color(sharedPreferences.getInt("backgroundColor1", Color.Blue.toArgb()))
        backgroundColor2 =  Color(sharedPreferences.getInt("backgroundColor2", Color.White.toArgb()))
        topBarColor =  Color(sharedPreferences.getInt("topBarColor", Color.Blue.toArgb()))
        baseTextColor = Color(sharedPreferences.getInt("baseTextColor", Color.Black.toArgb()))
        baseButtonColor = Color(sharedPreferences.getInt("baseButtonColor", Color.Green.toArgb()))
        detailsTextColor = Color(sharedPreferences.getInt("detailsTextColor", Color.Black.toArgb()))
        detailsDateColor = Color(sharedPreferences.getInt("detailsDateColor", Color.Blue.toArgb()))
        detailsWageColor = Color(sharedPreferences.getInt("detailsWageColor", Color.Red.toArgb()))
    }
    private fun openColorPicker(key: String, onColorSelected: (Color) -> Unit) {
        colorKey = key
        selectedColor = onColorSelected
        showColorPicker = true
    }

    private fun saveColorPreference(key: String, color: Int) {
        val sharedPreferences = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt(key, color)
        editor.apply()
        Log.d("UserSettings", "Saved $color for key $key")

        //refresh color preferences
        loadColorPreferences()
    }

    private fun resetUserPreferences(context: Context, updateUIState: () -> Unit) {
        val sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.clear()

        editor.putInt("workDay1Color", Color.Yellow.toArgb())
        editor.putInt("outlineColor", Color.Blue.toArgb())
        editor.putInt("topBarColor", Color.Blue.toArgb())
        editor.putInt("backgroundColor1", Color.White.toArgb())
        editor.putInt("backgroundColor2", Color.White.toArgb())
        editor.putInt("baseTextColor", Color.Black.toArgb())
        editor.putInt("baseButtonColor", Color.Green.toArgb())
        editor.putInt("detailsTextColor", Color.Black.toArgb())
        editor.putInt("detailsDateColor", Color.Blue.toArgb())
        editor.putInt("detailsWageColor", Color.Red.toArgb())

        editor.apply()


        updateUIState()
    }

    fun updateUIState() {
        val sharedPreferences = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

        val newWorkDay1Color = sharedPreferences.getInt("workDay1Color", Color.Yellow.toArgb())
        val newOutlineColor = sharedPreferences.getInt("outlineColor", Color.Blue.toArgb())
        val newTopBarColor = sharedPreferences.getInt("topBarColor", Color.Blue.toArgb())
        val newBackgroundColor1 = sharedPreferences.getInt("backgroundColor1", Color.Gray.toArgb())
        val newBackgroundColor2 = sharedPreferences.getInt("backgroundColor2", Color.White.toArgb())
        val newBaseTextColor = sharedPreferences.getInt("baseTextColor", Color.Black.toArgb())
        val newBaseButtonColor = sharedPreferences.getInt("baseButtonColorr", Color.Green.toArgb())
        val newDetailsTextColor = sharedPreferences.getInt("detailsTextColor", Color.Black.toArgb())
        val newDetailsDateColor = sharedPreferences.getInt("detailsDateColor", Color.Blue.toArgb())
        val newDetailsWageColor = sharedPreferences.getInt("detailsWageColor", Color.Red.toArgb())

        workDay1Color = Color(newWorkDay1Color)
        outlineColor = Color(newOutlineColor)
        topBarColor = Color(newTopBarColor)
        backgroundColor1 = Color(newBackgroundColor1)
        backgroundColor2 = Color(newBackgroundColor2)
        baseTextColor = Color(newBaseTextColor)
        baseButtonColor = Color(newBaseButtonColor)
        detailsTextColor = Color(newDetailsTextColor)
        detailsDateColor = Color(newDetailsDateColor)
        detailsWageColor = Color(newDetailsWageColor)

    }

    @Composable
    fun ColorPickerDisplay(
        workDay1Color: Color,
//        workDay2Color: Color,
//        workDay3Color: Color,
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
//        onWorkDay2ColorSelected: (Color) -> Unit,
//        onWorkDay3ColorSelected: (Color) -> Unit,
        onOutlineColorSelected: (Color) -> Unit,
        onBackgroundColor1Selected: (Color) -> Unit,
        onBackgroundColor2Selected: (Color) -> Unit,
        onTopBarColorSelected: (Color) -> Unit,
        onBaseTextColorSelected: (Color) -> Unit,
        onBaseButtonColorSelected: (Color) -> Unit,
        onDetailsTextColorSelected: (Color) -> Unit,
        onDetailsDateColorSelected: (Color) -> Unit,
        onDetailsWageColorSelected: (Color) -> Unit
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            //Work Day 1
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = {
                    openColorPicker(WORK_DAY_1_COLOR_KEY) { color ->
                        onWorkDay1ColorSelected(color)
                        saveColorPreference(WORK_DAY_1_COLOR_KEY, color.toArgb())
                    }
                },
                    modifier = Modifier.weight(0.9f)
                ) {
                    Text("Change first job color")
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
//            //Work Day 2
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Button(onClick = {
//                    openColorPicker(WORK_DAY_2_COLOR_KEY) { color ->
//                        onWorkDay2ColorSelected(color)
//                        saveColorPreference(WORK_DAY_2_COLOR_KEY, color.toArgb())
//                    }
//                }) {
//                    Text("Change second job color")
//                }
//                Spacer(modifier = Modifier.width(16.dp))
//                Box(
//                    modifier = Modifier
//                        .size(50.dp)
//                        .background(workDay2Color, RoundedCornerShape(16.dp))
//                        .weight(1f, fill = false)
//                )
//            }
//            Spacer(modifier = Modifier.height(4.dp))
//
//            //Work Day 3
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Button(onClick = {
//                    openColorPicker(WORK_DAY_3_COLOR_KEY) { color ->
//                        onWorkDay3ColorSelected(color)
//                        saveColorPreference(WORK_DAY_3_COLOR_KEY, color.toArgb())
//                    }
//                }) {
//                    Text("Change third job color")
//                }
//                Spacer(modifier = Modifier.width(16.dp))
//                Box(
//                    modifier = Modifier
//                        .size(50.dp)
//                        .background(workDay3Color, RoundedCornerShape(16.dp))
//                        .weight(1f, fill = false)
//                )
//            }
//            Spacer(modifier = Modifier.height(4.dp))

            //Current Day outline
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = {
                    openColorPicker(OUTLINE_COLOR_KEY) { color ->
                        onOutlineColorSelected(color)
                        saveColorPreference(OUTLINE_COLOR_KEY, color.toArgb())
                    }
                },
                    modifier = Modifier.weight(0.5f)
                ) {
                    Text("Change current day outline color")
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
                    openColorPicker(BACKGROUND_COLOR_1_KEY) { color ->
                        onBackgroundColor1Selected(color)
                        saveColorPreference(BACKGROUND_COLOR_1_KEY, color.toArgb())
                    }
                },
                    modifier = Modifier.weight(0.5f)
                ) {
                    Text("Change Background 1 Color")
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
                    openColorPicker(BACKGROUND_COLOR_2_KEY) { color ->
                        onBackgroundColor2Selected(color)
                        saveColorPreference(BACKGROUND_COLOR_2_KEY, color.toArgb())
                    }
                },
                    modifier = Modifier.weight(0.5f)
                ) {
                    Text("Change Background 2 Color")
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
                    openColorPicker(TOP_BAR_COLOR_KEY) { color ->
                        onTopBarColorSelected(color)
                        saveColorPreference(TOP_BAR_COLOR_KEY, color.toArgb())
                    }
                },
                    modifier = Modifier.weight(0.5f)
                ) {
                    Text("Change Top Bar Color")
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
                    openColorPicker(BASE_TEXT_COLOR_KEY) { color ->
                        onBaseTextColorSelected(color)
                        saveColorPreference(BASE_TEXT_COLOR_KEY, color.toArgb())
                    }
                },
                    modifier = Modifier.weight(0.5f)
                ) {
                    Text("Change base text Color")
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
                    openColorPicker(BASE_BUTTON_COLOR_KEY) { color ->
                        onBaseButtonColorSelected(color)
                        saveColorPreference(BASE_BUTTON_COLOR_KEY, color.toArgb())
                    }
                },
                    modifier = Modifier.weight(0.5f)
                ) {
                    Text("Change base button Color")
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
                    openColorPicker(DETAILS_TEXT_COLOR_KEY) { color ->
                        onDetailsTextColorSelected(color)
                        saveColorPreference(DETAILS_TEXT_COLOR_KEY, color.toArgb())
                    }
                },
                    modifier = Modifier.weight(0.5f)
                ) {
                    Text("Change details text Color")
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
                    openColorPicker(DETAILS_DATE_COLOR_KEY) { color ->
                        onDetailsDateColorSelected(color)
                        saveColorPreference(DETAILS_DATE_COLOR_KEY, color.toArgb())
                    }
                },
                    modifier = Modifier.weight(0.5f)
                ) {
                    Text("Change details date Color")
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
                    openColorPicker(DETAILS_WAGE_COLOR_KEY) { color ->
                        onDetailsDateColorSelected(color)
                        saveColorPreference(DETAILS_WAGE_COLOR_KEY, color.toArgb())
                    }
                },
                    modifier = Modifier.weight(0.5f)
                ) {
                    Text("Change details wage Color")
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

    @Composable
    fun ColorPickerDialog(
        onDismissRequest: () -> Unit,
        onColorSelected: (Color) -> Unit,
        colorKey: String
    ) {
        //RGB State
        var red by remember { mutableStateOf(0f) }
        var green by remember { mutableStateOf(0f) }
        var blue by remember { mutableStateOf (0f) }

        Dialog(onDismissRequest = { onDismissRequest() }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
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
                        modifier = Modifier.fillMaxWidth()
                    )

                    //Green slider
                    Text("Green")
                    Slider(
                        value = green,
                        onValueChange = { green = it },
                        valueRange = 0f..1f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    //Blue slider
                    Text("Blue")
                    Slider(
                        value = blue,
                        onValueChange = { blue = it },
                        valueRange = 0f..1f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    //Select and Cancel buttons
                    Row (
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = { onDismissRequest() }) {
                            Text("Cancel")
                        }
                        Button(onClick = {
                            //Pass the selected color via the callback
                            val selectedColor = Color(red, green, blue)
                            onColorSelected(selectedColor)
                            onDismissRequest()
                        }) {
                            Text("Select")
                        }
                    }
                }
            }

        }
    }

}