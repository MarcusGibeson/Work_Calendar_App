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

class AddWorkActivity : AppCompatActivity() {

    private lateinit var workDate: EditText
    private lateinit var startTime: EditText
    private lateinit var endTime: EditText
    private lateinit var breakTime: EditText
    private lateinit var payType: Spinner
    private lateinit var hourlyRate: EditText
    private lateinit var overtimePay: EditText
    private lateinit var btnSave: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_work)

        //Initialize Values
        workDate = findViewById(R.id.workDate)
        startTime = findViewById(R.id.startTime)
        endTime = findViewById(R.id.endTime)
        breakTime = findViewById(R.id.breakTime)
        payType = findViewById(R.id.payTypeSpinner)
        hourlyRate = findViewById(R.id.payRate)
        overtimePay = findViewById(R.id.overtimePay)
        btnSave = findViewById(R.id.btnSave)

        //Date picker for work date
        workDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, {_, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                workDate.setText(formattedDate)
            }, year, month, day)

            datePickerDialog.show()
        }

        //Time Picker for start time
        startTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(this, {_, selectedHour, selectedMinute ->
                val formattedTime = String.format("%02d", selectedHour, selectedMinute)
                startTime.setText(formattedTime)
            }, hour, minute, true)

            timePickerDialog.show()
        }

        //Time Picker for end time
        endTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(this, {_, selectedHour, selectedMinute ->
                val formattedTime = String.format("%02d", selectedHour, selectedMinute)
                endTime.setText(formattedTime)
            }, hour, minute, true)

                timePickerDialog.show()
        }

        //Handle adding work schedule
        btnSave.setOnClickListener {
            val workDate = workDate.text.toString()
            val startTime = startTime.text.toString()
            val endTime = endTime.text.toString()
            val breakTime = breakTime.text.toString().toInt()
            val payType = payType.selectedItem.toString()
            val hourlyRate = hourlyRate.text.toString().toDouble()
            val overtimePay = overtimePay.text.toString().toDouble()
            val totalEarnings = calculateTotalEarnings(hourlyRate, startTime, endTime, breakTime, overtimePay)

            //Insert into database
            val dbHelper = WorkScheduleDatabaseHelper(this)
            dbHelper.insertWorkSchedule(workDate, startTime, endTime, breakTime, payType, hourlyRate, overtimePay, totalEarnings)

            Toast.makeText(this, "Work schedule added successfully!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun calculateTotalEarnings(hourlyRate: Double, startTime: String, endTime: String, breakTime: Int, overtimePay: Double): Double {
        //Logic to calculate total earnings, including break time and overtime
        val workHours = calculateHoursWorked(startTime, endTime) - (breakTime / 60)
        val overtimeHours = getOvertimeHours(workHours)
        return (workHours * hourlyRate) + (overtimeHours * overtimePay)
    }

    //Function to calculate hours worked
    private fun calculateHoursWorked(startTime: String, endTime: String): Double {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        val start = LocalTime.parse(startTime, timeFormatter)
        val end = LocalTime.parse(endTime, timeFormatter)

        var duration = Duration.between(start, end)

        //for overnight shifts
        if (duration.isNegative) {
            duration = duration.plusHours(24)
        }

        return duration.toMinutes() / 60.0
    }

    private fun getOvertimeHours(workHours: Double): Double {
        val standardHours = 8.0
        return if (workHours > standardHours) workHours - standardHours else 0.0
    }
}