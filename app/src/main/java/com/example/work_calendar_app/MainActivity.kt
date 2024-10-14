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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import com.example.work_calendar_app.calendar.DayDetailsPopup
import com.example.work_calendar_app.calendar.WorkCalendar
import com.example.work_calendar_app.data.Job
import com.example.work_calendar_app.data.WorkDetails
import com.example.work_calendar_app.data.WorkEntry
import com.example.work_calendar_app.database.WorkScheduleDatabaseHelper
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: WorkScheduleDatabaseHelper
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
        var showJobDialog by remember { mutableStateOf(false) }
        var jobName by remember { mutableStateOf("") }
        var workEntries by remember { mutableStateOf(mutableMapOf<Long, WorkEntry>()) }
        var currentMonth by remember { mutableStateOf(LocalDate.now()) }
        val context = LocalContext.current
        val dbHelper = WorkScheduleDatabaseHelper(context)
        val sharedPreferences =
            context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)


        //colors
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
                        Color.Black.toArgb()
                    )
                )
            )
        }

        var workDay1Color by remember {
            mutableStateOf(
                Color(
                    sharedPreferences.getInt(
                        "workDay1Color",
                        Color.Yellow.toArgb()
                    )
                )
            )
        }
        var workDay2Color by remember {
            mutableStateOf(
                Color(
                    sharedPreferences.getInt(
                        "workDay2Color",
                        Color.Yellow.toArgb()
                    )
                )
            )
        }
        var workDay3Color by remember {
            mutableStateOf(
                Color(
                    sharedPreferences.getInt(
                        "workDay3Color",
                        Color.Yellow.toArgb()
                    )
                )
            )
        }
        var workDay4Color by remember {
            mutableStateOf(
                Color(
                    sharedPreferences.getInt(
                        "workDay4Color",
                        Color.Green.toArgb()
                    )
                )
            )
        }


        val jobs = remember { mutableStateOf(listOf<Job>()) }
        val jobColorMap = remember(workDay1Color, workDay2Color, workDay3Color, workDay4Color) {
            derivedStateOf {
                mapOf(
                    1L to workDay1Color,
                    2L to workDay2Color,
                    3L to workDay3Color,
                    4L to workDay4Color
                )
            }
        }

        //Fetch jobs when the screen is composed
        LaunchedEffect(Unit) {
            val jobList = dbHelper.fetchJobsFromDatabase()
            jobs.value = jobList
        }

        //Whenever the screen is recomposed, ensure it checks if the preferences have changed
        DisposableEffect(Unit) {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                when (key) {
                    "topBarColor" -> {
                        topBarColor = Color(sharedPreferences.getInt("topBarColor", Color.Blue.toArgb()))
                    }
                    "baseTextColor" -> {
                        baseTextColor = Color(sharedPreferences.getInt("baseTextColor", Color.Black.toArgb()))
                    }
                    "workDay1Color" -> {
                        workDay1Color = Color(sharedPreferences.getInt("workDay1Color", Color.Yellow.toArgb()))
                    }
                    "workDay2Color" -> {
                        workDay2Color = Color(sharedPreferences.getInt("workDay2Color", Color.Red.toArgb()))
                    }
                    "workDay3Color" -> {
                        workDay3Color = Color(sharedPreferences.getInt("workDay3Color", Color.Green.toArgb()))
                    }
                    "workDay4Color" -> {
                        workDay4Color = Color(sharedPreferences.getInt("workDay4Color", Color.White.toArgb()))
                    }
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
                        IconButton(onClick = {
                            showJobDialog = true
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Manage Jobs")
                        }
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
                Column(modifier = Modifier.padding(innerPadding)) {
                    //Job bar under top bar
                    if (jobs.value.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val displayedJobs = jobs.value.take(4)

                            //Create segments for each job
                            displayedJobs.forEach { job ->
                                val jobBackgroundColor = jobColorMap.value[job.id] ?: Color.Gray
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(jobBackgroundColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = job.name, color = baseTextColor)
                                }
                            }

                            //If there are fewer than 4 jobs, fill the remaining space with empty boxes
                            repeat(4 - displayedJobs.size) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(Color.LightGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    //Empty Space
                                }
                            }
                        }
                    }


                    CalendarContent(
                        modifier = Modifier.padding(innerPadding),
                        workEntries = workEntries,
                        onMonthChanged = { newMonth ->
                            currentMonth = currentMonth.withMonth(newMonth.value)
                            workEntries = fetchWorkEntriesForMonth(
                                currentMonth.month.value,
                                currentMonth.year
                            )
                        },
                        entryEdited = entryEdited,
                        onEntryEditedChange = { isEdited -> entryEdited = isEdited },
                        onWorkEntriesChanged = { refreshWorkEntries() }
                    )

                    //Show the Job Management Dialog conditionally
                    if (showJobDialog) {
                        JobManagementDialog(
                            onDismiss = { showJobDialog = false },
                            onJobAdded = { jobName ->
                                //Insert the new job into the database
                                dbHelper.insertJob(jobName)
                                Toast.makeText(context, "Job '$jobName' added successfully!", Toast.LENGTH_SHORT).show()

                            }
                        )
                    }
                }
            }
        )
    }

    @Composable
    fun CalendarContent(modifier: Modifier, workEntries: MutableMap<Long, WorkEntry>, onMonthChanged: (Month) -> Unit, entryEdited: Boolean, onEntryEditedChange: (Boolean) -> Unit, onWorkEntriesChanged: () -> Unit) {
        Box(modifier = Modifier) {
            var currentMonth by remember { mutableStateOf(LocalDate.now()) }
            var showPopup by remember { mutableStateOf(false) }

            //Store fetched work details for the selected day
            var workDetailsForPopup by remember { mutableStateOf(WorkDetails(0,0,"","","","", "",0.0, 0.0,0.0,0.0, 0.0)) }

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
                            Color(143, 216, 230).toArgb()
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
                            Color.Black.toArgb()
                        )
                    )
                )
            }

            var baseButtonColor by remember {
                mutableStateOf(
                    Color(
                        sharedPreferences.getInt(
                            "baseButtonColor",
                            Color(204, 153, 255).toArgb()
                        )
                    )
                )
            }

            var detailsTextColor by remember {
                mutableStateOf(Color(sharedPreferences.getInt("detailsTextColor", Color.Black.toArgb())))
            }
            var workDay1Color by remember {
                mutableStateOf(
                    Color(
                        sharedPreferences.getInt(
                            "workDay1Color",
                            Color.Yellow.toArgb()
                        )
                    )
                )
            }
            var workDay2Color by remember {
                mutableStateOf(
                    Color(
                        sharedPreferences.getInt(
                            "workDay2Color",
                            Color.Yellow.toArgb()
                        )
                    )
                )
            }
            var workDay3Color by remember {
                mutableStateOf(
                    Color(
                        sharedPreferences.getInt(
                            "workDay3Color",
                            Color.Yellow.toArgb()
                        )
                    )
                )
            }
            var workDay4Color by remember {
                mutableStateOf(
                    Color(
                        sharedPreferences.getInt(
                            "workDay4Color",
                            Color.Green.toArgb()
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
                        "detailsTextColor" -> {
                            detailsTextColor = Color(sharedPreferences.getInt("detailsTextColor", Color.Black.toArgb()))
                        }
                        "workDay1Color" -> {
                            workDay1Color = Color(sharedPreferences.getInt("workDay1Color", Color.Yellow.toArgb()))
                        }
                        "workDay2Color" -> {
                            workDay2Color = Color(sharedPreferences.getInt("workDay2Color", Color.Red.toArgb()))
                        }
                        "workDay3Color" -> {
                            workDay3Color = Color(sharedPreferences.getInt("workDay3Color", Color.Green.toArgb()))
                        }
                        "workDay4Color" -> {
                            workDay4Color = Color(sharedPreferences.getInt("workDay4Color", Color.White.toArgb()))
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

            var isLoading by remember { mutableStateOf(true) }

            //Fetch the data for the selected day
            LaunchedEffect(selectedDay, entryEdited) {
                if (selectedDay != -1 && !isSelectingRange) {
                    isLoading = true
                    workDetailsForPopup = WorkDetails(0, 0,"", "", "", "", "", 0.0, 0.0 ,0.0, 0.0, 0.0)

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
                        isLoading = false
                    }
                }

                onEntryEditedChange(false)
            }


            val jobColorMap = mutableMapOf<Long, Color>()

            //Load colors from shared preferences
            LaunchedEffect(Unit) {
                jobColorMap[1] = workDay1Color
                jobColorMap[2] = workDay2Color
                jobColorMap[3] = workDay3Color
                jobColorMap[4] = workDay4Color
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
                            Text("Previous", color = detailsTextColor)
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
                            Text("Next", color = detailsTextColor)
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
                    WorkCalendar(currentMonth, daysInMonth = daysInMonth, workDays = workDays { day ->
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
                            Text("Add Work Entry", color = detailsTextColor)
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
                    if(isLoading) {
                        CircularProgressIndicator()
                    } else if (showPopup && selectedDay != -1 && !isSelectingRange) {
                        DayDetailsPopup(
                            selectedDay = selectedDay,
                            startTime = workDetailsForPopup.startTime,
                            endTime = workDetailsForPopup.endTime,
                            breakTime = workDetailsForPopup.breakTime,
                            payType = workDetailsForPopup.payType,
                            payRate = workDetailsForPopup.payRate,
                            commissionSales = workDetailsForPopup.commissionSales,
                            dailySalary = workDetailsForPopup.dailySalary,
                            tips = workDetailsForPopup.tips,
                            netEarnings = workDetailsForPopup.netEarnings
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
                        Color.Black.toArgb()
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
            Log.d("onSave", "updatedEntry.jobId: ${updatedEntry.jobId}")
            Log.d("onSave", "updatedEntry.workDate: ${updatedEntry.workDate}")
            Log.d("onSave", "updatedEntry.startTime: ${updatedEntry.startTime}")
            Log.d("onSave", "updatedEntry.endTime: ${updatedEntry.endTime}")
            Log.d("onSave", "updatedEntry.breakTime: ${updatedEntry.breakTime}")
            Log.d("onSave", "updatedEntry.payType: ${updatedEntry.payType}")
            Log.d("onSave", "updatedEntry.payRate: ${updatedEntry.payRate}")
            Log.d("onSave", "updatedEntry.overtimeRate: ${updatedEntry.overtimeRate}")
            Log.d("onSave", "updatedEntry.salaryAmount: ${updatedEntry.salaryAmount}")
            Log.d("onSave", "updatedEntry.commissionRate: ${updatedEntry.commissionRate}")
            Log.d("onSave", "updatedEntry.commissionDetails: ${updatedEntry.commissionDetails}")
            Log.d("onSave", "updatedEntry.tips: ${updatedEntry.tips}")
            val isSuccess = dbHelper.insertWorkSchedule(updatedEntry.id, updatedEntry.jobId, updatedEntry.workDate, updatedEntry.startTime, updatedEntry.endTime, updatedEntry.breakTime, updatedEntry.payType, updatedEntry.payRate,updatedEntry.overtimeRate, updatedEntry.commissionRate, updatedEntry.commissionDetails, updatedEntry.salaryAmount, updatedEntry.tips)
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
            val formattedWorkDate = workDateLocal.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
            Log.d("WorkDetailsList", "Valid workEntry: ID: $id, Date: $workDateLocal")
            Log.d("WorkDetailsList", "Verifying date: ID: $id, Date: $formattedWorkDate")
                WorkDetails(
                    id = workEntry.id,
                    jobId = workEntry.jobId,
                    workDate = formattedWorkDate,
                    startTime = workEntry.startTime,
                    endTime = workEntry.endTime,
                    breakTime = workEntry.breakTime.toString(),
                    payType = workEntry.payType,
                    payRate = workEntry.payRate,
                    commissionSales = workEntry.totalCommissionAmount,
                    dailySalary = workEntry.dailySalary,
                    tips = workEntry.tips,
                    netEarnings = workEntry.netEarnings
                ).also { Log.d("WorkDetailsList", "Created WorkDetails: ID: $id, Date: $workDateLocal") }
        }

        // Log the size of the work details list
        Log.d("WorkDetailsList", "WorkDetailsList size: ${workDetailsList.size}")

        //Calculate total wage
        val totalWage = workDetailsList.sumOf {it.netEarnings}


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
                            text = "$${workDetail.netEarnings}",
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
        var salaryAmount by remember { mutableStateOf(workEntry.salaryAmount) }
        var dailySalary by remember { mutableStateOf(workEntry.dailySalary) }
        var commissionRate by remember { mutableStateOf(workEntry.commissionRate) }
        var commissionDetails by remember { mutableStateOf(workEntry.commissionDetails)}

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

                        //Pay type specific fields
                        when (payType) {
                            "Hourly" -> {
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
                            }
                            "Salary" -> {
                                TextField(
                                    value = salaryAmount.toString(),
                                    onValueChange = {
                                        val newValue = it.toDoubleOrNull()
                                        if (newValue != null) {
                                            salaryAmount = newValue
                                        }
                                    },
                                    label = { Text ("Yearly Salary")}
                                )
                            }
                            "Commission" -> {
                                TextField(
                                    value = commissionRate.toString(),
                                    onValueChange = {
                                        val newValue = it.toIntOrNull()
                                        if (newValue != null) {
                                            commissionRate = newValue
                                        }
                                    },
                                    label = { Text ("Commission Rate")}
                                )
                                LazyColumn(
                                    modifier = Modifier
                                        .height(150.dp)
                                        .fillMaxWidth()
                                ){
                                    items(commissionDetails) {detail ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            TextField(
                                                value = detail.toString(),
                                                onValueChange = {
                                                    val newValue = it.toDoubleOrNull()
                                                    if (newValue != null) {
                                                        val index = commissionDetails.indexOf(detail)
                                                        commissionDetails = commissionDetails.toMutableList().apply {
                                                            set(index, newValue)
                                                        }
                                                    }
                                                },
                                                label = { Text("Sale amount") },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        //Delete button
                                        IconButton(
                                            onClick = {
                                                val index = commissionDetails.indexOf(detail)
                                                if (index != -1) {
                                                    commissionDetails = commissionDetails.toMutableList().apply {
                                                        removeAt(index)
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Sale")
                                        }
                                    }
                                }

                                Button(onClick = {
                                    //Add new sale to the list
                                    commissionDetails = commissionDetails.toMutableList().apply { add (0.0) }
                                }) {
                                    Text("Add sale")
                                }
                            }
                        }
                    } else {
                        //Non-editable texts
                        Text(text = "Start Time: $startTime")
                        Text(text = "End Time: $endTime")
                        Text(text = "Break Time: $breakTime minutes")
                        Text(text = "Pay Type: $payType")
                        when (payType) {
                            "Hourly" -> {
                                Text(text = "Pay Rate: $payRate")
                                Text(text = "Overtime Rate: $overtimeRate")
                            }
                            "Salary" -> {
                                Text(text = "Total salary: $salaryAmount")
                                Text(text = "Amount earned today: $dailySalary")
                            }
                            "Commission" -> {
                                Text(text = "Commission rate: $commissionRate")
                                Text(text = "Sale totals: $commissionDetails")
                            }
                        }


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
                                        overtimeRate = overtimeRate,
                                        commissionRate = commissionRate,
                                        commissionDetails = commissionDetails,
                                        salaryAmount = salaryAmount
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

    @Composable
    fun JobManagementDialog(
        onDismiss: () -> Unit,
        onJobAdded: (String) -> Unit
    ) {
        var jobName by remember { mutableStateOf("") }
        val context = LocalContext.current
        val sharedPreferences =
            context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "Enter Job Name", style = MaterialTheme.typography.headlineSmall)

                    TextField(
                        value = jobName,
                        onValueChange = { jobName = it },
                        label = { Text("Job Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        TextButton(onClick = {
                            if (jobName.isNotBlank()) {
                                onJobAdded(jobName)
                                onDismiss()
                            }
                        }) {
                            Text("Add Job")
                        }
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

                val commissionSalesList = commissionDetails.split(",")
                    .mapNotNull { it.trim().toDoubleOrNull() }

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
                                jobId,
                                workDate,
                                startTime,
                                endTime,
                                breakTime,
                                payType,
                                payRate,
                                overtimeRate,
                                commissionRate,
                                commissionSalesList,
                                totalCommissionSales,
                                salaryAmount,
                                dailySalary,
                                tips,
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


    private suspend fun getWorkDetailsForDate(context: Context, date: String): WorkDetails {
        //Log when function is called
        Log.d("MainActivity-getWorkDetailsForDate", "getWorkDetailsForDate called for date: $date")

        val dbHelper = WorkScheduleDatabaseHelper(context)
        val cursor = dbHelper.getWorkScheduleByDate(date)

        //Log whether the cursor is null or not
        if (cursor == null) {
            Log.e("MainActivity-getWorkDetailsForDate", "Cursor is null. Database query failed for date: $date")
            return WorkDetails(0,0, "", "", "", "", "",0.0,0.0, 0.0, 0.0, 0.0)
        }

        //Check if the cursor has data
        if (cursor.moveToFirst()) {
            try {
                //Extract the data from the cursor
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val jobId = cursor.getLong(cursor.getColumnIndexOrThrow("job_id"))
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
                    "Job ID: $jobId, Work Date: $workDate, Start time: $startTime, End Time: $endTime, Break Time: $breakTime, Pay Type: $payType, Pay Rate: $payRate, Commission Sales; $commissionSales, Daily Salary: $dailySalary, Tips: $tips, Net Earnings: $netEarnings"
                )

                //Create a WorkDetails object and return it
                return WorkDetails(id, jobId, workDate, startTime, endTime, breakTime, payType, payRate, commissionSales, dailySalary, tips, netEarnings)
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
        return WorkDetails(0, 0, "", "", "","", "",0.0, 0.0,0.0, 0.0, 0.0)
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

                val commissionSalesList = commissionDetails.split(",")
                    .mapNotNull { it.trim().toDoubleOrNull() }

                //Log or display the work detail
                Log.d("MainActivity-fetchWorkEntriesForMonth", "Loaded WorkDetail for month: $jobId, $workDate, $startTime - $endTime, Tips: $tips, Net Earnings: $netEarnings")
                val workDetail = WorkEntry(id, jobId, workDate, startTime, endTime, breakTime, payType, payRate, overtimeRate, commissionRate, commissionSalesList, totalCommissionSales, salaryAmount, dailySalary, tips, netEarnings)
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

                val commissionSalesList = commissionDetails.split(",")
                    .mapNotNull { it.trim().toDoubleOrNull() }

                //Display or append the work date and times to UI
                Log.d("MainActivity-displayWorkTimesForSelectedDates", "Loaded WorkDetail for range: $workDate, $startTime - $endTime, $breakTime, $payType, $payRate, $overtimeRate, $commissionRate, $commissionSalesList, $totalCommissionSales, $salaryAmount, $dailySalary, Tips: $tips, Net Earnings: $netEarnings")
                val workEntry = WorkEntry(id, jobId, workDate, startTime, endTime, breakTime, payType, payRate, overtimeRate, commissionRate, commissionSalesList, totalCommissionSales, salaryAmount, dailySalary, tips, netEarnings)
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