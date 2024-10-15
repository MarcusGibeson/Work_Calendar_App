package com.example.work_calendar_app

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.work_calendar_app.database.WorkScheduleDatabaseHelper
import java.util.Calendar
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.work_calendar_app.data.adapters.CommissionDetailsAdapter

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
    private lateinit var payRateText: TextView
    private lateinit var payRateEditText: EditText
    private lateinit var overtimePay: EditText
    private lateinit var overtimePayText: TextView
    private lateinit var overtimePayEditText: EditText
    private lateinit var commissionRate: EditText
    private lateinit var commissionRateText: TextView
    private lateinit var commissionRateEditText: EditText
    private lateinit var commissionDetail: TextView
    private lateinit var commissionDetailEditText: EditText
    private lateinit var commissionDetailIcon: ImageButton
    private lateinit var commissionDetailsRecyclerView: RecyclerView
    private val commissionDetailsList = mutableListOf<Double>()
    private lateinit var adapter: CommissionDetailsAdapter
    private lateinit var salaryAmount: EditText
    private lateinit var salaryAmountText: TextView
    private lateinit var salaryAmountEditText: EditText
    private lateinit var tips: EditText
    private lateinit var tipsEditText: EditText
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
        payRateText = findViewById(R.id.payRateText)
        payRateEditText = findViewById(R.id.payRate)
        overtimePay = findViewById(R.id.overtimePay)
        overtimePayText = findViewById(R.id.overtimePayText)
        overtimePayEditText = findViewById(R.id.overtimePay)
        commissionRate = findViewById(R.id.commissionRate)
        commissionRateText = findViewById(R.id.commissionRateText)
        commissionRateEditText = findViewById(R.id.commissionRate)
        commissionDetail = findViewById(R.id.commissionDetailText)
        commissionDetailEditText = findViewById(R.id.commissionDetail)
        commissionDetailIcon = findViewById(R.id.addCommissionDetailButton)
        commissionDetailsRecyclerView = findViewById(R.id.commissionDetailsRecyclerView)
        val recyclerView = findViewById<RecyclerView>(R.id.commissionDetailsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = CommissionDetailsAdapter(commissionDetailsList)
        recyclerView.adapter = adapter

        salaryAmount = findViewById(R.id.salaryAmount)
        salaryAmountText = findViewById(R.id.salaryAmountText)
        salaryAmountEditText = findViewById(R.id.salaryAmount)
        tips = findViewById(R.id.tips)
        tipsEditText = findViewById(R.id.tips)
        btnSave = findViewById(R.id.btnSave)
        btnSaveSchedule = findViewById(R.id.saveScheduleButton)
        btnFinish = findViewById(R.id.btnFinish)
        Log.d("AddWorkActivity", "Views initialized")

       updateUIState()

        val selectedDay = intent.getIntExtra("selectedDay", -1)
        val selectedMonth = intent.getIntExtra("selectedMonth", -1)
        val selectedYear = intent.getIntExtra("selectedYear", -1)

        if (selectedDay != -1 && selectedMonth != -1 && selectedYear != -1) {
            val formattedDate = String.format("%d/%02d/%02d", selectedYear, selectedMonth, selectedDay)
            workDate.setText(formattedDate)

            Log.d("MainActivity", "Pre-filled date: $formattedDate")
        }


        //Load saved schedules into the spinner
        loadSavedSchedulesIntoSpinner()

        payTypeSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedPayType = parent.getItemAtPosition(position) as String
                updatePayTypeView(selectedPayType)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //do nothing
            }
        }

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

            if (selectedDay != -1 && selectedMonth != -1 && selectedYear != -1) {
                calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
                calendar.set(Calendar.MONTH, selectedMonth - 1)
                calendar.set(Calendar.YEAR, selectedYear)
            }

            // Extract the year, month, and day for the DatePickerDialog
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

        commissionDetailIcon.setOnClickListener {
            val commissionDetailIconText = commissionDetailEditText.text.toString()

            if (commissionDetailIconText.isNotEmpty()) {
                val commissionDetail = commissionDetailIconText.toDoubleOrNull()
                if (commissionDetail != null) {
                    commissionDetailsList.add(commissionDetail)
                    adapter.notifyDataSetChanged()
                    commissionDetailEditText.text.clear()
                } else {
                    Toast.makeText(this, "Invalid commission value", Toast.LENGTH_SHORT).show()
                }
            }
        }

        //Handle adding work schedule
        btnSave.setOnClickListener {
            val workDate = workDate.text.toString()
            val startTime = startTime.text.toString()
            val endTime = endTime.text.toString()
            val breakTime = breakTime.text.toString().toIntOrNull() ?: 0
            val payType = payType.selectedItem.toString()
            val payRate = payRate.text.toString().toDoubleOrNull() ?: 0.0
            val overtimePay = overtimePay.text.toString().toDoubleOrNull() ?: 0.0
            val commissionRate = commissionRate.text.toString().toIntOrNull() ?: 0
            val adapter = commissionDetailsRecyclerView.adapter as CommissionDetailsAdapter
            val commissionDetails: List<Double> = adapter.getCommissionList()
            val salaryAmount = salaryAmount.text.toString().toDoubleOrNull() ?: 0.0
            val tips = tips.text.toString().toDoubleOrNull() ?: 0.0



            Log.d("AddWorkActivity", "Saving Work Schedule: Date: $workDate, Start time: $startTime, End time: $endTime, Break Time: $breakTime, Pay Type: $payType, Hourly Rate: $payRate, Overtime Pay: $overtimePay, Commission Rate: $commissionRate, Commission Details: $commissionDetails, Salary Amount: $salaryAmount, Tips: $tips")
            //Insert into database
            val dbHelper = WorkScheduleDatabaseHelper(this)
            dbHelper.insertWorkSchedule(null, workDate, startTime, endTime, breakTime, payType, payRate, overtimePay, commissionRate, commissionDetails, salaryAmount, tips)

            Toast.makeText(this, "Work schedule added successfully!", Toast.LENGTH_SHORT).show()
            val resultIntent = Intent()
            resultIntent.putExtra("entryAddedOrUpdated",true)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        //Save Schedule into database for later use
        btnSaveSchedule.setOnClickListener {
            val startTime = startTime.text.toString()
            val endTime = endTime.text.toString()
            val breakTime = breakTime.text.toString().toIntOrNull() ?: 0
            val payType = payTypeSpinner.selectedItem.toString()
            val hourlyRate = payRate.text.toString().toDoubleOrNull() ?: 0.0
            val overtimePay = overtimePay.text.toString().toDoubleOrNull() ?: 0.0
            val commissionRate = commissionRate.text.toString().toIntOrNull() ?: 0
            val salaryAmount = salaryAmount.text.toString().toDoubleOrNull() ?: 0.0

            //Use StartTime - EndTime as schedule name
            val scheduleName = "$startTime - $endTime"

            //Insert into saved schedules database
            dbHelper.insertSavedSchedule(scheduleName, startTime, endTime, breakTime, payType, hourlyRate, overtimePay, commissionRate, salaryAmount)
            updateUIState()
            Toast.makeText(this, "Schedule saved as $scheduleName", Toast.LENGTH_SHORT).show()
        }

        btnFinish.setOnClickListener {
            finish()
        }
    }





    private fun generateCommissionTotal(commissionRate: Int, commissionDetailsCSV: String): Float {
        //Split CSV
        val salesList = commissionDetailsCSV.split(",")
        Log.d("AddWorkActivity", "Sales list: $salesList")
        val totalSales = salesList.map { it.toFloatOrNull() ?: 0f }.sum()
        Log.d("AddWorkActivity", "Total sales: $totalSales")
        val commissionPercentage = (commissionRate / 100.0f)

        val totalCommission = totalSales * commissionPercentage
        Log.d("AddWorkActivity", "Generate commission total: $totalCommission")
        return totalCommission
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
            val commissionRate = cursor.getInt(cursor.getColumnIndexOrThrow("commission_rate"))
            val salaryAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("salary_amount"))


            //Populate the fields with the loaded data
            startTimeEditText.setText(startTime)
            endTimeEditText.setText(endTime)
            breakTimeEditText.setText(breakTime.toString())
            payRateEditText.setText(payRate.toString())
            overtimePayEditText.setText(overtimeRate.toString())
            commissionRateEditText.setText(commissionRate.toString())
            salaryAmountEditText.setText(salaryAmount.toString())


            //Set the selected pay type in the spinner
            val payTypes = arrayOf("Hourly", "Salary", "Commission")
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

    private fun updatePayTypeView(payType: String) {
        when (payType) {
            "Hourly" -> {
                salaryAmountEditText.visibility = View.GONE
                salaryAmount.visibility = View.GONE
                salaryAmountText.visibility = View.GONE

                commissionRateEditText.visibility = View.GONE
                commissionRate.visibility = View.GONE
                commissionRateText.visibility = View.GONE
                commissionDetail.visibility = View.GONE
                commissionDetailEditText.visibility = View.GONE
                commissionDetailIcon.visibility = View.GONE
                commissionDetailsRecyclerView.visibility = View.GONE

                payRateEditText.visibility = View.VISIBLE
                payRate.visibility = View.VISIBLE
                payRateText.visibility = View.VISIBLE
                overtimePayEditText.visibility = View.VISIBLE
                overtimePay.visibility = View.VISIBLE
                overtimePayText.visibility = View.VISIBLE
            }
            "Salary" -> {
                salaryAmountEditText.visibility = View.VISIBLE
                salaryAmount.visibility = View.VISIBLE
                salaryAmountText.visibility = View.VISIBLE

                commissionRateEditText.visibility = View.GONE
                commissionRate.visibility = View.GONE
                commissionDetail.visibility = View.GONE
                commissionDetailEditText.visibility = View.GONE
                commissionDetailIcon.visibility = View.GONE
                commissionDetailsRecyclerView.visibility = View.GONE

                payRateEditText.visibility = View.GONE
                payRate.visibility = View.GONE
                payRateText.visibility = View.GONE

                overtimePayEditText.visibility = View.GONE
                overtimePay.visibility = View.GONE
                overtimePayText.visibility = View.GONE
            }
            "Commission" -> {
                salaryAmount.visibility = View.GONE
                salaryAmountEditText.visibility = View.GONE
                salaryAmountText.visibility = View.GONE

                commissionRate.visibility = View.VISIBLE
                commissionRateEditText.visibility = View.VISIBLE
                commissionRateText.visibility = View.VISIBLE
                commissionDetail.visibility = View.VISIBLE
                commissionDetailEditText.visibility = View.VISIBLE
                commissionDetailIcon.visibility = View.VISIBLE
                commissionDetailsRecyclerView.visibility = View.VISIBLE

                payRate.visibility = View.GONE
                payRateEditText.visibility = View.GONE
                payRateText.visibility = View.GONE
                overtimePay.visibility = View.GONE
                overtimePayEditText.visibility = View.GONE
                overtimePayText.visibility = View.GONE
            }
        }
    }

    fun updateUIState() {
        val sharedPreferences = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        val baseButtonColor = sharedPreferences.getInt("baseButtonColor", Color(204, 153, 255).toArgb())
        val baseTextColor = sharedPreferences.getInt("baseTextColor", Color.Black.toArgb())
        val detailsTextColor = sharedPreferences.getInt("detailsTextColor", Color.Black.toArgb())
        val backgroundColor1 = sharedPreferences.getInt("backgroundColor1", Color(143, 216, 230).toArgb())
        val backgroundColor2 = sharedPreferences.getInt("backgroundColor2", Color.White.toArgb())


        //Set Button Colors
        btnSave.setBackgroundColor(baseButtonColor)
        btnSaveSchedule.setBackgroundColor(baseButtonColor)
        btnFinish.setBackgroundColor(baseButtonColor)

        //Set background colors in Gradient
        val gradientDrawable = GradientDrawable(GradientDrawable.Orientation.TR_BL, intArrayOf(backgroundColor1, backgroundColor2))
        findViewById<View>(R.id.rootLayout).background = gradientDrawable

        //Set text colors
        btnSave.setTextColor(baseTextColor)
        workDate.setTextColor(detailsTextColor)
        startTime.setTextColor(baseTextColor)
        startTimeEditText.setTextColor(detailsTextColor)
        endTime.setTextColor(baseTextColor)
        endTimeEditText.setTextColor(detailsTextColor)
        breakTime.setTextColor(baseTextColor)
        breakTimeEditText.setTextColor(detailsTextColor)
        payRate.setTextColor(baseTextColor)
        payRateEditText.setTextColor(detailsTextColor)
        overtimePay.setTextColor(baseTextColor)
        overtimePayEditText.setTextColor(baseTextColor)
        btnSave.setTextColor(baseTextColor)
        btnSaveSchedule.setTextColor(baseTextColor)
        btnFinish.setTextColor(baseTextColor)
    }
}