package com.example.work_calendar_app


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.work_calendar_app.data.database.WorkScheduleDatabaseHelper
import com.example.work_calendar_app.data.repositories.WorkRepository
import com.example.work_calendar_app.ui.composables.screens.*
import com.example.work_calendar_app.viewmodels.WorkViewModel
import com.example.work_calendar_app.viewmodels.WorkViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: WorkScheduleDatabaseHelper
    private lateinit var workRepository: WorkRepository

    private lateinit var addWorkActivityLauncher: ActivityResultLauncher<Intent>

    private val workViewModel: WorkViewModel by viewModels {
        WorkViewModelFactory(workRepository)
    }



    var entryEdited by mutableStateOf(false)
    private var workEntriesChanged by mutableStateOf(0)

    private var isSelectingRange by mutableStateOf(false)
    private var firstSelectedDate: String? = null
    private var secondSelectedDate: String? = null
    private var selectedDay by mutableStateOf(-1)

    val launchAddWorkEntryActivity: (Int?, Int?, Int?) -> Unit = {day, month, year ->
        val intent = Intent(this, AddWorkActivity::class.java).apply {
            day?.let { putExtra("selectedDay", it) }
            month?.let { putExtra("selectedMonth", it) }
            year?.let { putExtra("selectedYear", it) }
        }
        addWorkActivityLauncher.launch(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dbHelper = WorkScheduleDatabaseHelper(this)
        workRepository = WorkRepository(dbHelper)

        //Initialize the launcher for AddWorkActivity
        addWorkActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val entryAddedOrUpdated = data?.getBooleanExtra("entryAddedOrUpdated", false)
                if (entryAddedOrUpdated == true) {
                    onAddOrUpdateOrDeleteEntry()
                }
            }
        }

        setContent {
            MaterialTheme {
                CalendarScreen(
                    workViewModel,
                    launchAddWorkEntryActivity
                )
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




}