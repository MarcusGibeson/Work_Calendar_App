package com.example.work_calendar_app.data.repositories

import android.util.Log
import com.example.work_calendar_app.data.models.WorkEntry
import com.example.work_calendar_app.data.database.WorkScheduleDatabaseHelper
import com.example.work_calendar_app.data.models.Job
import com.example.work_calendar_app.data.models.SavedSchedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class WorkRepository (private val dbHelper: WorkScheduleDatabaseHelper) {
    //Fetch all work entries from the database
    suspend fun getAllWorkEntries(): Map<Long, WorkEntry> = withContext(Dispatchers.IO) {
        val workEntries = mutableMapOf<Long, WorkEntry>()
        val cursor = dbHelper.getAllWorkSchedule()

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val workEntry = cursorToWorkEntry(cursor)
                if (workEntry != null) {
                    workEntries[workEntry.id] = workEntry
                }
            } while (cursor.moveToNext())
        }
        return@withContext workEntries
    }

    //Fetch work entry for specific date
    suspend fun getWorkEntryForDate(date: String): WorkEntry = withContext(Dispatchers.IO) {
        val cursor = dbHelper.getWorkScheduleByDate(date)
        if (cursor == null) {
            Log.e("WorkRepository", "Cursor is null. Database query failed for date: $date")
        }

        var workEntry = WorkEntry(0, 0,"","","",0,"",0.0,0.0,0,listOf(0.0),0.0,0.0,0.0,0.0,0.0)

        if (cursor != null && cursor.moveToFirst()) {
            try {
                workEntry = cursorToWorkEntry(cursor) ?: workEntry
            } catch (e: Exception) {
                Log.e("WorkRepository", "Error extracting work entry from cursor: ${e.message}")
            }
        }
        cursor.close()
        return@withContext workEntry
    }

    //Insert or update a work entry in the database
    suspend fun addOrUpdateWorkEntry(workEntry: WorkEntry) = withContext(Dispatchers.IO) {
        dbHelper.insertWorkSchedule(
            id = workEntry.id,
            jobId = workEntry.jobId,
            date = workEntry.workDate,
            startTime = workEntry.startTime,
            endTime = workEntry.endTime,
            breakMinutes = workEntry.breakTime,
            payType = workEntry.payType,
            payRate = workEntry.payRate,
            overtimeRate = workEntry.overtimeRate,
            commissionRate = workEntry.commissionRate,
            commissionDetails = workEntry.commissionDetails,
            salaryAmount = workEntry.salaryAmount,
            tips = workEntry.tips
        )
    }

    //Delete a work entry by ID
    suspend fun deleteWorkEntry(id: Long) = withContext(Dispatchers.IO) {
        dbHelper.deleteWorkEntry(id)
    }

    //Fetch work entries for a specific month and year
    suspend fun getWorkEntriesBetweenDates(startDate: String, endDate: String): Map<Long, WorkEntry> = withContext(Dispatchers.IO) {
        val workEntries = mutableMapOf<Long, WorkEntry>()
        val formattedStartDate = formatDateForQuery(startDate)
        val formattedEndDate = formatDateForQuery(endDate)
        Log.d("WorkRepository-getWorkEntriesBetweenDates", "Formatted date range for query: $formattedStartDate to $formattedEndDate")


        //Query the database to fetch work times for each day between start and end
        val cursor = dbHelper.getWorkScheduleBetweenDates(formattedStartDate ,formattedEndDate)

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val workEntry = cursorToWorkEntry(cursor)
                if (workEntry != null) {
                    workEntries[workEntry.id] = workEntry
                }
            } while (cursor.moveToNext())
        }
        return@withContext workEntries
    }

    //Insert saved schedule into database
    suspend fun insertSavedSchedule(jobId: Long, scheduleName: String, startTime: String, endTime: String, breakTime: Int, payType: String, hourlyRate: Double, overtimeRate: Double, commissionRate: Int, salaryAmount: Double) {
        withContext(Dispatchers.IO) {
            dbHelper.insertSavedSchedule(jobId, scheduleName, startTime, endTime, breakTime, payType, hourlyRate, overtimeRate, commissionRate, salaryAmount)
        }
    }

    //Fetch all saved schedules
    suspend fun getAllSavedSchedules(): List<SavedSchedule> {
        return withContext(Dispatchers.IO) {
            val cursor = dbHelper.getAllSavedSchedules()

            val savedSchedules = mutableListOf<SavedSchedule>()
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                    val jobId = cursor.getLong(cursor.getColumnIndexOrThrow("job_id"))
                    val scheduleName = cursor.getString(cursor.getColumnIndexOrThrow("schedule_name"))
                    val startTime = cursor.getString(cursor.getColumnIndexOrThrow("start_time"))
                    val endTime = cursor.getString(cursor.getColumnIndexOrThrow("end_time"))
                    val breakTime = cursor.getInt(cursor.getColumnIndexOrThrow("break_time_minutes"))
                    val payType = cursor.getString(cursor.getColumnIndexOrThrow("pay_type"))
                    val hourlyRate = cursor.getDouble(cursor.getColumnIndexOrThrow("pay_rate"))
                    val overtimeRate = cursor.getDouble(cursor.getColumnIndexOrThrow("overtime_rate"))
                    val commissionRate = cursor.getInt(cursor.getColumnIndexOrThrow("commission_rate"))
                    val salaryAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("salary_amount"))

                    savedSchedules.add(
                        SavedSchedule(
                            id = id,
                            jobId = jobId,
                            scheduleName = scheduleName,
                            startTime = startTime,
                            endTime = endTime,
                            breakTime = breakTime,
                            payType = payType,
                            hourlyRate = hourlyRate,
                            overtimeRate = overtimeRate,
                            commissionRate = commissionRate,
                            salaryAmount = salaryAmount
                        )
                    )
                } while (cursor.moveToNext())
            }
            cursor.close()
            savedSchedules
        }
    }

    //retrieve the saved schedule by name
    suspend fun getSavedScheduleByName(scheduleName: String): SavedSchedule? {
        return dbHelper.getSavedScheduleByName(scheduleName)
    }


    //Fetch all jobs from the database
    suspend fun getAllJobs(): List<Job> = withContext(Dispatchers.IO) {
        val jobs = mutableListOf<Job>()
        val cursor = dbHelper.getAllJobs()

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val job = cursorToJob(cursor)
                if (job != null) {
                    jobs.add(job)
                }
            } while (cursor.moveToNext())
        }
        return@withContext jobs
    }

    //Insert a new job in the database
    suspend fun insertJob(jobName: String) = withContext(Dispatchers.IO) {
        dbHelper.insertJob(
            name = jobName
        )
    }

    //Delete job by id
    suspend fun deleteJob(jobId: Long) = withContext(Dispatchers.IO) {
        dbHelper.deleteJob(jobId)
    }

    //Helper function to convert a cursor into Job object
    private fun cursorToJob(cursor: android.database.Cursor): Job? {
        return try {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow("job_id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("job_name"))

            Job(
                id = id,
                name = name
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    //Helper function to convert a cursor into a WorkEntry object
    private fun cursorToWorkEntry(cursor: android.database.Cursor): WorkEntry? {
        return try {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
            val jobId = cursor.getLong(cursor.getColumnIndexOrThrow("job_id"))
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

            val commissionSalesList = commissionDetails.split(",").mapNotNull { it.trim().toDoubleOrNull() }

            WorkEntry(
                id = id,
                jobId = jobId,
                workDate = workDate,
                startTime = startTime,
                endTime = endTime,
                breakTime = breakTime,
                payType = payType,
                payRate = payRate,
                overtimeRate = overtimeRate,
                commissionRate = commissionRate,
                commissionDetails = commissionSalesList,
                totalCommissionAmount = totalCommissionSales,
                salaryAmount = salaryAmount,
                dailySalary = dailySalary,
                tips = tips,
                netEarnings = netEarnings
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun formatDateForQuery(date: String): String {
        if (!isValidDateFormat(date)) {
            val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(sdf.parse(date))
        } else {
            return date
        }
    }

    fun isValidDateFormat(date: String): Boolean {
        val regex = Regex("^\\d{4}-\\d{2}-\\d{2}$")
        return regex.matches(date)
    }


}