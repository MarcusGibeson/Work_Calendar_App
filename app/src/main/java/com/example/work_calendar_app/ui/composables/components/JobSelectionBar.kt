import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.work_calendar_app.data.models.Job
import com.example.work_calendar_app.viewmodels.WorkViewModel

@Composable
fun JobSelectionBar(
    jobs: List<Job>,
    jobColors: Map<Long, Color>,
    viewModel: WorkViewModel,
    selectedJobId: Long?,
    currentMonth: Int,
    currentYear: Int,
    baseTextColor: Color,
    innerPadding: PaddingValues
    ) {

//    val selectedJobId by viewModel.selectedJobId.collectAsState()

    Column(modifier = Modifier.padding(innerPadding)) {
        //Job bar under top bar
        if (jobs.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val displayedJobs = jobs.take(4)
                val jobWeight = 1f / displayedJobs.size

                //Create segments for each job
                displayedJobs.forEach { job ->
                    val jobBackgroundColor = jobColors[job.id] ?: Color.Gray
                    Box(
                        modifier = Modifier
                            .weight(jobWeight)
                            .fillMaxHeight()
                            .background(jobBackgroundColor)
                            .clickable {
                                if (selectedJobId == job.id) {
                                    //Switch to show all entries
                                    viewModel.setJobSpecificView(null)
                                } else {
                                    viewModel.setJobSpecificView(job.id)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = job.name, color = baseTextColor)
                    }
                }
            }
        }
    }
}