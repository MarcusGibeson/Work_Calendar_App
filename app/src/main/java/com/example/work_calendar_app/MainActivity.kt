package com.example.work_calendar_app

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.work_calendar_app.adapters.WorkDetailsAdapter
import com.example.work_calendar_app.data.WorkDetails
import com.example.work_calendar_app.database.WorkScheduleDatabaseHelper
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: WorkScheduleDatabaseHelper
    private lateinit var calendarView: CalendarView
    private lateinit var workDetailsRecyclerView: RecyclerView
    private lateinit var workDetailsAdapter: WorkDetailsAdapter

    class CustomItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.top = spacing
            outRect.bottom = spacing
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Initialize RecylclerView
        workDetailsRecyclerView = findViewById(R.id.work_details_recycler_view)
        workDetailsRecyclerView.layoutManager = LinearLayoutManager(this)
        workDetailsRecyclerView.addItemDecoration(CustomItemDecoration(20))
        workDetailsAdapter = WorkDetailsAdapter(mutableListOf())
        workDetailsRecyclerView.adapter = workDetailsAdapter

        dbHelper = WorkScheduleDatabaseHelper(this)
        loadAllWorkSchedules()
        calendarView = findViewById(R.id.calendarView)

        //Create the Date Range Picker
        val builder = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select work days")
            .setCalendarConstraints(
                CalendarConstraints.Builder()
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
            val selectedMonth = month + 1
            val selectedDate = String.format("%02d/%02d/%d", selectedMonth, dayOfMonth, year)
            //Query DB for work details on this date and display them
            showWorkDetailsForDate(selectedDate)
        }

    }

    private fun loadAllWorkSchedules() {
        val cursor = dbHelper.getAllWorkSchedule()
        val workDetailsList = mutableListOf<WorkDetails>()

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val workDate = cursor.getString(cursor.getColumnIndexOrThrow("work_date"))
                val startTime = cursor.getString(cursor.getColumnIndexOrThrow("start_time"))
                val endTime = cursor.getString(cursor.getColumnIndexOrThrow("end_time"))
                val wage = cursor.getDouble(cursor.getColumnIndexOrThrow("total_earnings"))

                val workDetail = WorkDetails(workDate, startTime, endTime, wage)
                workDetailsList.add(workDetail)

                Log.d("Main Activity", "Loaded WorkDetail: $workDetail")
            } while (cursor.moveToNext())

            workDetailsAdapter.updateData(workDetailsList)
            Log.d("MainActivity", "Total work schedules loaded: $(workDetailsList.size)")
        } else {
            Log.d("MainActivity", "No work schedules found.")
        }
    }

    private fun showWorkDetailsForDate(date: String) {
        //Log when function is called
        Log.d("MainActivity", "showWorkDetailsForDate called for date: $date")

        val cursor = dbHelper.getWorkScheduleByDate(date)

        //Log whether the cursor is null or not
        if (cursor == null) {
            Log.e("MainActivity", "Cursor is null. Database query failed for date: $date")
            Toast.makeText(this, "Error fetching data for the selected date.", Toast.LENGTH_SHORT).show()
            return
        }

        //Create a list to hold the work details
        val workDetailsList = mutableListOf<WorkDetails>()

        //Log the number of rows in the cursor
        Log.d("MainActivity", "Cursor row count: $(cursor.count)")

        //Check if the cursor has data
        if (cursor.moveToFirst()) {
            do {
                try {
                    //Extract the data from the cursor
                    val workDate = cursor.getString(cursor.getColumnIndexOrThrow("work_date"))
                    val startTime = cursor.getString(cursor.getColumnIndexOrThrow("start_time"))
                    val endTime = cursor.getString(cursor.getColumnIndexOrThrow("end_time"))
                    val wage = cursor.getDouble(cursor.getColumnIndexOrThrow("total_earnings"))

                    //Log the extracted data
                    Log.d("MainActivity", "Work Date: $workDate, Start time: $startTime, End Time: $endTime, Wage: $wage")

                    //Create a WorkDetails object and add it to the list
                    val workDetail = WorkDetails(workDate, startTime, endTime, wage)
                    workDetailsList.add(workDetail)

                    Log.d("MainActivity", "Loaded WorkDetail for $date: $workDetail")
                } catch (e: Exception) {
                    //Log any exceptions encountered while reading the cursor data
                    Log.e("MainActivity", "Error extracting work detail from cursor: ${e.message}")
                }
            } while (cursor.moveToNext())

            //Update the adapter with the new list of work details
            workDetailsAdapter.updateData(workDetailsList)
            Log.d("MainActivity", "Work details updated for date: $date, count: ${workDetailsList.size}")
        }else {
            //If no data is found for the date
            workDetailsAdapter.updateData(emptyList())
            Log.d("MainActivity", "No work details found for date: $date")
            Toast.makeText(this, "No work details for selected date.", Toast.LENGTH_SHORT).show()
        }
        //Close cursor to avoid memory leaks
        cursor.close()
    }

    private fun displayWorkTimesForSelectedDates(startDate: Long, endDate: Long) {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val start = dateFormat.format(Date(startDate))
        val end = dateFormat.format(Date(endDate))

        Log.d("MainActivity", "Fetching work details between $start and $end")

        //Query the database to fetch work times for each day between start and end
        val cursor = dbHelper.getWorkScheduleBetweenDates(start ,end)

        val workDetailsList = mutableListOf<WorkDetails>()
        //Iterate through the cursor to get work times and display them
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val workDate = cursor.getString(cursor.getColumnIndexOrThrow("work_date"))
                val startTime = cursor.getString(cursor.getColumnIndexOrThrow("start_time"))
                val endTime = cursor.getString(cursor.getColumnIndexOrThrow("end_time"))
                val wage = cursor.getDouble(cursor.getColumnIndexOrThrow("total_earnings"))

                //Display or append the work date and times to UI
                Log.d("MainActivity", "Loaded WorkDetail for range: $workDate, $startTime - $endTime, Wage: $wage")
                val workDetail = WorkDetails(workDate, startTime, endTime, wage)
                workDetailsList.add(workDetail)
            } while (cursor.moveToNext())

            //Update the adapter with the new list of work details
            workDetailsAdapter.updateData(workDetailsList)
            Log.d("MainActivity", "Work times displayed, count: ${workDetailsList.size}")
        } else {
            Log.d("MainActivity", "No work times found for the selected date range.")
            workDetailsAdapter.updateData(emptyList())
        }
    }
}