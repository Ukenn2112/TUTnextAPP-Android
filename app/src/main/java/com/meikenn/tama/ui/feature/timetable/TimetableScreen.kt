package com.meikenn.tama.ui.feature.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meikenn.tama.domain.model.Course
import com.meikenn.tama.ui.theme.CourseColors
import java.util.Calendar

private val WEEKDAY_NAMES = listOf("月", "火", "水", "木", "金", "土", "日")
private val TIME_COLUMN_WIDTH = 35.dp
private val LEFT_PADDING = 8.dp
private val RIGHT_PADDING = 10.dp
private val GRID_SPACING = 4.dp
private val HEADER_BOTTOM_SPACING = 16.dp
// Header area height: 10dp for the header row + 16dp bottom spacing = 26dp
// Plus top padding 8dp = 34dp total offset before grid
private val HEADER_AREA_HEIGHT = 40.dp // approximate header + spacing

@Composable
fun TimetableScreen(
    onCourseClick: (Course) -> Unit = {},
    viewModel: TimetableViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading && uiState.courses.isEmpty() -> {
                // Loading state
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "読み込み中...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            uiState.error != null && uiState.courses.isEmpty() -> {
                // Error state
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFFFF9800) // orange
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "エラーが発生しました",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.error ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.refresh() },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = "再読み込み",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp)
                        )
                    }
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

/**
 * Get current weekday as string "1"-"7" (Mon=1, Sun=7), or null if not applicable.
 */
private fun getCurrentWeekday(): String? {
    val cal = Calendar.getInstance()
    // Calendar: Sunday=1, Monday=2, ..., Saturday=7
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val mapped = when (dayOfWeek) {
        Calendar.MONDAY -> 1
        Calendar.TUESDAY -> 2
        Calendar.WEDNESDAY -> 3
        Calendar.THURSDAY -> 4
        Calendar.FRIDAY -> 5
        Calendar.SATURDAY -> 6
        Calendar.SUNDAY -> 7
        else -> null
    }
    return mapped?.toString()
}

/**
 * Get current period as string "1"-"7" based on current time, or null if outside class hours.
 */
private fun getCurrentPeriod(): String? {
    val cal = Calendar.getInstance()
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val minute = cal.get(Calendar.MINUTE)
    val totalMinutes = hour * 60 + minute

    return when {
        totalMinutes in 540..629 -> "1"   // 9:00-10:29
        totalMinutes in 640..729 -> "2"   // 10:40-12:09
        totalMinutes in 780..869 -> "3"   // 13:00-14:29
        totalMinutes in 880..969 -> "4"   // 14:40-16:09
        totalMinutes in 980..1069 -> "5"  // 16:20-17:49
        totalMinutes in 1080..1169 -> "6" // 18:00-19:29
        totalMinutes in 1180..1269 -> "7" // 19:40-21:09
        else -> null
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
    val currentWeekday = getCurrentWeekday()
    val currentPeriod = getCurrentPeriod()

    BoxWithConstraints(
        modifier = modifier
            .padding(start = LEFT_PADDING, end = RIGHT_PADDING, top = 8.dp)
    ) {
        val availableWidth = maxWidth
        val availableHeight = maxHeight

        // Calculate cell dimensions like iOS LayoutMetrics
        val columnCount = weekdays.size.coerceAtLeast(1)
        val rowCount = periods.size.coerceAtLeast(1)

        val cellWidth = (availableWidth - TIME_COLUMN_WIDTH - GRID_SPACING * (columnCount - 1)) / columnCount
        val cellHeight = (availableHeight - HEADER_AREA_HEIGHT - GRID_SPACING * (rowCount - 1)) / rowCount

        Column(modifier = Modifier.fillMaxSize()) {
            // Weekday header
            WeekdayHeader(
                weekdays = weekdays,
                currentWeekday = currentWeekday,
                cellWidth = cellWidth
            )

            Spacer(modifier = Modifier.height(HEADER_BOTTOM_SPACING))

            // Grid rows
            Column(verticalArrangement = Arrangement.spacedBy(GRID_SPACING)) {
                periods.forEach { periodInfo ->
                    Row(horizontalArrangement = Arrangement.spacedBy(GRID_SPACING)) {
                        // Time column
                        TimeColumn(
                            periodInfo = periodInfo,
                            height = cellHeight
                        )

                        // Course cells
                        weekdays.forEach { day ->
                            val course = courses[day]?.get(periodInfo.number)
                            val dayIndex = day.toIntOrNull()
                            val displayDay = if (dayIndex != null && dayIndex in 1..7) {
                                WEEKDAY_NAMES[dayIndex - 1]
                            } else day

                            val isCurrentDay = day == currentWeekday
                            val isCurrentPeriod = periodInfo.number == currentPeriod

                            TimeSlotCell(
                                course = course,
                                displayDay = displayDay,
                                period = periodInfo.number,
                                cellWidth = cellWidth,
                                cellHeight = cellHeight,
                                isCurrentDay = isCurrentDay,
                                isCurrentPeriod = isCurrentPeriod,
                                onClick = {
                                    if (course != null) {
                                        onCourseClick(course)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekdayHeader(
    weekdays: List<String>,
    currentWeekday: String?,
    cellWidth: Dp
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(GRID_SPACING),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left spacer for time column
        Spacer(modifier = Modifier.width(TIME_COLUMN_WIDTH))

        weekdays.forEach { day ->
            val dayIndex = day.toIntOrNull()
            val dayName = if (dayIndex != null && dayIndex in 1..7) {
                WEEKDAY_NAMES[dayIndex - 1]
            } else day

            Box(
                modifier = Modifier.width(cellWidth),
                contentAlignment = Alignment.Center
            ) {
                if (day == currentWeekday) {
                    // Current day: green circle badge
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Color(0xFF4CAF50), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayName,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                } else {
                    Text(
                        text = dayName,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeColumn(
    periodInfo: PeriodInfo,
    height: Dp
) {
    Column(
        modifier = Modifier
            .width(TIME_COLUMN_WIDTH)
            .height(height),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = periodInfo.startTime,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = periodInfo.number,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = periodInfo.endTime,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TimeSlotCell(
    course: Course?,
    displayDay: String,
    period: String,
    cellWidth: Dp,
    cellHeight: Dp,
    isCurrentDay: Boolean,
    isCurrentPeriod: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (course != null) {
        CourseColors.getColor(course.colorIndex)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val borderColor = if (isCurrentDay && isCurrentPeriod) {
        Color(0xFF4CAF50) // green
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    val borderWidth = if (isCurrentDay && isCurrentPeriod) 1.5.dp else 1.dp

    Box(
        modifier = Modifier
            .width(cellWidth)
            .height(cellHeight)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
            .clickable(enabled = course != null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (course != null) {
            // Course content
            Column(
                modifier = Modifier.padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = course.name,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )
                if (course.room.isNotBlank()) {
                    Text(
                        text = course.room,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Unread badge
            val unread = course.keijiMidokCnt ?: 0
            if (unread > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-6).dp)
                        .size(15.dp)
                        .background(Color.Red, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = unread.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 11.sp
                    )
                }
            }
        } else {
            // Empty cell
            Text(
                text = "$displayDay$period",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
