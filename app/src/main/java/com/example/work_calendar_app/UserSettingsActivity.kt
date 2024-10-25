package com.example.work_calendar_app

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.work_calendar_app.data.database.WorkScheduleDatabaseHelper


class UserSettingsActivity : AppCompatActivity() {

    private lateinit var dbHelper: WorkScheduleDatabaseHelper

    //State variables for colors
    //Calendar
    private var workDay1Color by mutableStateOf(Color.Yellow)
    private var workDay2Color by mutableStateOf(Color.Red)
    private var workDay3Color by mutableStateOf(Color.Green)
    private var workDay4Color by mutableStateOf(Color.White)
    private var outlineColor by mutableStateOf(Color.Blue)

    //App
    private var backgroundColor1 by mutableStateOf(Color(143, 216, 230))
    private var backgroundColor2 by mutableStateOf(Color.White)
    private var topBarColor by mutableStateOf(Color.Blue)
    private var baseTextColor by mutableStateOf(Color.Black)
    private var baseButtonColor by mutableStateOf(Color(204, 153, 255))


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
        const val WORK_DAY_4_COLOR_KEY = "workDay4Color"
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

        //Check if the activity was started to reset preferences
        val resetPreferences = intent.getBooleanExtra("resetPreferences", false)
        if (resetPreferences) {
            resetUserPreferences(this) {
                finish()
            }
        }

        setContentView(R.layout.activity_user_settings)

        //Load color preferences
        loadColorPreferences()

        //Set up the action bar or toolbar
        supportActionBar?.title = "Settings"

        updateUIState()

