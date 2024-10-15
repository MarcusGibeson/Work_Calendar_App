package com.example.work_calendar_app.data.repositories

import android.util.Log
import com.example.work_calendar_app.data.models.WorkEntry
import com.example.work_calendar_app.data.database.WorkScheduleDatabaseHelper
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

    //Insert or update a work entry in the database
    suspend fun addOrUpdateWorkEntry(workEntry: WorkEntry) = withContext(Dispatchers.IO) {
        dbHelper.insertWorkSchedule(
            id = workEntry.id,
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

    //Helper function to convert a cursor into a WorkEntry object
    private fun cursorToWorkEntry(cursor: android.database.Cursor): WorkEntry? {
        return try {
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

            val commissionSalesList = commissionDetails.split(",").mapNotNull { it.trim().toDoubleOrNull() }

            WorkEntry(
                id = id,
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