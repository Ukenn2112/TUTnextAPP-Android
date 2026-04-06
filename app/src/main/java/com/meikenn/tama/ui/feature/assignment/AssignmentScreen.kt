package com.meikenn.tama.ui.feature.assignment

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meikenn.tama.ui.component.AssignmentCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentScreen(
    viewModel: AssignmentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Read timeTick so recomposition happens on timer tick
    @Suppress("UNUSED_VARIABLE")
    val tick = uiState.timeTick

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("課題") },
                actions = {
                    if (!uiState.isLoading) {
                        IconButton(onClick = viewModel::refresh) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "再読み込み"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading && uiState.assignments.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null && uiState.assignments.isEmpty() -> {
                ErrorState(
                    message = uiState.error.orEmpty(),
                    onRetry = viewModel::loadAssignments,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            uiState.assignments.isEmpty() -> {
                EmptyState(
                    onRetry = viewModel::loadAssignments,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            else -> {
                Column(modifier = Modifier.padding(innerPadding)) {
                    // Filter chips
                    FilterRow(
                        selected = uiState.selectedFilter,
                        onSelect = viewModel::selectFilter,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    // Refreshing indicator
                    if (uiState.isRefreshing) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }

                    // Assignment list
                    val items = uiState.filteredAssignments
                    if (items.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "該当する課題はありません",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(items, key = { it.id }) { assignment ->
                                AssignmentCard(
                                    assignment = assignment,
                                    onClick = {
                                        if (assignment.url.isNotBlank()) {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(assignment.url))
                                            context.startActivity(intent)
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
}

@Composable
private fun FilterRow(
    selected: AssignmentFilter,
    onSelect: (AssignmentFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssignmentFilter.entries.forEach { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelect(filter) },
                label = { Text(filter.label) },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
private fun EmptyState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(60.dp)
        )
        Text(
            text = "課題はありません",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = "現在提出すべき課題はありません。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        TextButton(
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("再読み込み")
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "エラーが発生しました",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        TextButton(
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("再読み込み")
        }
    }
}
