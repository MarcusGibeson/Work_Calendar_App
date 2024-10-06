package com.example.work_calendar_app

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.work_calendar_app.database.WorkScheduleDatabaseHelper
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier

class AddWorkActivity : AppCompatActivity() {

    private lateinit var dbHelper: WorkScheduleDatabaseHelper
    private lateinit var savedScheduleSpinner: Spinner
    private lateinit var workDate: EditText
    private lateinit var startTime: EditText
    private lateinit var startTimeEditText: EditText
    private lateinit var endTime: EditText
    private lateinit var endTimeEditText: EditText
    private lateinit var breakTime: EditText
    private lateinit var breakTimeEditText: EditText
    private lateinit var payType: Spinner
    private lateinit var payTypeSpinner: Spinner
    private lateinit var payRate: EditText
    private lateinit var payRateEditText: EditText
    private lateinit var overtimePay: EditText
    private lateinit var overtimePayEditText: EditText
    private lateinit var btnSave: Button
    private lateinit var btnSaveSchedule: Button
    private lateinit var btnFinish: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_work)


        Log.d("AddWorkActivity", "Activity created, initializing views.")

        //Initialize Values
        dbHelper = WorkScheduleDatabaseHelper(this)
        savedScheduleSpinner = findViewById(R.id.savedScheduleSpinner)
        workDate = findViewById(R.id.workDate)
        startTime = findViewById(R.id.startTime)
        startTimeEditText = findViewById(R.id.startTime)
        endTime = findViewById(R.id.endTime)
        endTimeEditText = findViewById(R.id.endTime)
        breakTime = findViewById(R.id.breakTime)
        breakTimeEditText = findViewById(R.id.breakTime)
        payType = findViewById(R.id.payTypeSpinner)
        payTypeSpinner = findViewById(R.id.payTypeSpinner)
        payRate = findViewById(R.id.payRate)
        payRateEditText = findViewById(R.id.payRate)
        overtimePay = findViewById(R.id.overtimePay)
        overtimePayEditText = findViewById(R.id.overtimePay)
        btnSave = findViewById(R.id.btnSave)
        btnSaveSchedule = findViewById(R.id.saveScheduleButton)
        Log.d("AddWorkActivity", "Views initialized")

        btnFinish = findViewById(R.id.btnFinish)

        //Load saved schedules into the spinner
        loadSavedSchedulesIntoSpinner()

        //Set OnItemSelectedListener for savedScheduleSpinner
        savedScheduleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedScheduleName = parent.getItemAtPosition(position) as String
                loadScheduleDetails(selectedScheduleName)
            }

            override fun onNothingSelected(parent:AdapterView<*>?) {
                //Do nothing if no item is selected
            }
        }

        //Date picker for work date
        workDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, {_, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%d/%02d/%02d", selectedYear, selectedMonth + 1, selectedDay)
                workDate.setText(formattedDate)
                Log.d("AddWorkActivity", "Selected date: $formattedDate")
            }, year, month, day)

            datePickerDialog.show()
        }

        //Time Picker for start time
        startTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(this, {_, selectedHour, selectedMinute ->
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)

                val formattedTime = android.text.format.DateFormat.format("hh:mm a", calendar).toString()

                startTime.setText(formattedTime)
                Log.d("AddWorkActivity", "Selected start time: $formattedTime")
            }, hour, minute, false)

            timePickerDialog.show()
        }

        //Time Picker for end time
        endTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(this, {_, selectedHour, selectedMinute ->
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)

                val formattedTime = android.text.format.DateFormat.format("hh:mm a", calendar).toString()
                endTime.setText(formattedTime)
                Log.d("AddWorkActivity", "Selected end time: $formattedTime")
            }, hour, minute, false)

            timePickerDialog.show()
        }

        //Handle adding work schedule
        btnSave.setOnClickListener {
            val workDate = workDate.text.toString()
            val startTime = startTime.text.toString()
            val endTime = endTime.text.toString()
            val breakTime = breakTime.text.toString().toInt()
            val payType = payType.selectedItem.toString()
            val payRate = payRate.text.toString().toDouble()
            val overtimePay = overtimePay.text.toString().toDouble()
            val totalEarnings = calculateTotalEarnings(payRate, startTime, endTime, breakTime, overtimePay)

            Log.d("AddWorkActivity", "Saving Work Schedule: Date: $workDate, Start time: $startTime, End time: $endTime, Break Time: $breakTime, Pay Type: $payType, Hourly Rate: $payRate, Overtime Pay: $overtimePay, Total Earnings: $totalEarnings ")
            //Insert into database
            val dbHelper = WorkScheduleDatabaseHelper(this)
            dbHelper.insertWorkSchedule(null, workDate, startTime, endTime, breakTime, payType, payRate, overtimePay, totalEarnings)

            Toast.makeText(this, "Work schedule added successfully!", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        }

        //Save Schedule into database for later use
        btnSaveSchedule.setOnClickListener {
            val startTime = startTime.text.toString()
            val endTime = endTime.text.toString()
            val breakTime = breakTime.text.toString().toInt()
            val payType = payTypeSpinner.selectedItem.toString()
            val hourlyRate = payRate.text.toString().toDouble()
            val overtimePay = overtimePay.text.toString().toDouble()

            //Use StartTime - EndTime as schedule name
            val scheduleName = "$startTime - $endTime"

            //Insert into saved schedules database
            dbHelper.insertSavedSchedule(scheduleName, startTime, endTime, breakTime, payType, hourlyRate, overtimePay)

            Toast.makeText(this, "Schedule saved as $scheduleName", Toast.LENGTH_SHORT).show()
        }

        btnFinish.setOnClickListener {
            finish()
        }
    }

    private fun calculateTotalEarnings(hourlyRate: Double, startTime: String, endTime: String, breakTime: Int, overtimePay: Double): Double {
        //Logic to calculate total earnings, including break time and overtime
        val workHours = calculateHoursWorked(startTime, endTime) - (breakTime / 60)
        val overtimeHours = getOvertimeHours(workHours)
        val totalEarnings = (workHours * hourlyRate) + (overtimeHours * overtimePay)
        Log.d("Add Work Activity", "Calculated Total Earnings: $totalEarnings (Work Hours: $workHours, Overtime hours: $overtimeHours)")
        return totalEarnings
    }

    //Function to calculate hours worked
    private fun calculateHoursWorked(startTime: String, endTime: String): Double {
        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

        val start = LocalTime.parse(startTime, timeFormatter)
        val end = LocalTime.parse(endTime, timeFormatter)

        var duration = Duration.between(start, end)

        //for overnight shifts
        if (duration.isNegative) {
            duration = duration.plusHours(24)
        }

        val hoursWorked = duration.toMinutes() / 60.0
        Log.d("AddWorkActivity", "Calculated hours worked: $hoursWorked (Start: $startTime, End: $endTime)")
        return hoursWorked
    }

    private fun getOvertimeHours(workHours: Double): Double {
        val standardHours = 8.0
        return if (workHours > standardHours) workHours - standardHours else 0.0
    }

    //Spinner to pick a saved schedule
    private fun loadSavedSchedulesIntoSpinner() {
        val cursor = dbHelper.getAllSavedSchedules()
        val scheduleNames = mutableListOf<String>()

        //Add empty option as default
        scheduleNames.add("")

        if(cursor.moveToFirst()) {
            do {
                val scheduleName = cursor.getString(cursor.getColumnIndexOrThrow("schedule_name"))
                scheduleNames.add(scheduleName)
            } while (cursor.moveToNext())
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, scheduleNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        savedScheduleSpinner.adapter = adapter
    }

    private fun loadScheduleDetails(scheduleName: String) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM saved_schedules WHERE schedule_name = ?",arrayOf(scheduleName))

        if (cursor.moveToFirst()) {
            val startTime = cursor.getString(cursor.getColumnIndexOrThrow("start_time"))
            val endTime = cursor.getString(cursor.getColumnIndexOrThrow("end_time"))
            val breakTime = cursor.getInt(cursor.getColumnIndexOrThrow("break_time_minutes"))
            val payTypeFromDB = cursor.getString(cursor.getColumnIndexOrThrow("pay_type"))
            val payRate = cursor.getDouble(cursor.getColumnIndexOrThrow("pay_rate"))
            val overtimeRate = cursor.getDouble(cursor.getColumnIndexOrThrow("overtime_rate"))

            //Populate the fields with the loaded data
            startTimeEditText.setText(startTime)
            endTimeEditText.setText(endTime)
            breakTimeEditText.setText(breakTime.toString())
            payRateEditText.setText(payRate.toString())
            overtimePayEditText.setText(overtimeRate.toString())

            //Set the selected pay type in the spinner
            val payTypes = arrayOf("Hourly", "Comission", "Tips")
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, payTypes)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            payTypeSpinner.adapter = adapter

            //Find the position of the value from the database and set the selection
            val spinnerPosition = adapter.getPosition(payTypeFromDB)
            if (spinnerPosition >= 0) {
                payTypeSpinner.setSelection(spinnerPosition)
            }
        }
        cursor.close()
    }
}