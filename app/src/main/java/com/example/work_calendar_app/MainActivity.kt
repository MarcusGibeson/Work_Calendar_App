package com.example.work_calendar_app


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.compose.material3.Button
import android.widget.CalendarView
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.recyclerview.widget.RecyclerView
import com.example.work_calendar_app.adapters.WorkDetailsAdapter
import com.example.work_calendar_app.calendar.DayDetailsPopup
import com.example.work_calendar_app.calendar.WorkCalendar
import com.example.work_calendar_app.data.WorkDetails
import com.example.work_calendar_app.data.WorkEntry
import com.example.work_calendar_app.database.WorkScheduleDatabaseHelper
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: WorkScheduleDatabaseHelper
    private lateinit var addWorkActivityLauncher: ActivityResultLauncher<Intent>
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
                onAddOrUpdateOrDeleteEntry()

            }
        }
        setContent {
            MaterialTheme {
                CalendarScreen()
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CalendarScreen() {
        var workEntries by remember { mutableStateOf(mutableMapOf<Long, WorkEntry>()) }
        var currentMonth by remember { mutableStateOf(LocalDate.now()) }
        val context = LocalContext.current
        val sharedPreferences =
            context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

        var topBarColor by remember {
            mutableStateOf(
                Color(
                    sharedPreferences.getInt(
                        "topBarColor",
                        Color.Blue.toArgb()
                    )
                )
            )
        }

        var baseTextColor by remember {
            mutableStateOf(
                Color(
                    sharedPreferences.getInt(
                        "baseTextColor",
                        Color.Blue.toArgb()
                    )
                )
            )
        }

        //Whenever the screen is recomposed, ensure it checks if the preferences have changed
        DisposableEffect(Unit) {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == "topBarColor") {
                    //Update topBarColor when the preference changes
                    topBarColor =
                        Color(sharedPreferences.getInt("topBarColor", Color.Blue.toArgb()))
                } else if (key == "baseTextColor") {
                    baseTextColor =
                        Color(sharedPreferences.getInt("baseTextColor", Color.Black.toArgb()))
                }
            }
            sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

            //Clean up listener when composable leaves the composition
            onDispose {
                sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = "Work Calendar",
                                color = baseTextColor
                            )
                            Spacer(Modifier.weight(1f))
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSelectingRange = !isSelectingRange
                            firstSelectedDate = null
                            secondSelectedDate = null
                            selectedDay = -1
                        }, modifier = Modifier.width(80.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = if (isSelectingRange) R.drawable.ic_selecting_range_mode else R.drawable.ic_single_day_mode),
                                    contentDescription = "Toggle mode"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSelectingRange) "Select Range" else "Single",
                                    fontSize = 12.sp,
                                    color = baseTextColor,
                                    maxLines = 2,
                                    modifier = Modifier.width(80.dp),
                                    textAlign = TextAlign.Start
                                )
                            }

                        }
                    },
                    actions = {
                        IconButton(onClick = { onSettingsClicked() }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(topBarColor.toArgb())
                    )
                )
            },
            content = {innerPadding ->
                CalendarContent(
                    modifier = Modifier.padding(innerPadding),
                    workEntries = workEntries,
                    onMonthChanged = { newMonth ->
                        currentMonth = currentMonth.withMonth(newMonth.value)
                        workEntries = fetchWorkEntriesForMonth(currentMonth.month.value, currentMonth.year)
                    },
                    entryEdited = entryEdited,
                    onEntryEditedChange = { isEdited -> entryEdited = isEdited },
                    onWorkEntriesChanged = { refreshWorkEntries() }
                )
            }
        )
    }

    @Composable
    fun CalendarContent(modifier: Modifier, workEntries: MutableMap<Long, WorkEntry>, onMonthChanged: (Month) -> Unit, entryEdited: Boolean, onEntryEditedChange: (Boolean) -> Unit, onWorkEntriesChanged: () -> Unit) {
        Box(modifier = Modifier) {
            var currentMonth by remember { mutableStateOf(LocalDate.now()) }
            var showPopup by remember { mutableStateOf(false) }

            //Store fetched work details for the selected day
            var workDetailsForPopup by remember { mutableStateOf(WorkDetails(0,"","","","",0.0)) }

            //Store fetched work details for the selected range
            val workDetailsList = remember { mutableStateListOf<WorkDetails>()}

            //Fetch context and database-related work entries
            val context = LocalContext.current
            val daysInMonth = currentMonth.lengthOfMonth()
            val firstDayOfMonth = currentMonth.withDayOfMonth(1).dayOfWeek.value

            //Dummy data for workDays and workEntries
            val workDays = remember { mutableStateListOf<Int>() }
            val sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

            //Retrieve colors from preferences, with default fallback values
            var backgroundColor1 by remember {
                mutableStateOf(
                    Color(
                        sharedPreferences.getInt(
                            "backgroundColor1",
                            Color.Blue.toArgb()
                        )
                    )
                )
            }
            var backgroundColor2 by remember {
                mutableStateOf(
                    Color(
                        sharedPreferences.getInt(
                            "backgroundColor2",
                            Color.White.toArgb()
                        )
                    )
                )
            }

            var baseTextColor by remember {
                mutableStateOf(
                    Color(
                        sharedPreferences.getInt(
                            "baseTextColor",
                            Color.Blue.toArgb()
                        )
                    )
                )
            }

            var baseButtonColor by remember {
                mutableStateOf(
                    Color(
                        sharedPreferences.getInt(
                            "baseButtonColor",
                            Color.Blue.toArgb()
                        )
                    )
                )
            }




            //Whenever the screen is recomposed, ensure it checks if the preferences have changed
            DisposableEffect(Unit) {
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    when (key) {
                        "backgroundColor1" -> {
                            backgroundColor1 = Color(sharedPreferences.getInt("backgroundColor1", Color.Blue.toArgb()))
                        }
                        "backgroundColor2" -> {
                            backgroundColor2 = Color(sharedPreferences.getInt("backgroundColor2", Color.White.toArgb()))
                        }
                        "baseTextColor" -> {
                            baseTextColor = Color(sharedPreferences.getInt("baseTextColor", Color.Black.toArgb()))
                        }
                        "baseButtonColor" -> {
                            baseButtonColor = Color(sharedPreferences.getInt("baseButtonColor", Color.Black.toArgb()))
                        }
                    }
                }
                sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

                //Clean up listener when composable leaves the composition
                onDispose {
                    sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }

            //Initial load
            LaunchedEffect(Unit) {
                loadAllWorkSchedules(workDays, workEntries, currentMonth.monthValue, currentMonth.year)
                Log.d("Workdays", "Initial load - Work Days: $workDays")
            }

            //Fetch data from database in a LaunchedEffect
            LaunchedEffect(currentMonth, workEntriesChanged) {
                workDays.clear()
                loadAllWorkSchedules(workDays, workEntries, currentMonth.monthValue, currentMonth.year)
                Log.d("WorkDays", "Current month: ${currentMonth.month} ${currentMonth.year}, Work Days: $workDays")
            }

            //Reset SelectedDay when switching modes
            LaunchedEffect(isSelectingRange) {
                selectedDay = -1
            }

            //Fetch the data for the selected day
            LaunchedEffect(selectedDay, entryEdited) {
                if (selectedDay != -1 && !isSelectingRange) {
                    val formattedDate = String.format("%04d-%02d-%02d", currentMonth.year, currentMonth.monthValue, selectedDay)
                    Log.d("MainActivity","formatted date: $formattedDate")
                    val workDetails = getWorkDetailsForDate(context, formattedDate)

                    //check if work details are valid
                    workDetailsForPopup = workDetails
                    if (workDetails.startTime === "" && workDetails.endTime === "") {
                        Log.d("MainActivity","No valid work details for popup")
                    } else {
                        Log.d("Popup","Work details: $workDetails")
                        showPopup = true
                    }
                }

                onEntryEditedChange(false)
            }

            //Box around calendar to detect swipe gestures
            val screenWidth = LocalConfiguration.current.screenWidthDp
            var accumulatedDrag = 0f
            val swipeThreshold = screenWidth * 1f

            //Define subtle gradient
            val gradientBrush = Brush.linearGradient(
                colors = listOf(
                    Color(backgroundColor1.toArgb()),
                    Color(backgroundColor2.toArgb())
                ),
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush = gradientBrush)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, dragAmount ->
                                accumulatedDrag += dragAmount //Accumulate drag distance

                                //Check if the accumulated drag surpasses the swipe threshold
                                if (accumulatedDrag > swipeThreshold) {
                                    //Swipe right (previous month)
                                    accumulatedDrag = 0f
                                    currentMonth = currentMonth.minusMonths(1)
                                    onMonthChanged(currentMonth.month)
                                } else if (accumulatedDrag < -swipeThreshold) {
                                    //Swipe left (next month)
                                    accumulatedDrag = 0f
                                    currentMonth = currentMonth.plusMonths(1)
                                    onMonthChanged(currentMonth.month)
                                }
                            },
                            onDragEnd = {
                                //Reset the accumulated drag when the gesture ends
                                accumulatedDrag = 0f
                            }
                        )
                    }
                ) {
                //Main Layout of Calendar
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp, bottom = 0.dp)
                ) {
                    Spacer(modifier = Modifier.height(70.dp)) //spacer between TopBar and prev/next buttons

                    //Row for previous and next buttons with Month title in between
                    Row(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(onClick = {
                            currentMonth = currentMonth.minusMonths(1)
                            onMonthChanged(currentMonth.month)
                        },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = baseButtonColor,
                                contentColor = baseTextColor,
                            )
                        ) {
                            Text("Previous")
                        }
                        Text(
                            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                            color = baseTextColor,
                            modifier = Modifier.padding(
                                PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp)),
                            fontSize = 16.sp
                        )
                        Button(onClick = {
                            currentMonth = currentMonth.plusMonths(1)
                            onMonthChanged(currentMonth.month)
                        },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = baseButtonColor,
                                contentColor = baseTextColor,
                            )
                        ) {
                            Text("Next")
                        }
                    }

                    //Row for weekday headers
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Spacer(modifier = Modifier.weight(0.3f))
                        val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                        for (day in daysOfWeek) {
                            Text (
                                text = day,
                                color = baseTextColor,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 2.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    //Custom Work Calendar
                    WorkCalendar(currentMonth, daysInMonth = daysInMonth, workDays = workDays.toList()) { day ->
                        if (isSelectingRange) {
                            val monthValue = currentMonth.monthValue
                            val yearValue = currentMonth.year

                            if (firstSelectedDate == null) {
                                firstSelectedDate = "$monthValue/$day/$yearValue"
                                Toast.makeText(context, "First date selected: $firstSelectedDate", Toast.LENGTH_SHORT).show()
                            } else if (secondSelectedDate == null) {
                                secondSelectedDate = "$monthValue/$day/$yearValue"
                                Toast.makeText(context, "Second date selected: $secondSelectedDate", Toast.LENGTH_SHORT).show()
                                displayWorkTimesForSelectedDates(firstSelectedDate!!, secondSelectedDate!!, workEntries)
                                isSelectingRange = false
                            }
                        } else {
                            selectedDay = day
                            showPopup = true
                        }
                    }

                    Row(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(16.dp))

                        //Add work schedule button
                        Button(
                            onClick = {
                                val intent = Intent(this@MainActivity, AddWorkActivity::class.java)
                                addWorkActivityLauncher.launch(intent)
                            },
                            modifier = Modifier
                                .padding(start = 0.dp, end = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = baseButtonColor,
                                contentColor = baseTextColor,
                            )
                        ){
                            Text("Add Work Entry")
                        }
                    }


                    //Composable view for Work Details
                    Text(
                        text = "  Work Date  |  Work Time  ",
                        modifier = Modifier.padding(vertical = 0.dp),
                        color = baseTextColor
                    )
                    WorkDetailsList(workEntries, currentMonth)

                    //Popup for displaying details
                    if (showPopup && selectedDay != -1 && !isSelectingRange) {
                        DayDetailsPopup(
                            selectedDay = selectedDay,
                            startTime = workDetailsForPopup.startTime,
                            endTime = workDetailsForPopup.endTime,
                            breakTime = workDetailsForPopup.breakTime,
                            wage = workDetailsForPopup.wage
                        ) { showPopup = false }
                    }
                }
            }
        }
    }

    private fun onSettingsClicked() {
        val intent = Intent(this, UserSettingsActivity::class.java)
        startActivity(intent)
    }
    @Composable
    fun WorkDetailsList(workEntries: MutableMap<Long, WorkEntry>, currentMonth: LocalDate) {
        //Dialog state to show or hide the details dialog
        var showDialog by remember { mutableStateOf(false) }
        var selectedWorkEntry by remember { mutableStateOf<WorkEntry?>(null) }

        //Extracting the current year and month for date formatting
        val currentYear = currentMonth.year
        val currentMonthValue = currentMonth.monthValue

        val context = LocalContext.current
        val sharedPreferences =
            context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

        var baseTextColor by remember {
            mutableStateOf(
                Color(
                    sharedPreferences.getInt(
                        "baseTextColor",
                        Color.Blue.toArgb()
                    )
                )
            )
        }

        var detailsTextColor by remember {
            mutableStateOf(
                Color(
                    sharedPreferences.getInt(
                        "detailsTextColor",
                        Color.Black.toArgb()
                    )
                )
            )
        }
        var detailsDateColor by remember {
            mutableStateOf(
                Color(
                    sharedPreferences.getInt(
                        "detailsDateColor",
                        Color.Blue.toArgb()
                    )
                )
            )
        }
        var detailsWageColor by remember {
            mutableStateOf(
                Color(
                    sharedPreferences.getInt(
                        "detailsWageColor",
                        Color.Red.toArgb()
                    )
                )
            )
        }

        //Refresh on color preference change
        DisposableEffect(Unit) {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                when (key) {
                    "baseTextColor" -> {
                        baseTextColor = Color(sharedPreferences.getInt("baseTextColor", Color.Black.toArgb()))
                    }
                    "detailsTextColor" -> {
                        detailsTextColor = Color(sharedPreferences.getInt("detailsTextColor", Color.Black.toArgb()))
                    }
                    "detailsDateColor" -> {
                        detailsDateColor = Color(sharedPreferences.getInt("detailsDateColor", Color.Blue.toArgb()))
                    }
                    "detailsWageColor" -> {
                        detailsWageColor = Color(sharedPreferences.getInt("detailsWageColor", Color.Red.toArgb()))
                    }
                }
            }
            sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

            //Clean up listener when composable leaves the composition
            onDispose {
                sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
            }
        }


        // Log current month and year
        Log.d("WorkDetailsList", "Current year: $currentYear, Current month: $currentMonthValue")


        //Function to update the workEntries when saving a new or modified entry
        val onSaveEntry: (WorkEntry) -> Unit ={ updatedEntry ->
            Log.d("onSave", "updatedEntry.id: ${updatedEntry.id}")
            Log.d("onSave", "updatedEntry.workDate: ${updatedEntry.workDate}")
            Log.d("onSave", "updatedEntry.startTime: ${updatedEntry.startTime}")
            Log.d("onSave", "updatedEntry.endTime: ${updatedEntry.endTime}")
            Log.d("onSave", "updatedEntry.breakTime: ${updatedEntry.breakTime}")
            Log.d("onSave", "updatedEntry.payType: ${updatedEntry.payType}")
            Log.d("onSave", "updatedEntry.payRate: ${updatedEntry.payRate}")
            Log.d("onSave", "updatedEntry.overtimeRate: ${updatedEntry.overtimeRate}")
            Log.d("onSave", "updatedEntry.netEarnings: ${updatedEntry.netEarnings}")
            val isSuccess = dbHelper.insertWorkSchedule(updatedEntry.id, updatedEntry.workDate, updatedEntry.startTime, updatedEntry.endTime, updatedEntry.breakTime, updatedEntry.payType, updatedEntry.payRate,updatedEntry.overtimeRate, updatedEntry.netEarnings)
            if (isSuccess) {
                showDialog = false
                Log.d("onSave", "Successfully update entry")

                //Update the existing workEntries map with the modified entry
                workEntries[updatedEntry.id] = updatedEntry
                onAddOrUpdateOrDeleteEntry()
            }else {
                Log.e("onSave", "Failed to update entry")
            }
        }

        val onDeleteEntry: () -> Unit = {
            selectedWorkEntry?.let { entryToDelete ->
                val isSuccess = dbHelper.deleteWorkEntry(entryToDelete.id)
                if (isSuccess) {
                    //Remove the entry from workEntries after successful deletion
                    workEntries.remove(entryToDelete.id)

                    showDialog = false
                    Log.d("WorkDetails", "Successfully deleted entry")
                    onAddOrUpdateOrDeleteEntry()
                }else {
                    Log.e("WorkDetails", "Failed to delete entry")
                }
            }
        }

        //Generate work details for the current month
        val workDetailsList = workEntries.mapNotNull { (id, workEntry) ->
            val workDate = workEntry.workDate
            val day = workDate.substring(8, 10).toInt()
            Log.d("WorkDetailsList", "Checking workEntry: ID: $id, Date: $workDate")

            //Create a Localdate for the current year, current month, and specific day
            val workDateLocal = LocalDate.parse(workEntry.workDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            Log.d("WorkDetailsList", "Valid workEntry: ID: $id, Date: $workDateLocal")
                WorkDetails(
                    id = workEntry.id,
                    workDate = workDateLocal.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    startTime = workEntry.startTime,
                    endTime = workEntry.endTime,
                    breakTime = workEntry.breakTime.toString(),
                    wage = workEntry.netEarnings
                ).also { Log.d("WorkDetailsList", "Created WorkDetails: ID: $id, Date: $workDateLocal") }
        }

        // Log the size of the work details list
        Log.d("WorkDetailsList", "WorkDetailsList size: ${workDetailsList.size}")

        //Calculate total wage
        val totalWage = workDetailsList.sumOf {it.wage}


        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(end = 8.dp) //padding for scrollbar
            ) {
                items(workDetailsList) { workDetail ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(75.dp)
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${workDetail.workDate}:",
                                modifier = Modifier.weight(1f),
                                color = detailsDateColor
                            )
                            Text(
                                text = " ${workDetail.startTime} - ${workDetail.endTime}",
                                color = detailsTextColor
                            )
                        }
                        Text(
                            text = "$${workDetail.wage}",
                            modifier = Modifier.padding(start = 8.dp, end = 16.dp),
                            color = detailsWageColor
                        )

                        Text(
                            text = "Details",
                            modifier = Modifier
                                .clickable {
                                    val workDateKey = workDetail.id
                                    selectedWorkEntry = workEntries[workDateKey]
                                    showDialog = true
                                    // Log when a work entry is selected for details
                                    Log.d("WorkDetailsList", "Selected WorkEntry ID: $workDateKey for details")
                                },
                            color = detailsTextColor
                        )
                    }
                }
            }

            Row (
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(16.dp)
                    .border(
                        width = 2.dp,
                        color = detailsDateColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            ){

                Text(
                    text = "Total Earned: ",
                    style = MaterialTheme.typography.headlineMedium,
                    color = detailsTextColor
                )

                //Display total wage amount
                Text(
                    text = "$${String.format("%.2f", totalWage)}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = detailsWageColor
                )
            }



        }

        //Show Dialog if the state is true
        if (showDialog && selectedWorkEntry != null) {
            Log.d("WorkDetailsList", "Showing dialog for selected entry ID: ${selectedWorkEntry!!.id}")
            WorkDetailsPopUp(
                workEntry = selectedWorkEntry!!,
                onClose = { showDialog = false },
                onSave = onSaveEntry,
                onDelete = onDeleteEntry
            )
        }
    }

    @Composable
    fun WorkDetailsPopUp(workEntry: WorkEntry, onClose: () -> Unit,
                         onSave: (WorkEntry) -> Unit, onDelete: () -> Unit) {
        var isEditable by remember { mutableStateOf(false) }

        //State variables for editing work entry details
        var startTime by remember { mutableStateOf(workEntry.startTime) }
        var endTime by remember { mutableStateOf(workEntry.endTime) }
        var breakTime by remember { mutableStateOf(workEntry.breakTime) }
        var payType by remember { mutableStateOf(workEntry.payType) }
        var payRate by remember { mutableStateOf(workEntry.payRate) }
        var overtimeRate by remember { mutableStateOf(workEntry.overtimeRate) }

        Dialog(onDismissRequest = { onClose() }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Work Details", style = MaterialTheme.typography.headlineSmall)

                    Spacer(modifier = Modifier.height(8.dp))

                    //Conditionally render fields based on isEditable
                    if (isEditable) {
                        //Editable Textfields
                        TextField(value = startTime, onValueChange = { startTime = it }, label = { Text("Start Time")})
                        TextField(value = endTime, onValueChange = { endTime = it }, label = { Text("End Time")})
                        TextField(
                            value = breakTime.toString(),
                            onValueChange = {
                                val newValue = it.toIntOrNull()
                                if (newValue != null) {
                                    breakTime = newValue
                                }
                            },
                            label = { Text("Break Time(minutes)")}
                        )
                        TextField(value = payType, onValueChange = { payType = it }, label = { Text("Pay Type")})
                        TextField(
                            value = payRate.toString(),
                            onValueChange = {
                                val newValue = it.toDoubleOrNull()
                                if (newValue != null) {
                                    payRate = newValue
                                }
                            },
                            label = { Text ("Pay Rate")}
                        )
                        TextField(
                            value = overtimeRate.toString(),
                            onValueChange = {
                                val newValue = it.toDoubleOrNull()
                                if (newValue != null) {
                                    overtimeRate = newValue
                                }
                            },
                            label = { Text ("Overtime Rate")}
                        )
                    } else {
                        //Non-editable texts
                        Text(text = "Start Time: $startTime")
                        Text(text = "End Time: $endTime")
                        Text(text = "Break Time: $breakTime minutes")
                        Text(text = "Pay Type: $payType")
                        Text(text = "Pay Rate: $payRate")
                        Text(text = "Overtime Rate: $overtimeRate")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row (
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        //Toggle between Edit and Save
                        Button(onClick = {
                            if (isEditable) {
                                onSave(
                                    workEntry.copy(
                                        startTime = startTime,
                                        endTime = endTime,
                                        breakTime = breakTime,
                                        payRate = payRate,
                                        payType = payType,
                                        overtimeRate = overtimeRate
                                    )
                                )
                            }
                            isEditable = !isEditable
                        }) {
                            Text(if (isEditable) "Save" else "Edit")
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    //Delete button
                    Button(onClick = { onDelete() }) {
                        Text("Delete")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    //Close button
                    Button(onClick = { onClose() }) {
                        Text("Close")
                    }
                }
            }
        }
    }

    private fun loadAllWorkSchedules(workDays: MutableList<Int>, workEntries: MutableMap<Long, WorkEntry>, currentMonth: Int, currentYear: Int) {
        dbHelper = WorkScheduleDatabaseHelper(this)
        val cursor = dbHelper.getAllWorkSchedule()

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
                val netEarnings = cursor.getDouble(cursor.getColumnIndexOrThrow("total_earnings"))

                if (workDate == null) {
                    Log.e("MainActivity-loadAllWorkSchedules", "Invalid work date format: $workDate")
                } else {
                    //Extract month, day, and year from the workDate string
                    if (workDate.length >= 10) {

                        val workYear = workDate.substring(0, 4).toInt()
                        val workMonth = workDate.substring(5, 7).toInt()
                        val workDay = workDate.substring(8, 10).toInt()

                        //Check if the workDate belongs to the current month and year in calendar
                        if (workMonth == currentMonth && workYear == currentYear) {
                            //Add the work entry to the list only if it's in the current month and year
                            workEntries[id] = WorkEntry(
                                id,
                                workDate,
                                startTime,
                                endTime,
                                breakTime,
                                payType,
                                payRate,
                                overtimeRate,
                                netEarnings
                            )
                            workDays.add(workDay)
                        }
                    } else {
                        Log.e("MainActivity-loadAllWorkSchedules", "Invalid work date format: $workDate")
                    }
                }
            } while (cursor.moveToNext())
        } else {
            Log.d("MainActivity-loadAllWorkSchedules", "No work schedules found.")
        }
    }

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
        Log.d("AddWorKActivity-calculateHoursWorked", "Calculated hours worked: $hoursWorked (Start: $startTime, End: $endTime)")
        return hoursWorked
    }

    private suspend fun getWorkDetailsForDate(context: Context, date: String): WorkDetails {
        //Log when function is called
        Log.d("MainActivity-getWorkDetailsForDate", "getWorkDetailsForDate called for date: $date")

        val dbHelper = WorkScheduleDatabaseHelper(context)
        val cursor = dbHelper.getWorkScheduleByDate(date)

        //Log whether the cursor is null or not
        if (cursor == null) {
            Log.e("MainActivity-getWorkDetailsForDate", "Cursor is null. Database query failed for date: $date")
            return WorkDetails(0,"", "", "", "", 0.0)
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
                val wage = cursor.getDouble(cursor.getColumnIndexOrThrow("total_earnings"))

                //Log the extracted data
                Log.d(
                    "MainActivity-getWorkDetailsForDate",
                    "Work Date: $workDate, Start time: $startTime, End Time: $endTime, Break Time: $breakTime, Wage: $wage"
                )

                //Create a WorkDetails object and return it
                return WorkDetails(id, workDate, startTime, endTime, breakTime, wage)
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
        return WorkDetails(0, "", "", "","", 0.0)
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
                val wage = cursor.getDouble(cursor.getColumnIndexOrThrow("total_earnings"))

                //Log or display the work detail
                Log.d("MainActivity-fetchWorkEntriesForMonth", "Loaded WorkDetail for month: $workDate, $startTime - $endTime, Wage: $wage")
                val workDetail = WorkEntry(id, workDate, startTime, endTime, breakTime, payType, payRate, overtimeRate, wage)
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
                val netEarnings = cursor.getDouble(cursor.getColumnIndexOrThrow("total_earnings"))

                //Display or append the work date and times to UI
                Log.d("MainActivity-displayWorkTimesForSelectedDates", "Loaded WorkDetail for range: $workDate, $startTime - $endTime, $breakTime, $payType, $payRate, $overtimeRate, Net Earnings: $netEarnings")
                val workEntry = WorkEntry(id, workDate, startTime, endTime, breakTime, payType, payRate, overtimeRate, netEarnings)
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