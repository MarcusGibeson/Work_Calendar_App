package com.example.work_calendar_app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import com.example.work_calendar_app.data.database.WorkScheduleDatabaseHelper
import com.example.work_calendar_app.data.repositories.WorkRepository
import com.example.work_calendar_app.ui.composables.screens.AddWorkEntryScreen
import com.example.work_calendar_app.viewmodels.WorkViewModel
import com.example.work_calendar_app.viewmodels.WorkViewModelFactory

class AddWorkActivity : AppCompatActivity(){

    private val workViewModel: WorkViewModel by viewModels {
        WorkViewModelFactory(WorkRepository(WorkScheduleDatabaseHelper(this)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                AddWorkEntryScreen(
                    viewModel = workViewModel,
                    onSave = { saveWorkEntry() },
                    onSaveSchedule = { saveWorkSchedule() },
                    onFinish = { finish() }
                )
            }
        }
    }

    private fun saveWorkEntry() {
        //save the work entry through the viewModel
        finishWithResult(entryAddedOrUpdated = true)
    }

    private fun saveWorkSchedule() {
        //save the work schedule through the viewModel
        finishWithResult(entryAddedOrUpdated = true)
    }

    private fun finishWithResult(entryAddedOrUpdated: Boolean) {
        val resultIntent = Intent().apply {
            putExtra("entryAddedOrUpdated", entryAddedOrUpdated)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

}