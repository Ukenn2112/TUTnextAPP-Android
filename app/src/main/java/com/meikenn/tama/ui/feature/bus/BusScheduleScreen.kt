package com.meikenn.tama.ui.feature.bus

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meikenn.tama.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusScheduleScreen(
    viewModel: BusScheduleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("バス時刻表") }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading && uiState.schedule == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null && uiState.schedule == null -> {
                ErrorContent(
                    message = uiState.error!!,
                    onRetry = { viewModel.fetchBusSchedule() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
            uiState.schedule != null -> {
                BusScheduleContent(
                    uiState = uiState,
                    viewModel = viewModel,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun BusScheduleContent(
    uiState: BusScheduleUiState,
    viewModel: BusScheduleViewModel,
    modifier: Modifier = Modifier
) {
    val schedule = uiState.schedule ?: return
    val filteredSchedule = viewModel.getFilteredSchedule()
    val listState = rememberLazyListState()
    // Scroll to current hour on first load
    LaunchedEffect(filteredSchedule, uiState.currentHour) {
        val nextBus = viewModel.getNextBus()
        val targetHour = nextBus?.hour ?: uiState.currentHour
        val visibleSchedules = filteredSchedule?.hourSchedules
            ?.filter { it.times.isNotEmpty() } ?: return@LaunchedEffect
        val index = visibleSchedules.indexOfFirst { it.hour == targetHour }
        if (index >= 0) {
            // Offset by header items: messages card (0 or 1) + schedule type selector (1) + route selector (1) + time card (1)
            val headerOffset = (if (schedule.temporaryMessages.isNotEmpty()) 1 else 0) + 3
            listState.animateScrollToItem(index + headerOffset)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
    ) {
        // Temporary messages
        if (schedule.temporaryMessages.isNotEmpty()) {
            item(key = "messages") {
                TemporaryMessagesRow(messages = schedule.temporaryMessages)
            }
        }

        // Schedule type selector
        item(key = "scheduleType") {
            ScheduleTypeSelector(
                selected = uiState.selectedScheduleType,
                onSelect = { viewModel.selectScheduleType(it) }
            )
        }

        // Route selector
        item(key = "routeSelector") {
            RouteSelector(
                selected = uiState.selectedRoute,
                onSelect = { viewModel.selectRoute(it) }
            )
        }

        // Time card (current time + next bus)
        item(key = "timeCard") {
            TimeInfoCard(
                uiState = uiState,
                nextBus = viewModel.getNextBus(),
                pinMessage = schedule.pin
            )
        }

        // Wednesday special message
        if (uiState.selectedScheduleType == ScheduleType.WEDNESDAY) {
            item(key = "wednesdayMessage") {
                WednesdayInfoBanner()
            }
        }

        // Schedule table
        val visibleSchedules = filteredSchedule?.hourSchedules
            ?.filter { it.times.isNotEmpty() } ?: emptyList()

        items(
            items = visibleSchedules,
            key = { "hour_${it.hour}" }
        ) { hourSchedule ->
            val rowIndex = visibleSchedules.indexOf(hourSchedule)
            HourScheduleRow(
                hourSchedule = hourSchedule,
                isCurrentHour = viewModel.isNextBusHour(hourSchedule.hour),
                isNextBus = { viewModel.isNextBus(it) },
                rowIndex = rowIndex
            )
        }

        // Special notes
        item(key = "specialNotes") {
            SpecialNotesSection(
                specialNotes = schedule.specialNotes,
                showWednesdayWarning = uiState.selectedScheduleType != ScheduleType.WEDNESDAY
            )
        }

        // Bottom spacer
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

// region Temporary Messages

@Composable
private fun TemporaryMessagesRow(messages: List<TemporaryMessage>) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        messages.forEach { message ->
            Card(
                modifier = Modifier.width(260.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        Color(0xFFFF9800).copy(alpha = 0.4f)
                    )
                )
            ) {
                Row(
                    modifier = Modifier
                        .clickable(enabled = message.url != null) {
                            message.url?.let { url ->
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                )
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("⚠", fontSize = 14.sp)
                    Text(
                        text = message.title,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        maxLines = 2
                    )
                }
            }
        }
    }
}

// endregion

// region Schedule Type Selector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleTypeSelector(
    selected: ScheduleType,
    onSelect: (ScheduleType) -> Unit
) {
    val types = ScheduleType.entries

    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        types.forEachIndexed { index, type ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size),
                onClick = { onSelect(type) },
                selected = selected == type
            ) {
                Text(
                    text = type.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1
                )
            }
        }
    }
}

// endregion

// region Route Selector

@Composable
private fun RouteSelector(
    selected: RouteType,
    onSelect: (RouteType) -> Unit
) {
    val stationRoutes = listOf(RouteType.FROM_SEISEKI_TO_SCHOOL, RouteType.FROM_NAGAYAMA_TO_SCHOOL)
    val schoolRoutes = listOf(RouteType.FROM_SCHOOL_TO_SEISEKI, RouteType.FROM_SCHOOL_TO_NAGAYAMA)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        stationRoutes.forEach { route ->
            RouteChip(
                label = route.displayName,
                isSelected = selected == route,
                onClick = { onSelect(route) }
            )
        }

        VerticalDivider(
            modifier = Modifier.height(20.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        schoolRoutes.forEach { route ->
            RouteChip(
                label = route.displayName,
                isSelected = selected == route,
                onClick = { onSelect(route) }
            )
        }
    }
}

@Composable
private fun RouteChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        tonalElevation = if (isSelected) 2.dp else 0.dp,
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor
        )
    }
}

