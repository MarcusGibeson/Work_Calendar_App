package com.example.work_calendar_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CalendarView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.work_calendar_app.adapters.WorkDetailsAdapter
import com.example.work_calendar_app.database.WorkScheduleDatabaseHelper
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: WorkScheduleDatabaseHelper
    private lateinit var calendarView: CalendarView
    private lateinit var workDetailsRecyclerView: RecyclerView
    private lateinit var workDetailsAdapter: WorkDetailsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Initialize RecylclerView
        workDetailsRecyclerView = findViewById(R.id.work_details_recycler_view)
        workDetailsRecyclerView.layoutManager = LinearLayoutManager(this)
        workDetailsAdapter = WorkDetailsAdapter(mutableListOf())
        workDetailsRecyclerView.adapter = workDetailsAdapter

        dbHelper = WorkScheduleDatabaseHelper(this)
        calendarView = findViewById(R.id.calendarView)

        //Create the Date Range Picker
        val builder = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select work days")
            .setCalendarConstraints(
                CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointForward.now())
                    .build()
            )
        val datePicker = builder.build()

        //Show date picker when needed
        findViewById<Button>(R.id.select_dates_button).setOnClickListener{
            datePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
        }

        //Handle the selected dates
        datePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = selection.first
            val endDate = selection.second

            //Display the selected date range
            Toast.makeText(this, "Selected: $startDate to $endDate", Toast.LENGTH_LONG).show()

            //Add logic to display the time worked for each day in the selected range
            displayWorkTimesForSelectedDates(startDate, endDate)
        }

        //Add work schedule button
        findViewById<FloatingActionButton>(R.id.addWorkFab).setOnClickListener {
            val intent = Intent(this, AddWorkActivity::class.java)
            startActivity(intent)
        }

        //Calendar item click listener to show work details
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val selectedDate = "$dayOfMonth/${month}/$year"
            //Query DB for work details on this date and display them
            showWorkDetailsForDate(selectedDate)
        }
    }

    private fun showWorkDetailsForDate(date: String) {
        val cursor = dbHelper.getWorkSchedule()
    }

    private fun displayWorkTimesForSelectedDates(startDate: Long, endDate: Long) {
        val start = Date(startDate)
        val end = Date(endDate)

        //Query the database to fetch work times for each day between start and end
        val cursor = dbHelper.getWorkScheduleBetweenDates(start ,end)

        //Iterate through the cursor to get work times and display them
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val workDate = cursor.getString(cursor.getColumnIndexOrThrow("workDate"))
                val startTime = cursor.getString(cursor.getColumnIndexOrThrow("startTime"))
                val endTime = cursor.getString(cursor.getColumnIndexOrThrow("endTime"))

                //Display or append the work date and times to UI
                Log.d("MainActivity", "Work on $workDate: $startTime - $endTime")
            } while (cursor.moveToNext())
        }
    }
}