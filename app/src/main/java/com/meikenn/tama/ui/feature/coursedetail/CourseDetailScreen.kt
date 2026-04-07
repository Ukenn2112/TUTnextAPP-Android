package com.meikenn.tama.ui.feature.coursedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.meikenn.tama.ui.theme.AppColors
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meikenn.tama.domain.model.Announcement
import com.meikenn.tama.domain.model.Attendance
import com.meikenn.tama.ui.theme.CourseColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: CourseDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        topBar = {
            TopAppBar(
                title = { Text("科目詳細") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "戻る")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                }
            }
            uiState.courseDetail != null -> {
                val detail = uiState.courseDetail!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Hero header card
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        DetailCard {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Course name
                                Text(
                                    uiState.courseName,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // Period badge as capsule
                                val courseColor = CourseColors.getColor(uiState.colorIndex)
                                if (uiState.periodInfo.isNotEmpty()) {
                                    Text(
                                        text = uiState.periodInfo,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(courseColor.copy(alpha = 0.5f))
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                // Teacher info
                                if (uiState.teacherName.isNotEmpty()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = "教員",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            uiState.teacherName,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Room info
                                if (uiState.roomName.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = "教室",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            uiState.roomName,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Announcements card
                    item {
                        DetailCard {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Section header
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = "掲示",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "掲示情報",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "${detail.announcements.size}",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                if (detail.announcements.isEmpty()) {
                                    Text(
                                        "お知らせはありません",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    detail.announcements.forEachIndexed { index, announcement ->
                                        if (index > 0) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                color = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        }
                                        AnnouncementRow(announcement)
                                    }
                                }
                            }
                        }
                    }

                    // Attendance card
                    if (detail.syuKetuKanriFlg) {
                        item {
                            AttendanceCard(detail.attendance)
                        }
                    }

                    // Memo card
                    item {
                        MemoCard(
                            memoText = uiState.memoText,
                            isSaving = uiState.isSavingMemo,
                            isSaved = uiState.memoSaved,
                            hasChanges = uiState.memoHasChanges,
                            onMemoChanged = viewModel::onMemoChanged,
                            onSave = viewModel::saveMemo
                        )
                    }

                    // Color picker card
                    item {
                        ColorPickerCard(
                            selectedIndex = uiState.colorIndex,
                            onColorSelected = viewModel::onColorSelected
                        )
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun DetailCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = MaterialTheme.shapes.medium,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f)
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        content()
    }
}

@Composable
private fun AnnouncementRow(announcement: Announcement) {
    Column {
        Text(
            announcement.title,
            fontSize = 14.sp,
            fontWeight = if (announcement.isRead) FontWeight.Normal else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            announcement.formattedDate,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AttendanceCard(attendance: Attendance) {
    val semantic = AppColors.semantic
    val presentColor = semantic.success
    val absentColor = MaterialTheme.colorScheme.error
    val lateColor = semantic.warning
    val earlyColor = semantic.earlyLeave
    val sickColor = semantic.excusedAbsence

    DetailCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "出席状況",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (attendance.total == 0) {
                Text(
                    "出席データがありません",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Large count numbers in a row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AttendanceCountItem("出席", attendance.present, presentColor)
                    AttendanceCountItem("欠席", attendance.absent, absentColor)
                    AttendanceCountItem("遅刻", attendance.late, lateColor)
                    AttendanceCountItem("早退", attendance.early, earlyColor)
                    AttendanceCountItem("公欠", attendance.sick, sickColor)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stacked horizontal bar (capsule shape)
                val total = attendance.total.toFloat()
                if (total > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                    ) {
                        val segments = listOf(
                            attendance.present to presentColor,
                            attendance.absent to absentColor,
                            attendance.late to lateColor,
                            attendance.early to earlyColor,
                            attendance.sick to sickColor
                        ).filter { it.first > 0 }

                        segments.forEach { (count, color) ->
                            Box(
                                modifier = Modifier
                                    .weight(count.toFloat() / total)
                                    .height(8.dp)
                                    .background(color)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Legend with colored dots
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val items = listOf(
                            "出席" to presentColor,
                            "欠席" to absentColor,
                            "遅刻" to lateColor,
                            "早退" to earlyColor,
                            "公欠" to sickColor
                        )
                        items.forEach { (label, color) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    label,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceCountItem(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "$count",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MemoCard(
    memoText: String,
    isSaving: Boolean,
    isSaved: Boolean,
    hasChanges: Boolean,
    onMemoChanged: (String) -> Unit,
    onSave: () -> Unit
) {
    DetailCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "メモ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = memoText,
                onValueChange = onMemoChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { Text("メモ") },
                shape = MaterialTheme.shapes.small
            )

            if (hasChanges || isSaving) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onSave,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    if (isSaved) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        "保存",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorPickerCard(
    selectedIndex: Int,
    onColorSelected: (Int) -> Unit
) {
    DetailCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "カラー",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 5-column grid of 40x40 circles, skip index 0
            val colors = CourseColors.presets.drop(1) // skip index 0
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                maxItemsInEachRow = 5
            ) {
                colors.forEachIndexed { listIndex, color ->
                    val actualIndex = listIndex + 1 // offset since we dropped index 0
                    val isSelected = actualIndex == selectedIndex
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (isSelected) Modifier.border(
                                    2.5.dp,
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                ) else Modifier
                            )
                            .clickable { onColorSelected(actualIndex) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "選択中",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
