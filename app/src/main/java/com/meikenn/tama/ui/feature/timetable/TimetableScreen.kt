package com.meikenn.tama.ui.feature.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meikenn.tama.domain.model.Course
import com.meikenn.tama.ui.component.CourseCard

private val WEEKDAY_NAMES = listOf("月", "火", "水", "木", "金", "土", "日")
private val PERIOD_COLUMN_WIDTH = 32.dp
private val CELL_HEIGHT = 80.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    onCourseClick: (Course) -> Unit = {},
    viewModel: TimetableViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val semester = uiState.semester
                    Text(
                        text = if (semester != null) {
                            "${semester.year}年度 ${semester.termName}"
                        } else {
                            "時間割"
                        }
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refresh() },
                        enabled = !uiState.isRefreshing && !uiState.isLoading
                    ) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(8.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "更新"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading && uiState.courses.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.error != null && uiState.courses.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error ?: "エラーが発生しました",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                else -> {
                    TimetableGrid(
                        courses = uiState.courses,
                        weekdays = uiState.getWeekdays(),
                        periods = uiState.getPeriods(),
                        onCourseClick = onCourseClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun TimetableGrid(
    courses: Map<String, Map<String, Course>>,
    weekdays: List<String>,
    periods: List<PeriodInfo>,
    onCourseClick: (Course) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val borderColor = MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 4.dp)
    ) {
        // Header row with weekday names
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Empty corner cell for period column
            Box(
                modifier = Modifier
                    .width(PERIOD_COLUMN_WIDTH)
                    .height(28.dp),
                contentAlignment = Alignment.Center
            ) {
                // intentionally empty
            }

            weekdays.forEach { day ->
                val dayIndex = day.toIntOrNull()
                val dayName = if (dayIndex != null && dayIndex in 1..7) {
                    WEEKDAY_NAMES[dayIndex - 1]
                } else day

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                        .border(0.5.dp, borderColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Period rows
        periods.forEach { periodInfo ->
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Period number column
                Box(
                    modifier = Modifier
                        .width(PERIOD_COLUMN_WIDTH)
                        .height(CELL_HEIGHT)
                        .border(0.5.dp, borderColor),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = periodInfo.number,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = periodInfo.startTime,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Course cells for each weekday
                weekdays.forEach { day ->
                    val course = courses[day]?.get(periodInfo.number)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(CELL_HEIGHT)
                            .border(0.5.dp, borderColor)
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(1.dp)
                    ) {
                        if (course != null) {
                            CourseCard(
                                course = course,
                                modifier = Modifier.fillMaxSize(),
                                onClick = { onCourseClick(course) }
                            )
                        }
                    }
                }
            }
        }
    }
}