        //Set up compose View for displaying color boxes
        val composeView = findViewById<ComposeView>(R.id.compose_view)
        composeView.setContent {
            val context = LocalContext.current
            var showDialog by remember { mutableStateOf(false) }

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
                    //Scrollable Content
                    Box(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(bottom = 16.dp)
                        ) {
                            ColorPickerDisplay(
                                workDay1Color,
                                workDay2Color,
                                workDay3Color,
                                workDay4Color,
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
                                onWorkDay2ColorSelected = { color -> openColorPicker(WORK_DAY_2_COLOR_KEY) { workDay2Color = color } },
                                onWorkDay3ColorSelected = { color -> openColorPicker(WORK_DAY_3_COLOR_KEY) { workDay3Color = color } },
                                onWorkDay4ColorSelected = { color -> openColorPicker(WORK_DAY_4_COLOR_KEY) { workDay4Color = color } },
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
                        }
                    }

                    Spacer (modifier = Modifier.height(16.dp))
                    Column {
                        Row {
                            //Add Reset defaults button
                            Button (
                                onClick = {
                                    resetUserPreferences(context, ::updateUIState)
                                },
                                colors = ButtonDefaults.buttonColors(baseButtonColor)
                            ) {
                                Text("Reset default", color = detailsTextColor)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            //Add finished button
                            Button(
                                onClick = {
                                    finish()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(baseButtonColor)
                            ) {
                                Text("Okay", color = detailsTextColor)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        //Reset database
                        Row (horizontalArrangement = Arrangement.Center) {
                            Button(
                                onClick = { showDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(baseButtonColor)
                            ) {
                                Text("Clear database)")
                            }

                            if (showDialog) {
                                ConfirmClearDatabaseDialog(
                                    onConfirm = {
                                        clearDatabase(context)
                                        showDialog = false
                                    },
                                    onDismiss = { showDialog = false }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ConfirmClearDatabaseDialog(
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Confirm Action") },
            text = { Text("Are you sure you want to clear the database? This action cannot be reversed.") },
            confirmButton = {
                TextButton(
                    onClick = onConfirm
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("No")
                }
            }
        )
    }


    private fun clearDatabase(context: Context) {

        val dbHelper = WorkScheduleDatabaseHelper(context)
        val database = dbHelper.writableDatabase

        database.beginTransaction()
        try{
            database.execSQL("DELETE FROM work_schedule")
            database.execSQL("DELETE FROM saved_schedules")

            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
            database.close()
        }
    }

    private fun loadColorPreferences() {
        val sharedPreferences = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        workDay1Color = Color(sharedPreferences.getInt("workDay1Color", Color.Yellow.toArgb()))
        workDay2Color = Color(sharedPreferences.getInt("workDay2Color", Color.Red.toArgb()))
        workDay3Color = Color(sharedPreferences.getInt("workDay3Color", Color.Green.toArgb()))
        workDay4Color = Color(sharedPreferences.getInt("workDay4Color", Color.White.toArgb()))
        outlineColor = Color(sharedPreferences.getInt("outlineColor", Color.Blue.toArgb()))
        backgroundColor1 = Color(sharedPreferences.getInt("backgroundColor1", Color(143, 216, 230).toArgb()))
        backgroundColor2 =  Color(sharedPreferences.getInt("backgroundColor2", Color.White.toArgb()))
        topBarColor =  Color(sharedPreferences.getInt("topBarColor", Color.Blue.toArgb()))
        baseTextColor = Color(sharedPreferences.getInt("baseTextColor", Color.Black.toArgb()))
        baseButtonColor = Color(sharedPreferences.getInt("baseButtonColor", Color(204, 153, 255).toArgb()))
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
        editor.putInt("workDay2Color", Color.Red.toArgb())
        editor.putInt("workDay3Color", Color.Green.toArgb())
        editor.putInt("workDay4Color", Color.White.toArgb())
        editor.putInt("outlineColor", Color.Blue.toArgb())
        editor.putInt("topBarColor", Color.Blue.toArgb())
        editor.putInt("backgroundColor1", Color(143, 216, 230).toArgb()) //light blue
        editor.putInt("backgroundColor2", Color.White.toArgb())
        editor.putInt("baseTextColor", Color.Black.toArgb())
        editor.putInt("baseButtonColor", Color(204, 153, 255).toArgb()) //light purple
        editor.putInt("detailsTextColor", Color.Black.toArgb())
        editor.putInt("detailsDateColor", Color.Blue.toArgb())
        editor.putInt("detailsWageColor", Color.Red.toArgb())

        editor.apply()


        updateUIState()
    }

    fun updateUIState() {
        val sharedPreferences = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

        val newWorkDay1Color = sharedPreferences.getInt("workDay1Color", Color.Yellow.toArgb())
        val newWorkDay2Color = sharedPreferences.getInt("workDay2Color", Color.Red.toArgb())
        val newWorkDay3Color = sharedPreferences.getInt("workDay3Color", Color.Green.toArgb())
        val newWorkDay4Color = sharedPreferences.getInt("workDay4Color", Color.White.toArgb())
        val newOutlineColor = sharedPreferences.getInt("outlineColor", Color.Blue.toArgb())
        val newTopBarColor = sharedPreferences.getInt("topBarColor", Color.Blue.toArgb())
        val newBackgroundColor1 = sharedPreferences.getInt("backgroundColor1", Color(143, 216, 230).toArgb())
        val newBackgroundColor2 = sharedPreferences.getInt("backgroundColor2", Color.White.toArgb())
        val newBaseTextColor = sharedPreferences.getInt("baseTextColor", Color.Black.toArgb())
        val newBaseButtonColor = sharedPreferences.getInt("baseButtonColor", Color(204, 153, 255).toArgb())
        val newDetailsTextColor = sharedPreferences.getInt("detailsTextColor", Color.Black.toArgb())
        val newDetailsDateColor = sharedPreferences.getInt("detailsDateColor", Color.Blue.toArgb())
        val newDetailsWageColor = sharedPreferences.getInt("detailsWageColor", Color.Red.toArgb())

        workDay1Color = Color(newWorkDay1Color)
        workDay2Color = Color(newWorkDay2Color)
        workDay3Color = Color(newWorkDay3Color)
        workDay4Color = Color(newWorkDay4Color)
        outlineColor = Color(newOutlineColor)
        topBarColor = Color(newTopBarColor)
        backgroundColor1 = Color(newBackgroundColor1)
        backgroundColor2 = Color(newBackgroundColor2)
        baseTextColor = Color(newBaseTextColor)
        baseButtonColor = Color(newBaseButtonColor)
        detailsTextColor = Color(newDetailsTextColor)
        detailsDateColor = Color(newDetailsDateColor)
        detailsWageColor = Color(newDetailsWageColor)

        //Set background colors in Gradient
        val gradientDrawable = GradientDrawable(GradientDrawable.Orientation.TR_BL, intArrayOf(backgroundColor1.toArgb(), backgroundColor2.toArgb()))
        findViewById<View>(R.id.rootLayout).background = gradientDrawable

    }

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
                    openColorPicker(WORK_DAY_2_COLOR_KEY) { color ->
                        onWorkDay2ColorSelected(color)
                        saveColorPreference(WORK_DAY_2_COLOR_KEY, color.toArgb())
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
                    openColorPicker(WORK_DAY_3_COLOR_KEY) { color ->
                        onWorkDay3ColorSelected(color)
                        saveColorPreference(WORK_DAY_3_COLOR_KEY, color.toArgb())
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
                    openColorPicker(WORK_DAY_4_COLOR_KEY) { color ->
                        onWorkDay4ColorSelected(color)
                        saveColorPreference(WORK_DAY_4_COLOR_KEY, color.toArgb())
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
                    openColorPicker(OUTLINE_COLOR_KEY) { color ->
                        onOutlineColorSelected(color)
                        saveColorPreference(OUTLINE_COLOR_KEY, color.toArgb())
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
                    openColorPicker(BACKGROUND_COLOR_1_KEY) { color ->
                        onBackgroundColor1Selected(color)
                        saveColorPreference(BACKGROUND_COLOR_1_KEY, color.toArgb())
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
                    openColorPicker(BACKGROUND_COLOR_2_KEY) { color ->
                        onBackgroundColor2Selected(color)
                        saveColorPreference(BACKGROUND_COLOR_2_KEY, color.toArgb())
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
                    openColorPicker(TOP_BAR_COLOR_KEY) { color ->
                        onTopBarColorSelected(color)
                        saveColorPreference(TOP_BAR_COLOR_KEY, color.toArgb())
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
                    openColorPicker(BASE_TEXT_COLOR_KEY) { color ->
                        onBaseTextColorSelected(color)
                        saveColorPreference(BASE_TEXT_COLOR_KEY, color.toArgb())
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
                    openColorPicker(BASE_BUTTON_COLOR_KEY) { color ->
                        onBaseButtonColorSelected(color)
                        saveColorPreference(BASE_BUTTON_COLOR_KEY, color.toArgb())
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
                    openColorPicker(DETAILS_TEXT_COLOR_KEY) { color ->
                        onDetailsTextColorSelected(color)
                        saveColorPreference(DETAILS_TEXT_COLOR_KEY, color.toArgb())
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
                    openColorPicker(DETAILS_DATE_COLOR_KEY) { color ->
                        onDetailsDateColorSelected(color)
                        saveColorPreference(DETAILS_DATE_COLOR_KEY, color.toArgb())
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
                    openColorPicker(DETAILS_WAGE_COLOR_KEY) { color ->
                        onDetailsDateColorSelected(color)
                        saveColorPreference(DETAILS_WAGE_COLOR_KEY, color.toArgb())
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
}