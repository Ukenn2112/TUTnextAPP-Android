package com.meikenn.tama.ui.feature.bus

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.meikenn.tama.ui.navigation.LocalScaffoldPadding

// Colors matching iOS appPrimary and other constants
private val AppPrimary = Color(0xFF6650A4)
private val OrangeAccent = Color(0xFFFF9800)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusScheduleScreen(
    viewModel: BusScheduleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scaffoldPadding = LocalScaffoldPadding.current

    when {
        uiState.isLoading && uiState.schedule == null -> {
            Box(
                modifier = Modifier.fillMaxSize().padding(scaffoldPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        uiState.error != null && uiState.schedule == null -> {
            ErrorContent(
                message = uiState.error!!,
                onRetry = { viewModel.fetchBusSchedule() },
                modifier = Modifier.fillMaxSize().padding(scaffoldPadding)
            )
        }
        uiState.schedule != null -> {
            BusScheduleContent(
                uiState = uiState,
                viewModel = viewModel,
                scaffoldPadding = scaffoldPadding,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BusScheduleContent(
    uiState: BusScheduleUiState,
    viewModel: BusScheduleViewModel,
    scaffoldPadding: PaddingValues = PaddingValues(0.dp),
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
            // Header items: messages (0 or 1) + schedule type (1) + route (1) + time card (1) + table header (1)
            val headerOffset = (if (schedule.temporaryMessages.isNotEmpty()) 1 else 0) + 4
            listState.animateScrollToItem(index + headerOffset)
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = scaffoldPadding,
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        // 1. Temporary messages (horizontal scroll cards)
        if (schedule.temporaryMessages.isNotEmpty()) {
            item(key = "messages") {
                TemporaryMessagesRow(messages = schedule.temporaryMessages)
            }
        }

        // 2. Schedule type selector (segmented)
        item(key = "scheduleType") {
            ScheduleTypeSelector(
                selected = uiState.selectedScheduleType,
                onSelect = { viewModel.selectScheduleType(it) }
            )
        }

        // 3. Route selector (pill buttons)
        item(key = "routeSelector") {
            RouteSelector(
                selected = uiState.selectedRoute,
                onSelect = { viewModel.selectRoute(it) }
            )
        }

        // 4. Floating time card
        item(key = "timeCard") {
            TimeInfoCard(
                uiState = uiState,
                nextBus = viewModel.getNextBus(),
                selectedEntry = uiState.selectedEntry,
                pinMessage = schedule.pin
            )
        }

        // 5. Time table header
        item(key = "tableHeader") {
            TimeTableHeader()
        }

        // 6. Hour rows
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
                isSelected = { entry ->
                    uiState.selectedEntry?.let { it.hour == entry.hour && it.minute == entry.minute } ?: false
                },
                onEntryClick = { viewModel.selectEntry(it) },
                rowIndex = rowIndex
            )
        }

        // 7. Special notes legend
        item(key = "specialNotes") {
            SpecialNotesSection(
                specialNotes = schedule.specialNotes,
                showWednesdayWarning = uiState.selectedScheduleType != ScheduleType.WEDNESDAY
            )
        }

        // Bottom spacer
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// region 1. Temporary Messages

@Composable
private fun TemporaryMessagesRow(messages: List<TemporaryMessage>) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        messages.forEach { message ->
            Box(
                modifier = Modifier
                    .width(260.dp)
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(10.dp),
                        ambientColor = Color.Black.copy(alpha = 0.08f),
                        spotColor = Color.Black.copy(alpha = 0.08f)
                    )
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = OrangeAccent.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable(enabled = message.url != null) {
                        message.url?.let { url ->
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            )
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = OrangeAccent
                    )
                    Text(
                        text = message.title,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 2,
                        modifier = Modifier.weight(1f)
                    )
                    if (message.url != null) {
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// endregion

// region 2. Schedule Type Selector

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
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
        }
    }
}

// endregion

// region 3. Route Selector

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

        // Divider between route pairs
        Box(
            modifier = Modifier
                .height(20.dp)
                .width(1.dp)
                .background(Color.Gray.copy(alpha = 0.3f))
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
        MaterialTheme.colorScheme.surfaceContainerHighest
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
        shadowElevation = if (isSelected) 3.dp else 0.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            color = contentColor
        )
    }
}

// endregion

// region 4. Floating Time Card

@Composable
private fun TimeInfoCard(
    uiState: BusScheduleUiState,
    nextBus: TimeEntry?,
    selectedEntry: TimeEntry?,
    pinMessage: PinMessage?
) {
    val context = LocalContext.current

    // Determine effective target bus (selected or next)
    val targetBus = selectedEntry ?: nextBus
    val isSelectedMode = selectedEntry != null

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left: current time
                Column {
                    Text(
                        text = "現在時刻",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("%02d:%02d", uiState.currentHour, uiState.currentMinute),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Right: countdown
                if (targetBus != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isSelectedMode) "選択したバスまで" else "次のバスまで",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val diffMinutes = (targetBus.hour - uiState.currentHour) * 60 +
                            (targetBus.minute - uiState.currentMinute)
                        val countdownColor = if (isSelectedMode) OrangeAccent else Color(0xFF4CAF50)
                        if (diffMinutes >= 0) {
                            val hours = diffMinutes / 60
                            val mins = diffMinutes % 60
                            val countdownText = if (hours > 0) "${hours}時間${mins}分" else "${mins}分"
                            Text(
                                text = countdownText,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = countdownColor
                            )
                        } else {
                            Text(
                                text = "到着済み",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "本日の運行は終了しました",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Pin message
            if (pinMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(OrangeAccent.copy(alpha = 0.15f))
                        .border(
                            width = 1.dp,
                            color = OrangeAccent.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .then(
                            if (pinMessage.url != null) {
                                Modifier.clickable {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(pinMessage.url))
                                    )
                                }
                            } else Modifier
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "\uD83D\uDCCC",
                            fontSize = 13.sp
                        )
                        Text(
                            text = pinMessage.title,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.weight(1f),
                            maxLines = 2
                        )
                        if (pinMessage.url != null) {
                            Text(
                                text = "詳細",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = OrangeAccent
                            )
                        }
                    }
                }
            }
        }
    }
}

// endregion

// region 5. Time Table Header

@Composable
private fun TimeTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "時間",
            modifier = Modifier.width(70.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "発車時刻",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// endregion

// region 6. Hour Schedule Row

@Composable
private fun HourScheduleRow(
    hourSchedule: HourSchedule,
    isCurrentHour: Boolean,
    isNextBus: (TimeEntry) -> Boolean,
    isSelected: (TimeEntry) -> Boolean,
    onEntryClick: (TimeEntry) -> Unit,
    rowIndex: Int
) {
    val backgroundColor = when {
        isCurrentHour -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        rowIndex % 2 == 0 -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Hour column - 70dp width, centered
            Text(
                text = "${hourSchedule.hour}",
                modifier = Modifier.width(70.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            )

            // Minutes grid
            MinutesGrid(
                times = hourSchedule.times,
                isNextBus = isNextBus,
                isSelected = isSelected,
                onEntryClick = onEntryClick,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )
        }

        // Divider between hours
        HorizontalDivider(
            thickness = 1.dp,
            color = Color.Gray.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun MinutesGrid(
    times: List<TimeEntry>,
    isNextBus: (TimeEntry) -> Boolean,
    isSelected: (TimeEntry) -> Boolean,
    onEntryClick: (TimeEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    // LazyVGrid-like: 5 columns, item width ~50dp, spacing 8h 12v
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        times.chunked(5).forEach { rowTimes ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowTimes.forEach { time ->
                    TimeEntryChip(
                        time = time,
                        isHighlighted = isNextBus(time),
                        isSelected = isSelected(time),
                        onClick = { onEntryClick(time) }
                    )
                }
                // Fill remaining slots for alignment
                repeat(5 - rowTimes.size) {
                    Spacer(modifier = Modifier.size(width = 50.dp, height = 36.dp))
                }
            }
        }
    }
}

@Composable
private fun TimeEntryChip(
    time: TimeEntry,
    isHighlighted: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.clickable { onClick() }
    ) {
        val backgroundColor = when {
            isSelected -> OrangeAccent.copy(alpha = 0.9f)
            isHighlighted -> MaterialTheme.colorScheme.primary
            else -> Color.Transparent
        }
        val textColor = when {
            isSelected -> Color.White
            isHighlighted -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.onSurface
        }

        Box(
            modifier = Modifier
                .size(width = 50.dp, height = 36.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = String.format("%02d", time.minute),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp,
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
                        Color.Red.copy(alpha = 0.8f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = time.specialNote,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        }
    }
}

// endregion

// region 7. Special Notes Legend

@Composable
private fun SpecialNotesSection(
    specialNotes: List<SpecialNote>,
    showWednesdayWarning: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
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
                                Color.Red.copy(alpha = 0.8f),
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
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp)
                    )
                }
            }

            // Wednesday warning
            if (showWednesdayWarning) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Red.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.Red
                        )
                        Text(
                            text = "水曜日は特別ダイヤで運行しています",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.Red
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
