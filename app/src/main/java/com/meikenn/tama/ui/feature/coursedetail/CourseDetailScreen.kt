package com.meikenn.tama.ui.feature.coursedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meikenn.tama.domain.model.Announcement
import com.meikenn.tama.domain.model.Attendance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: CourseDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.courseName, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "戻る")
                    }
                }
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
                    // Attendance section
                    if (detail.syuKetuKanriFlg) {
                        item {
                            AttendanceSection(detail.attendance)
                        }
                    }

                    // Announcements section
                    item {
                        Text(
                            "お知らせ (${detail.announcements.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (detail.announcements.isEmpty()) {
                        item {
                            Text(
                                "お知らせはありません",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(detail.announcements) { announcement ->
                            AnnouncementItem(announcement)
                        }
                    }

                    // Memo section
                    item {
                        MemoSection(
                            memoText = uiState.memoText,
                            isSaving = uiState.isSavingMemo,
                            isSaved = uiState.memoSaved,
                            onMemoChanged = viewModel::onMemoChanged,
                            onSave = viewModel::saveMemo
                        )
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun AttendanceSection(attendance: Attendance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
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
                AttendanceRow("出席", attendance.present, attendance.presentRate, MaterialTheme.colorScheme.primary)
                AttendanceRow("欠席", attendance.absent, attendance.absentRate, MaterialTheme.colorScheme.error)
                AttendanceRow("遅刻", attendance.late, attendance.lateRate, MaterialTheme.colorScheme.tertiary)
                AttendanceRow("早退", attendance.early, attendance.earlyRate, MaterialTheme.colorScheme.tertiary)
                AttendanceRow("公欠", attendance.sick, attendance.sickRate, MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
private fun AttendanceRow(label: String, count: Int, rate: Double, color: androidx.compose.ui.graphics.Color) {
    if (count == 0) return
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${count}回 (${String.format("%.0f", rate)}%)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        LinearProgressIndicator(
            progress = { (rate / 100).toFloat() },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun AnnouncementItem(announcement: Announcement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (announcement.isRead)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                announcement.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (announcement.isRead) FontWeight.Normal else FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                announcement.formattedDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MemoSection(
    memoText: String,
    isSaving: Boolean,
    isSaved: Boolean,
    onMemoChanged: (String) -> Unit,
    onSave: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "メモ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSaved) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "保存済み",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                TextButton(
                    onClick = onSave,
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(16.dp).width(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("保存")
                    }
                }
            }
        }
        OutlinedTextField(
            value = memoText,
            onValueChange = onMemoChanged,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            placeholder = { Text("メモを入力...") }
        )
    }
}