// endregion

// region Time Info Card

@Composable
private fun TimeInfoCard(
    uiState: BusScheduleUiState,
    nextBus: TimeEntry?,
    pinMessage: PinMessage?
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "現在時刻",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("%02d:%02d", uiState.currentHour, uiState.currentMinute),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                if (nextBus != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "次のバスまで",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val diffMinutes = (nextBus.hour - uiState.currentHour) * 60 +
                            (nextBus.minute - uiState.currentMinute)
                        val hours = diffMinutes / 60
                        val mins = diffMinutes % 60
                        val countdownText = if (hours > 0) "${hours}時間${mins}分" else "${mins}分"
                        Text(
                            text = countdownText,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (nextBus.specialNote != null) {
                            Text(
                                text = nextBus.specialNote,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.errorContainer,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "本日の運行は終了しました",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Pin message
            if (pinMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (pinMessage.url != null) {
                                Modifier.clickable {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(pinMessage.url))
                                    )
                                }
                            } else Modifier
                        ),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFF9800).copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("📌", fontSize = 13.sp)
                        Text(
                            text = pinMessage.title,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.weight(1f),
                            maxLines = 2
                        )
                        if (pinMessage.url != null) {
                            Text(
                                text = "詳細 ›",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFFF9800)
                            )
                        }
                    }
                }
            }
        }
    }
}

// endregion

// region Wednesday Info

@Composable
private fun WednesdayInfoBanner() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("ℹ️", fontSize = 16.sp)
            Text(
                text = "水曜日は特別ダイヤで運行しています",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}

// endregion

// region Hour Schedule Row

@Composable
private fun HourScheduleRow(
    hourSchedule: HourSchedule,
    isCurrentHour: Boolean,
    isNextBus: (TimeEntry) -> Boolean,
    rowIndex: Int
) {
    val backgroundColor = when {
        isCurrentHour -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        rowIndex % 2 == 0 -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Hour column
            Text(
                text = "${hourSchedule.hour}",
                modifier = Modifier.width(70.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            )

            VerticalDivider(
                modifier = Modifier.height(IntrinsicSize.Min),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Minutes grid
            FlowMinutesGrid(
                times = hourSchedule.times,
                isNextBus = isNextBus,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun FlowMinutesGrid(
    times: List<TimeEntry>,
    isNextBus: (TimeEntry) -> Boolean,
    modifier: Modifier = Modifier
) {
    // Simple wrapping layout using rows of 5
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        times.chunked(5).forEach { rowTimes ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowTimes.forEach { time ->
                    TimeEntryChip(
                        time = time,
                        isHighlighted = isNextBus(time)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeEntryChip(
    time: TimeEntry,
    isHighlighted: Boolean
) {
    Box(contentAlignment = Alignment.Center) {
        val backgroundColor = when {
            isHighlighted -> MaterialTheme.colorScheme.primary
            else -> Color.Transparent
        }
        val textColor = when {
            isHighlighted -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.onSurface
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = String.format("%02d", time.minute),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                ),
                color = textColor
            )
        }

        // Special note badge
        if (time.specialNote != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-4).dp)
                    .size(18.dp)
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = time.specialNote,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        }
    }
}

// endregion

// region Special Notes

@Composable
private fun SpecialNotesSection(
    specialNotes: List<SpecialNote>,
    showWednesdayWarning: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "備考",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            specialNotes.forEach { note ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = note.symbol,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                    Text(
                        text = note.description,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (showWednesdayWarning) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠", fontSize = 14.sp)
                        Text(
                            text = "水曜日は特別ダイヤで運行しています",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

// endregion

// region Error Content

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("再読み込み")
        }
    }
}

// endregion
