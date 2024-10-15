package com.example.work_calendar_app


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.compose.material3.Button
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.work_calendar_app.ui.composables.calendar.DayDetailsPopup
import com.example.work_calendar_app.ui.composables.calendar.WorkCalendar
import com.example.work_calendar_app.data.models.WorkDetails
import com.example.work_calendar_app.data.models.WorkEntry
import com.example.work_calendar_app.data.database.WorkScheduleDatabaseHelper
import com.example.work_calendar_app.data.repositories.WorkRepository
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.work_calendar_app.ui.composables.calendar.CalendarContent
import com.example.work_calendar_app.ui.composables.workdetails.*
import com.example.work_calendar_app.ui.composables.screens.*
import com.example.work_calendar_app.viewmodels.WorkViewModel
import com.example.work_calendar_app.viewmodels.WorkViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: WorkScheduleDatabaseHelper
    private val workRepository = WorkRepository(dbHelper)

    private val workViewModel: WorkViewModel by viewModels {
        WorkViewModelFactory(workRepository)
    }


    public lateinit var addWorkActivityLauncher: ActivityResultLauncher<Intent>
    var entryEdited by mutableStateOf(false)
    private var workEntriesChanged by mutableStateOf(0)

    private var isSelectingRange by mutableStateOf(false)
    private var firstSelectedDate: String? = null
    private var secondSelectedDate: String? = null
    private var selectedDay by mutableStateOf(-1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addWorkActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val entryAddedOrUpdated = data?.getBooleanExtra("entryAddedOrUpdated", false)
                if (entryAddedOrUpdated == true){
                    onAddOrUpdateOrDeleteEntry()
                }
            }
        }
        setContent {
            MaterialTheme {
                CalendarScreen(workViewModel)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                //Start the UserSettingsActivity when the settings icon is clicked
                val intent = Intent(this, UserSettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun refreshWorkEntries() {
        workEntriesChanged++
    }

    fun onAddOrUpdateOrDeleteEntry() {
        refreshWorkEntries()
        entryEdited = true
    }

    private fun onSettingsClicked() {
        val intent = Intent(this, UserSettingsActivity::class.java)
        startActivity(intent)
    }

    private fun loadAllWorkSchedules(workDays: MutableList<Int>, workEntries: MutableMap<Long, WorkEntry>, currentMonth: Int, currentYear: Int) {
        dbHelper = WorkScheduleDatabaseHelper(this)
        val cursor = dbHelper.getAllWorkSchedule()


    }


    private suspend fun getWorkDetailsForDate(context: Context, date: String): WorkDetails {
        //Log when function is called
        Log.d("MainActivity-getWorkDetailsForDate", "getWorkDetailsForDate called for date: $date")

        val dbHelper = WorkScheduleDatabaseHelper(context)
        val cursor = dbHelper.getWorkScheduleByDate(date)

        //Log whether the cursor is null or not
        if (cursor == null) {
            Log.e("MainActivity-getWorkDetailsForDate", "Cursor is null. Database query failed for date: $date")
            return WorkDetails(0,"", "", "", "", "",0.0,0.0, 0.0, 0.0, 0.0)
        }

        //Check if the cursor has data
        if (cursor.moveToFirst()) {
            try {
                //Extract the data from the cursor
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val workDate = cursor.getString(cursor.getColumnIndexOrThrow("work_date"))
                val startTime = cursor.getString(cursor.getColumnIndexOrThrow("start_time"))
                val endTime = cursor.getString(cursor.getColumnIndexOrThrow("end_time"))
                val breakTime = cursor.getInt(cursor.getColumnIndexOrThrow("break_time_minutes")).toString()
                val payType = cursor.getString(cursor.getColumnIndexOrThrow("pay_type"))
                val payRate = cursor.getDouble(cursor.getColumnIndexOrThrow("pay_rate"))
                val commissionSales = cursor.getDouble(cursor.getColumnIndexOrThrow("total_commission_sales"))
                val dailySalary = cursor.getDouble(cursor.getColumnIndexOrThrow("daily_salary"))
                val tips = cursor.getDouble(cursor.getColumnIndexOrThrow("tips"))
                val netEarnings = cursor.getDouble(cursor.getColumnIndexOrThrow("total_earnings"))


                //Log the extracted data
                Log.d(
                    "MainActivity-getWorkDetailsForDate",
                    "Work Date: $workDate, Start time: $startTime, End Time: $endTime, Break Time: $breakTime, Pay Type: $payType, Pay Rate: $payRate, Commission Sales; $commissionSales, Daily Salary: $dailySalary, Tips: $tips, Net Earnings: $netEarnings"
                )

                //Create a WorkDetails object and return it
                return WorkDetails(id, workDate, startTime, endTime, breakTime, payType, payRate, commissionSales, dailySalary, tips, netEarnings)
            } catch (e: Exception) {
                //Log any exceptions encountered while reading the cursor data
                Log.e("MainActivity-getWorkDetailsForDate", "Error extracting work detail from cursor: ${e.message}")
            }
        } else {
            // If no data is found for the date
            Log.d("MainActivity-getWorkDetailsForDate", "No work details found for date: $date")
        }

        //Close cursor to avoid memory leaks
        cursor.close()
        return WorkDetails(0, "", "", "","", "",0.0, 0.0,0.0, 0.0, 0.0)
    }

    private fun fetchWorkEntriesForMonth(month: Int, year: Int): MutableMap<Long, WorkEntry> {
        //Create the start and end dates for the month
        val startOfMonth = LocalDate.of(year, month, 1)
        val endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth())

        //Format dates for the query
        val formattedStartDate = formatDateForQuery(startOfMonth.toString())
        val formattedEndDate = formatDateForQuery(endOfMonth.toString())

        Log.d("MainActivity-fetchWorkEntriesForMonth", "Fetching work details for month: $month/$year between $formattedStartDate and $formattedEndDate")

        //Query the database to fetch work times for the entire month
        val cursor = dbHelper.getWorkScheduleBetweenDates(formattedStartDate, formattedEndDate)

        val workEntries = mutableMapOf<Long, WorkEntry>()

        //Iterate through the cursor to get work times
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val workDate = cursor.getString(cursor.getColumnIndexOrThrow("work_date"))
                val startTime = cursor.getString(cursor.getColumnIndexOrThrow("start_time"))
                val endTime = cursor.getString(cursor.getColumnIndexOrThrow("end_time"))
                val breakTime = cursor.getInt(cursor.getColumnIndexOrThrow("break_time_minutes"))
                val payType = cursor.getString(cursor.getColumnIndexOrThrow("pay_type"))
                val payRate = cursor.getDouble(cursor.getColumnIndexOrThrow("pay_rate"))
                val overtimeRate = cursor.getDouble(cursor.getColumnIndexOrThrow("overtime_rate"))
                val commissionRate = cursor.getInt(cursor.getColumnIndexOrThrow("commission_rate"))
                val commissionDetails = cursor.getString(cursor.getColumnIndexOrThrow("commission_details"))
                val totalCommissionSales = cursor.getDouble(cursor.getColumnIndexOrThrow("total_commission_sales"))
                val salaryAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("salary_amount"))
                val dailySalary = cursor.getDouble(cursor.getColumnIndexOrThrow("daily_salary"))
                val tips = cursor.getDouble(cursor.getColumnIndexOrThrow("tips"))
                val netEarnings = cursor.getDouble(cursor.getColumnIndexOrThrow("total_earnings"))

                val commissionSalesList = commissionDetails.split(",")
                    .mapNotNull { it.trim().toDoubleOrNull() }

                //Log or display the work detail
                Log.d("MainActivity-fetchWorkEntriesForMonth", "Loaded WorkDetail for month: $workDate, $startTime - $endTime, Tips: $tips, Net Earnings: $netEarnings")
                val workDetail = WorkEntry(id, workDate, startTime, endTime, breakTime, payType, payRate, overtimeRate, commissionRate, commissionSalesList, totalCommissionSales, salaryAmount, dailySalary, tips, netEarnings)
                workEntries[id] = workDetail
            } while (cursor.moveToNext())
            Log.d("MainActivity-fetchWorkEntriesForMonth", "Work times displayed for month: ${workEntries.size} entries")
            cursor.close()
        } else {
            Log.d("MainActivity-fetchWorkEntriesForMonth", "No work times found for the selected month: $month/$year")
            cursor?.close()
        }
        return workEntries
    }

    private fun displayWorkTimesForSelectedDates(start: String, end: String, workEntries: MutableMap<Long, WorkEntry>) {
        Log.d("MainActivity-displayWorkTimesForSelectedDates", "Fetching work details between $start and $end")

        val formattedStartDate = formatDateForQuery(start)
        val formattedEndDate = formatDateForQuery(end)
        Log.d("MainActivity-displayWorkTimesForSelectedDates", "Formatted date range for query: $formattedStartDate to $formattedEndDate")

        //Query the database to fetch work times for each day between start and end
        val cursor = dbHelper.getWorkScheduleBetweenDates(formattedStartDate ,formattedEndDate)

        //Iterate through the cursor to get work times and display them
        if (cursor != null && cursor.moveToFirst()) {
            val fetchedEntries = mutableStateMapOf<Long, WorkEntry>()
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val workDate = cursor.getString(cursor.getColumnIndexOrThrow("work_date"))
                val startTime = cursor.getString(cursor.getColumnIndexOrThrow("start_time"))
                val endTime = cursor.getString(cursor.getColumnIndexOrThrow("end_time"))
                val breakTime = cursor.getInt(cursor.getColumnIndexOrThrow("break_time_minutes"))
                val payType = cursor.getString(cursor.getColumnIndexOrThrow("pay_type"))
                val payRate = cursor.getDouble(cursor.getColumnIndexOrThrow("pay_rate"))
                val overtimeRate = cursor.getDouble(cursor.getColumnIndexOrThrow("overtime_rate"))
                val commissionRate = cursor.getInt(cursor.getColumnIndexOrThrow("commission_rate"))
                val commissionDetails = cursor.getString(cursor.getColumnIndexOrThrow("commission_details"))
                val totalCommissionSales = cursor.getDouble(cursor.getColumnIndexOrThrow("total_commission_sales"))
                val salaryAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("salary_amount"))
                val dailySalary = cursor.getDouble(cursor.getColumnIndexOrThrow("daily_salary"))
                val tips = cursor.getDouble(cursor.getColumnIndexOrThrow("tips"))
                val netEarnings = cursor.getDouble(cursor.getColumnIndexOrThrow("total_earnings"))

                val commissionSalesList = commissionDetails.split(",")
                    .mapNotNull { it.trim().toDoubleOrNull() }

                //Display or append the work date and times to UI
                Log.d("MainActivity-displayWorkTimesForSelectedDates", "Loaded WorkDetail for range: $workDate, $startTime - $endTime, $breakTime, $payType, $payRate, $overtimeRate, $commissionRate, $commissionSalesList, $totalCommissionSales, $salaryAmount, $dailySalary, Tips: $tips, Net Earnings: $netEarnings")
                val workEntry = WorkEntry(id, workDate, startTime, endTime, breakTime, payType, payRate, overtimeRate, commissionRate, commissionSalesList, totalCommissionSales, salaryAmount, dailySalary, tips, netEarnings)
                fetchedEntries[id] = workEntry
            } while (cursor.moveToNext())
            workEntries.clear()
            workEntries.putAll(fetchedEntries)

            Log.d("MainActivity-displayWorkTimesForSelectedDates", "Work times displayed, count: ${workEntries.size}")
            cursor.close()
        } else {
            Log.d("MainActivity-displayWorkTimesForSelectedDates", "No work times found for the selected date range.")
            workEntries.clear()
        }

    }








}