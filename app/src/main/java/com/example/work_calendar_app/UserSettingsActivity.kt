package com.example.work_calendar_app

import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class UserSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_settings)

        //Set up the action bar or toolbar
        supportActionBar?.title = "Settings"

        //Buttons to choose colors
        val workDay1ColorButton: Button = findViewById(R.id.button_work_day_1_color)
        val workDay2ColorButton: Button = findViewById(R.id.button_work_day_2_color)
        val workDay3ColorButton: Button = findViewById(R.id.button_work_day_3_color)
        val currentDayOutlineColorButton: Button = findViewById(R.id.button_outline_color)

        //Open color picker dialog or predefined color options for workday background
        currentDayOutlineColorButton.setOnClickListener {
            openColorPicker { color ->
                saveColorPreference("outlineColor", color)
            }
        }

        workDay1ColorButton.setOnClickListener {
            openColorPicker { color ->
                saveColorPreference("workDay1Color", color)
            }
        }

        workDay2ColorButton.setOnClickListener {
            openColorPicker { color ->
                saveColorPreference("workDay2Color", color)
            }
        }

        workDay3ColorButton.setOnClickListener {
            openColorPicker { color ->
                saveColorPreference("workDay3Color", color)
            }
        }
    }

    private fun openColorPicker(onColorSelected: (Int) -> Unit) {
        // Open color picker dialog (using ColorPicker library or custom implementation)
        // Once color is selected, return it via the callback
        //onColorSelected(selectedColor)
    }

    private fun saveColorPreference(key: String, color: Int) {
        val sharedPreferences = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt(key, color).apply()
    }

}