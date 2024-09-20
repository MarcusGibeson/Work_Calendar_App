package com.example.work_calendar_app.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.work_calendar_app.models.WorkSchedule
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkScheduleDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "work_schedule.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "work_schedule"
        private const val COLUMN_ID = "id"
        private const val COLUMN_DATE = "work_date"
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
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertWorkSchedule(date: String, startTime: String, endTime: String, breakMinutes: Int, payType: String, payRate: Double, overtimeRate: Double, totalEarnings: Double): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_DATE, date)
        contentValues.put(COLUMN_START_TIME, startTime)
        contentValues.put(COLUMN_END_TIME, endTime)
        contentValues.put(COLUMN_BREAK_TIME_MINUTES, breakMinutes)
        contentValues.put(COLUMN_PAY_TYPE, payType)
        contentValues.put(COLUMN_PAY_RATE, payRate)
        contentValues.put(COLUMN_OVERTIME_RATE, overtimeRate)
        contentValues.put(COLUMN_TOTAL_EARNINGS, totalEarnings)
        return db.insert(TABLE_NAME, null, contentValues)
    }

    fun getAllWorkSchedules(): List<WorkSchedule> {
        val workSchedules = mutableListOf<WorkSchedule>()
        val cursor = getWorkSchedule()

        if(cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                val startTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_TIME))
                val endTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_TIME))
                val breakTime = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BREAK_TIME_MINUTES))
                val payType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAY_TYPE))
                val payRate = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PAY_RATE))
                val overtimeRate = cursor.getDouble(cursor.getColumnIndexOrThrow(
                    COLUMN_OVERTIME_RATE
                ))
                val totalEarnings = cursor.getDouble(cursor.getColumnIndexOrThrow(
                    COLUMN_TOTAL_EARNINGS
                ))
                val workSchedule = WorkSchedule(id, date, startTime, endTime, breakTime, payType, payRate, overtimeRate, totalEarnings)
                workSchedules.add(workSchedule);

            } while (cursor.moveToNext())
        }

        cursor.close()
        return workSchedules
    }

    fun getWorkSchedule(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME", null)
    }

    fun getWorkScheduleBetweenDates(startDate: Date, endDate: Date): Cursor {
        val db = this.readableDatabase
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val query = "SELECT * FROM $TABLE_NAME WHERE work_date BETWEEN ? AND ?"
        val cursor = db.rawQuery(query, arrayOf(dateFormat.format(startDate), dateFormat.format(endDate)))

        return cursor
    }
}