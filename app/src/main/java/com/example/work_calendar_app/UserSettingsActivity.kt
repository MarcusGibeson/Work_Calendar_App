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
import com.example.work_calendar_app.ui.composables.components.ColorPickerDialog
import com.example.work_calendar_app.ui.composables.components.ColorPickerDisplay


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
                    backgroundColor1,
                    backgroundColor2,
                    baseButtonColor,
                    detailsTextColor,
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
                                openColorPicker = ::openColorPicker,
                                saveColorPreference = ::saveColorPreference
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
            database.execSQL("DELETE FROM jobs")

            //Reset Autoincrement counters
            database.execSQL("DELETE FROM sqlite_sequence WHERE name = 'work_schedule'")
            database.execSQL("DELETE FROM sqlite_sequence WHERE name = 'saved_schedules'")
            database.execSQL("DELETE FROM sqlite_sequence WHERE name = 'jobs'")

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


}