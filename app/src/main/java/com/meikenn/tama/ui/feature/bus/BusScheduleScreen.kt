package com.meikenn.tama.ui.feature.bus

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.meikenn.tama.ui.theme.AppColors
import com.meikenn.tama.ui.theme.AppPrimary
import com.meikenn.tama.ui.theme.CurrentHourBgDark
import com.meikenn.tama.ui.theme.CurrentHourBgLight

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
            listState.animateScrollToItem(index + 1) // +1 for table header
        }
    }

    Column(
        modifier = modifier
            .padding(scaffoldPadding)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        // Fixed header area (does not scroll)

        // 1. Temporary messages
        if (schedule.temporaryMessages.isNotEmpty()) {
            TemporaryMessagesRow(messages = schedule.temporaryMessages)
        }

        // 2. Schedule type selector
        ScheduleTypeSelector(
            selected = uiState.selectedScheduleType,
            onSelect = { viewModel.selectScheduleType(it) }
        )

        // 3. Route selector
        RouteSelector(
            selected = uiState.selectedRoute,
            onSelect = { viewModel.selectRoute(it) }
        )

        // 4. Fixed time card
        TimeInfoCard(
            uiState = uiState,
            nextBus = viewModel.getNextBus(),
            selectedEntry = uiState.selectedEntry,
            pinMessage = schedule.pin
        )

        // Scrollable timetable area
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f)
        ) {
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
                Box(modifier = Modifier.animateItemPlacement()) {
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
                        shape = MaterialTheme.shapes.medium,
                        ambientColor = Color.Black.copy(alpha = 0.08f),
                        spotColor = Color.Black.copy(alpha = 0.08f)
                    )
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = AppColors.semantic.warning.copy(alpha = 0.4f),
                        shape = MaterialTheme.shapes.medium
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
                        contentDescription = "お知らせ",
                        modifier = Modifier.size(14.dp),
                        tint = AppColors.semantic.warning
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
                            modifier = Modifier.size(9.dp),
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

    // Unified chip bar inside a surface container
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            types.forEach { type ->
                val isSelected = selected == type
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.surface
                    else Color.Transparent,
                    animationSpec = tween(250),
                    label = "typeBg"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .shadow(
                            elevation = if (isSelected) 1.dp else 0.dp,
                            shape = RoundedCornerShape(50)
                        )
                        .background(bgColor)
                        .clickable { onSelect(type) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = type.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
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
    val allRoutes = listOf(
        RouteType.FROM_SEISEKI_TO_SCHOOL,
        RouteType.FROM_NAGAYAMA_TO_SCHOOL,
        RouteType.FROM_SCHOOL_TO_SEISEKI,
        RouteType.FROM_SCHOOL_TO_NAGAYAMA
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        allRoutes.forEach { route ->
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "chipScale"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) AppPrimary else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = tween(200),
        label = "chipBg"
    )
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Surface(
        onClick = onClick,
        modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale),
        shape = RoundedCornerShape(50),
        color = containerColor,
        shadowElevation = if (isSelected) 2.dp else 0.dp,
        interactionSource = interactionSource
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
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
    val targetBus = selectedEntry ?: nextBus
    val isSelectedMode = selectedEntry != null

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: current time
                Column {
                    Text(
                        text = "現在時刻",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = String.format("%02d:%02d", uiState.currentHour, uiState.currentMinute),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = (-0.5).sp
                        )
                    )
                }

                // Right: countdown
                if (targetBus != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isSelectedMode) "選択したバス" else "次のバスまで",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        val diffTotalSeconds = (targetBus.hour - uiState.currentHour) * 3600 +
                            (targetBus.minute - uiState.currentMinute) * 60 -
                            uiState.currentSecond
                        val countdownColor = if (isSelectedMode) AppColors.semantic.warning else AppColors.semantic.countdown
                        if (diffTotalSeconds > 0) {
                            val hours = diffTotalSeconds / 3600
                            val mins = (diffTotalSeconds % 3600) / 60
                            val secs = diffTotalSeconds % 60
                            val countdownText = when {
                                hours > 0 -> "${hours}時間${mins}分"
                                else -> "${mins}分${secs}秒"
                            }
                            Text(
                                text = countdownText,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = countdownColor
                            )
                        } else if (diffTotalSeconds == 0) {
                            Text(
                                text = "出発時刻です",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = AppColors.semantic.warning
                            )
                        } else {
                            Text(
                                text = "到着済み",
                                style = MaterialTheme.typography.titleLarge,
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
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    onClick = {
                        pinMessage.url?.let { url ->
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    },
                    enabled = pinMessage.url != null
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = "\uD83D\uDCCC", fontSize = 16.sp)
                        Text(
                            text = pinMessage.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f),
                            maxLines = 2
                        )
                        if (pinMessage.url != null) {
                            Text(
                                text = "詳細 →",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
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
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)) // top-only rounding
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "時間",
            modifier = Modifier.width(70.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "発車時刻",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
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
    val currentHourBg = if (isSystemInDarkTheme()) CurrentHourBgDark else CurrentHourBgLight
    val backgroundColor = when {
        isCurrentHour -> currentHourBg
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hour column
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

        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
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
            isSelected -> AppColors.semantic.warning.copy(alpha = 0.9f)
            isHighlighted -> AppPrimary
            else -> Color.Transparent
        }
        val textColor = when {
            isSelected || isHighlighted -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.onSurface
        }

        Box(
            modifier = Modifier
                .size(width = 50.dp, height = 36.dp)
                .clip(MaterialTheme.shapes.extraSmall)
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
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(20.dp)
                    .background(
                        MaterialTheme.colorScheme.error,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = time.specialNote,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onError
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
                shape = MaterialTheme.shapes.medium,
                ambientColor = AppPrimary.copy(alpha = 0.12f),
                spotColor = AppPrimary.copy(alpha = 0.12f)
            )
            .clip(MaterialTheme.shapes.medium)
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(
                                MaterialTheme.colorScheme.error,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = note.symbol,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                    Text(
                        text = note.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Wednesday warning
            if (showWednesdayWarning) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "注意",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "水曜日は特別ダイヤで運行しています",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            ),
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
