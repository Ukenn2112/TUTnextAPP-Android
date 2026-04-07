package com.meikenn.tama.ui.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import com.meikenn.tama.ui.theme.DarkModeColors
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarkModeSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DarkModeSettingsViewModel = hiltViewModel()
) {
    val currentMode by viewModel.darkMode.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        topBar = {
            TopAppBar(
                title = { Text("外観モード") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Preview icons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                // Sun icon preview
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(DarkModeColors.lightPreviewBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\u2600",
                        fontSize = 32.sp,
                        color = DarkModeColors.lightPreviewIcon
                    )
                }
                Spacer(modifier = Modifier.width(24.dp))
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(DarkModeColors.darkPreviewBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\uD83C\uDF19",
                        fontSize = 32.sp,
                        color = DarkModeColors.darkPreviewIcon
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Options card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                DarkModeOptionRow(
                    title = "システムに従う",
                    iconBackground = Brush.linearGradient(DarkModeColors.systemOptionGradient),
                    selected = currentMode == 0,
                    onClick = { viewModel.setDarkMode(0) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 52.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                DarkModeOptionRow(
                    title = "ライトモード",
                    iconBackground = Brush.linearGradient(DarkModeColors.lightOptionGradient),
                    selected = currentMode == 1,
                    onClick = { viewModel.setDarkMode(1) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 52.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                DarkModeOptionRow(
                    title = "ダークモード",
                    iconBackground = Brush.linearGradient(DarkModeColors.darkOptionGradient),
                    selected = currentMode == 2,
                    onClick = { viewModel.setDarkMode(2) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "「システムに従う」を選択すると、デバイスの設定に合わせて自動的に切り替わります。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun DarkModeOptionRow(
    title: String,
    iconBackground: Brush,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 28x28 gradient badge
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            // Use unicode icons since Material doesn't have sun/moon
            Text(
                text = when {
                    title.contains("ライト") -> "\u2600"
                    title.contains("ダーク") -> "\uD83C\uDF19"
                    else -> "\u2699"
                },
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = "選択中",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
