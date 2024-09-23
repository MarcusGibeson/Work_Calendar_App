package com.example.work_calendar_app.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.work_calendar_app.models.WorkSchedule
import java.text.SimpleDateFormat
import java.util.Date
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
        private const val COLUMN_TOTAL_EARNINGS = "total_earnings"
    }



    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = ("CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_DATE TEXT, " +
                "$COLUMN_START_TIME TEXT, " +
                "$COLUMN_END_TIME TEXT, " +
                "$COLUMN_BREAK_TIME_MINUTES INTEGER, " +
                "$COLUMN_PAY_TYPE TEXT, " +
                "$COLUMN_PAY_RATE REAL, " +
                "$COLUMN_OVERTIME_RATE REAL, " +
                "$COLUMN_TOTAL_EARNINGS REAL )")
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
                "$COLUMN_TOTAL_EARNINGS REAL )")
        db?.execSQL(createSecondTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertWorkSchedule(date: String, startTime: String, endTime: String, breakMinutes: Int, payType: String, payRate: Double, overtimeRate: Double, totalEarnings: Double): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_DATE, date)
            put(COLUMN_START_TIME, startTime)
            put(COLUMN_END_TIME, endTime)
            put(COLUMN_BREAK_TIME_MINUTES, breakMinutes)
            put(COLUMN_PAY_TYPE, payType)
            put(COLUMN_PAY_RATE, payRate)
            put(COLUMN_OVERTIME_RATE, overtimeRate)
            put(COLUMN_TOTAL_EARNINGS, totalEarnings)
        }
        return try {
            val newRowId = db.insert("work_schedule", null, values)
            if (newRowId != -1L) {
                Log.d("Database", "Work Schedule inserted succesffuly with ID: $newRowId")
                true
            } else {
                Log.e("Database", "Failed to insert work schedule")
                false
            }
        } catch (e: Exception) {
            Log.e("Database", "Error while inserting work schedule: ${e.message}")
            false
        } finally {
            db.close()
        }
    }

    fun insertSavedSchedule(name: String, startTime: String, endTime: String, breakTime: Int, payType: String, hourlyRate: Double, overtimeRate: Double) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SCHEDULE_NAME, name)
            put(COLUMN_START_TIME, startTime)
            put(COLUMN_END_TIME, endTime)
            put(COLUMN_BREAK_TIME_MINUTES, breakTime)
            put(COLUMN_PAY_TYPE, payType)
            put(COLUMN_PAY_RATE, hourlyRate)
            put(COLUMN_OVERTIME_RATE, overtimeRate)
        }
        db.insert("saved_schedules", null, values)
        Log.d("Database", "Schedule save: $name")
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

        return cursor
    }


    fun getAllSavedSchedules(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM saved_schedules", null)
    }



}
