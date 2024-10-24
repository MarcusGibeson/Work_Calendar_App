package com.example.work_calendar_app.data.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class WorkScheduleDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "work_schedule.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "work_schedule"
        private const val SECOND_TABLE_NAME = "saved_schedules"
        private const val COLUMN_ID = "id"
        private const val COLUMN_DATE = "work_date"
        private const val COLUMN_SCHEDULE_NAME = "schedule_name"
        private const val COLUMN_START_TIME = "start_time"
        private const val COLUMN_END_TIME = "end_time"
        private const val COLUMN_BREAK_TIME_MINUTES = "break_time_minutes"
        private const val COLUMN_PAY_TYPE = "pay_type"
        private const val COLUMN_PAY_RATE = "pay_rate"
        private const val COLUMN_OVERTIME_RATE = "overtime_rate"
        private const val COLUMN_TIPS = "tips"
        private const val COLUMN_COMMISSION_RATE = "commission_rate"
        private const val COLUMN_COMMISSION_DETAILS = "commission_details"
        private const val COLUMN_TOTAL_COMMISSION_SALES = "total_commission_sales"
        private const val COLUMN_SALARY_AMOUNT = "salary_amount"
        private const val COLUMN_DAILY_SALARY = "daily_salary"
        private const val COLUMN_TOTAL_EARNINGS = "total_earnings"
        private const val JOBS_TABLE_NAME = "jobs"
        private const val COLUMN_JOB_ID = "job_id"
        private const val COLUMN_JOB_NAME = "job_name"
    }



    override fun onCreate(db: SQLiteDatabase?) {
        val createJobsTableQuery = ("CREATE TABLE $JOBS_TABLE_NAME (" +
                "$COLUMN_JOB_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_JOB_NAME TEXT )")
        db?.execSQL(createJobsTableQuery)

        val createTableQuery = ("CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_DATE TEXT, " +
                "$COLUMN_START_TIME TEXT, " +
                "$COLUMN_END_TIME TEXT, " +
                "$COLUMN_BREAK_TIME_MINUTES INTEGER, " +
                "$COLUMN_PAY_TYPE TEXT, " +
                "$COLUMN_PAY_RATE REAL, " +
                "$COLUMN_OVERTIME_RATE REAL, " +
                "$COLUMN_COMMISSION_RATE REAL, " +
                "$COLUMN_COMMISSION_DETAILS TEXT, " +
                "$COLUMN_TOTAL_COMMISSION_SALES REAL, " +
                "$COLUMN_SALARY_AMOUNT REAL, " +
                "$COLUMN_DAILY_SALARY REAL, " +
                "$COLUMN_TIPS REAL, " +
                "$COLUMN_TOTAL_EARNINGS REAL, " +
                "FOREIGN KEY($COLUMN_JOB_ID) REFERENCES $JOBS_TABLE_NAME($COLUMN_JOB_ID) ON DELETE CASCADE )")
        db?.execSQL(createTableQuery)

        val createSecondTableQuery = ("CREATE TABLE $SECOND_TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_SCHEDULE_NAME TEXT NOT NULL, " +
                "$COLUMN_START_TIME TEXT, " +
                "$COLUMN_END_TIME TEXT, " +
                "$COLUMN_BREAK_TIME_MINUTES INTEGER, "+
                "$COLUMN_PAY_TYPE TEXT, " +
                "$COLUMN_PAY_RATE REAL, " +
                "$COLUMN_OVERTIME_RATE REAL, " +
                "$COLUMN_COMMISSION_RATE REAL, " +
                "$COLUMN_COMMISSION_DETAILS TEXT, " +
                "$COLUMN_TOTAL_COMMISSION_SALES INT, " +
                "$COLUMN_SALARY_AMOUNT REAL, " +
                "$COLUMN_TOTAL_EARNINGS REAL, " +
                "FOREIGN KEY($COLUMN_JOB_ID) REFERENCES $JOBS_TABLE_NAME($COLUMN_JOB_ID) ON DELETE CASCADE )")
        db?.execSQL(createSecondTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun isValidDateFormat(date: String): Boolean {
        val regex = Regex("^\\d{4}-\\d{2}-\\d{2}$")
        return regex.matches(date)
    }

    fun insertWorkSchedule(id: Long?, jobId: Long, date: String, startTime: String, endTime: String, breakMinutes: Int, payType: String, payRate: Double, overtimeRate: Double, commissionRate: Int, commissionDetails: List<Double>, salaryAmount: Double, tips: Double): Boolean {
        val db = this.writableDatabase
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        var formattedDate: String? = null
        if (!isValidDateFormat(date)) {
            formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(sdf.parse(date))
        }
        val commissionDetailsCSV = commissionDetails.joinToString(separator = ", ")

        val totalCommissionSales = generateCommissionTotal(commissionRate, commissionDetailsCSV)

        val dailySalary = String.format("%.2f", salaryAmount / 365).toDouble()//rough daily estimate

        val totalEarnings = calculateTotalEarnings(payRate, startTime, endTime, breakMinutes, overtimeRate, totalCommissionSales, salaryAmount, tips)


        // Log all the information being submitted
        Log.d("Database-insertWorkSchedule", "Attempting to insert work schedule with the following details:")
        if (!isValidDateFormat(date)){
            Log.d("Database-insertWorkSchedule", "Date (Formatted): $formattedDate")
        }else {
            Log.d("Database-insertWorkSchedule", "Date (Original): $date")
        }
        Log.d("Databse-insertWorkSchedule", "Job ID: $jobId")
        Log.d("Database-insertWorkSchedule", "Start Time: $startTime")
        Log.d("Database-insertWorkSchedule", "End Time: $endTime")
        Log.d("Database-insertWorkSchedule", "Break Minutes: $breakMinutes")
        Log.d("Database-insertWorkSchedule", "Pay Type: $payType")
        Log.d("Database-insertWorkSchedule", "Pay Rate: $payRate")
        Log.d("Database-insertWorkSchedule", "Overtime Rate: $overtimeRate")
        Log.d("Database-insertWorkSchedule", "Commission Rate: $commissionRate")
        Log.d("Database-insertWorkSchedule", "Commission Details: $commissionDetailsCSV")
        Log.d("Database-insertWorkSchedule", "Total Commission Sales: $totalCommissionSales")
        Log.d("Database-insertWorkSchedule", "Total Salary Amount: $salaryAmount")
        Log.d("Database-insertWorkSchedule", "Daily Salary Amount: $dailySalary")
        Log.d("Database-insertWorkSchedule", "Tips: $tips")
        Log.d("Database-insertWorkSchedule", "Total Earnings: $totalEarnings")

        val values = ContentValues().apply {
            put(COLUMN_DATE, formattedDate ?: date)
            put(COLUMN_START_TIME, startTime)
            put(COLUMN_END_TIME, endTime)
            put(COLUMN_BREAK_TIME_MINUTES, breakMinutes)
            put(COLUMN_PAY_TYPE, payType)
            when (payType) {
                "Hourly" -> {
                    put(COLUMN_PAY_RATE, payRate)
                    put(COLUMN_OVERTIME_RATE, overtimeRate)
                    put(COLUMN_COMMISSION_RATE, 0)
                    put(COLUMN_TOTAL_COMMISSION_SALES, 0)
                    put(COLUMN_SALARY_AMOUNT, 0)
                    put(COLUMN_DAILY_SALARY, 0)
                }
                "Salary" -> {
                    put(COLUMN_PAY_RATE, 0)
                    put(COLUMN_OVERTIME_RATE, 0)
                    put(COLUMN_COMMISSION_RATE, 0)
                    put(COLUMN_TOTAL_COMMISSION_SALES, 0)
                    put(COLUMN_SALARY_AMOUNT, salaryAmount)
                    put(COLUMN_DAILY_SALARY, dailySalary)
                }
                "Commission" -> {
                    put(COLUMN_PAY_RATE, 0)
                    put(COLUMN_OVERTIME_RATE, 0)
                    put(COLUMN_COMMISSION_RATE, commissionRate)
                    put(COLUMN_TOTAL_COMMISSION_SALES, totalCommissionSales)
                    put(COLUMN_SALARY_AMOUNT, 0)
                    put(COLUMN_DAILY_SALARY, 0)
                }
            }
            put(COLUMN_COMMISSION_DETAILS, commissionDetailsCSV)
            put(COLUMN_TIPS, tips)
            put(COLUMN_TOTAL_EARNINGS, totalEarnings)
            put(COLUMN_JOB_ID, jobId)
        }
        return try {
            if (id != null) {
                //Update existing entry by ID
                val rowsUpdated =
                    db.update("work_schedule", values, "$COLUMN_ID = ?", arrayOf(id.toString()))
                if (rowsUpdated > 0) {
                    Log.d("Database", "Work Schedule inserted/updated successfully")
                    true
                } else {
                    Log.e("Database", "Failed to insert/update work schedule")
                    false
                }
            } else {
                //Insert new entry since no ID was provided
                val newRowId = db.insert("work_schedule", null, values)

                if (newRowId != -1L) {
                    Log.d("Database", "Work schedule inserted successfully with ID: $newRowId")
                    true
                } else {
                    Log.e("Database", "Failed to insert work schedule.")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("Database", "Error while inserting/updating work schedule: ${e.message}")
            false
        } finally {
            db.close()
        }
    }

    fun insertSavedSchedule(jobId: Long, name: String, startTime: String, endTime: String, breakTime: Int, payType: String, hourlyRate: Double, overtimeRate: Double, commissionRate: Int, salaryAmount: Double) {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_SCHEDULE_NAME, name)
            put(COLUMN_START_TIME, startTime)
            put(COLUMN_END_TIME, endTime)
            put(COLUMN_BREAK_TIME_MINUTES, breakTime)
            put(COLUMN_PAY_TYPE, payType)
            put(COLUMN_PAY_RATE, hourlyRate)
            put(COLUMN_OVERTIME_RATE, overtimeRate)
            put(COLUMN_COMMISSION_RATE, commissionRate)
            put(COLUMN_SALARY_AMOUNT, salaryAmount)
            put(COLUMN_JOB_ID, jobId)
        }
        db.insert("saved_schedules", null, values)
        Log.d("Database-insertSavedSchedule", "Schedule save: $name")
    }

    fun fetchJobsFromDatabase(): List<Job> {
        val db = this.readableDatabase
        val cursor = db.query(
            JOBS_TABLE_NAME,
            arrayOf(COLUMN_JOB_ID, COLUMN_JOB_NAME),
            null, null, null, null, null
        )

        val jobs = mutableListOf<Job>()
        if(cursor.moveToFirst()) {
            do {
                val jobId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_JOB_ID))
                val jobName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_JOB_NAME))
                jobs.add(Job(jobId, jobName))
                Log.d("Database-fetchJobs", "Job fetched: $jobName : $jobId")
            } while (cursor.moveToNext())
        }
        cursor.close()
        return jobs
    }


    fun deleteWorkEntry(id: Long): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_NAME, "$COLUMN_ID= ?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }

    fun getAllWorkSchedule(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM work_schedule", null)
    }

    fun getWorkScheduleByDate(date: String): Cursor {
        val db = this.readableDatabase

        //Define a query to fetch work schedule for a specific date
        val query = "SELECT * FROM work_schedule WHERE work_date = ?"

        //Execute the query and return the result
        return db.rawQuery(query, arrayOf(date))
    }

    fun getWorkScheduleBetweenDates(startDate: String, endDate: String): Cursor {
        val db = this.readableDatabase

        val query = "SELECT * FROM work_schedule WHERE work_date BETWEEN ? AND ?"
        val cursor = db.rawQuery(query, arrayOf(startDate, endDate))

        // Log the result: Check if the cursor has any results and log how many rows were returned
        if (cursor != null && cursor.moveToFirst()) {
            Log.d("DatabaseHelper-getWorkScheduleBetweenDates", "Query successful. Number of results: ${cursor.count}")

            // Iterate over the cursor and log each row
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
                val tips = cursor.getDouble(cursor.getColumnIndexOrThrow("tips"))
                val netEarnings = cursor.getDouble(cursor.getColumnIndexOrThrow("total_earnings"))

                // Log each column in the current row
                Log.d("DatabaseHelper-getWorkScheduleBetweenDates", "Row: ID: $id, WorkDate: $workDate, StartTime: $startTime, EndTime: $endTime, BreakTime: $breakTime, PayType: $payType, PayRate: $payRate, OvertimeRate: $overtimeRate, CommissionRate: $commissionRate, CommissionDetails: $commissionDetails, TotalCommissionSales: $totalCommissionSales, SalaryAmount: $salaryAmount, Tips: $tips, NetEarnings: $netEarnings")

            } while (cursor.moveToNext())
        } else {
            Log.d("DatabaseHelper-getWorkScheduleBetweenDates", "Query returned no results.")
        }


        return cursor
    }

    fun getAllSavedSchedules(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM saved_schedules", null)
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
        Log.d("Database-CalculateHoursWorked", "Calculated hours worked: $hoursWorked (Start: $startTime, End: $endTime)")
        return hoursWorked
    }

    private fun getOvertimeHours(workHours: Double): Double {
        val standardHours = 8.0
        return if (workHours > standardHours) workHours - standardHours else 0.0
    }

    private fun calculateTotalEarnings(hourlyRate: Double, startTime: String, endTime: String, breakTime: Int, overtimePay: Double, totalCommissionSales: Double, salaryAmount: Double, tips: Double): Double {
        //Logic to calculate total earnings, including break time and overtime
        val workHours = calculateHoursWorked(startTime, endTime) - (breakTime / 60)
        val overtimeHours = getOvertimeHours(workHours)
        val totalHourlyEarnings = (workHours * hourlyRate) + (overtimeHours * overtimePay)
        val formattedTotalHourlyEarnings = String.format("%.2f", totalHourlyEarnings).toDouble()

        val formattedCommissionTotal = String.format("%.2f", totalCommissionSales).toDouble()

        val formattedSalaryAmount = String.format("%.2f", salaryAmount / 365).toDouble()

        val totalEarnings = (formattedTotalHourlyEarnings + formattedCommissionTotal) + formattedSalaryAmount + tips



        Log.d("Database-CalculateTotalEarnings", "Calculated Total Earnings: $totalEarnings (Work Hours: $workHours, Overtime hours: $overtimeHours)")
        return totalEarnings
    }

    fun generateCommissionTotal(commissionRate: Int, commissionDetailsCSV: String): Double {
        //Split CSV
        val salesList = commissionDetailsCSV.split(",")
        val totalSales = salesList.map { it.toFloatOrNull() ?: 0f }.sum()
        val formattedSales = String.format("%.2f", totalSales).toDouble()
        Log.d("Database- generateCommissionTotal", "Sales list: $salesList, Total sales: $totalSales, Commission Rate: $commissionRate")
        val totalCommission = formattedSales * (commissionRate.toDouble() / 100)
        Log.d("Database- generateCommissionTotal", "Total Commission earned: $totalCommission")

        return totalCommission
    }
}
