package com.meikenn.tama.ui.feature.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDarkMode: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("設定") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // User info section
            UserInfoSection(
                fullName = uiState.user?.fullName ?: "ユーザー名",
                username = uiState.user?.username ?: "username",
                initials = viewModel.getInitials()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Account settings
            SectionHeader("アカウント設定")
            SettingsItem(
                title = "パスワード変更",
                icon = Icons.Default.Lock,
                iconTint = Color(0xFF2196F3),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://google.tama.ac.jp/unicornidm/user/tama/password/"))
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App settings
            SectionHeader("アプリ設定")
            SettingsItem(
                title = "ダークモード",
                icon = DarkModeIcon,
                iconTint = Color(0xFF3F51B5),
                detail = viewModel.getDarkModeText(darkMode),
                onClick = onNavigateToDarkMode
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Other section
            SectionHeader("その他")
            SettingsItem(
                title = "利用規約",
                icon = DocIcon,
                iconTint = Color.Gray,
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://tama.qaq.tw/user-agreement"))
                    context.startActivity(intent)
                }
            )
            HorizontalDivider(modifier = Modifier.padding(start = 48.dp))
            SettingsItem(
                title = "プライバシーポリシー",
                icon = PrivacyIcon,
                iconTint = Color(0xFF2196F3),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://tama.qaq.tw/policy"))
                    context.startActivity(intent)
                }
            )
            HorizontalDivider(modifier = Modifier.padding(start = 48.dp))
            SettingsItem(
                title = "フィードバック",
                icon = FeedbackIcon,
                iconTint = Color(0xFF009688),
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:admin@ukenn.top?subject=${Uri.encode("TUTnext アプリフィードバック")}")
                    }
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Logout
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                TextButton(
                    onClick = { viewModel.logout(onLogout) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    enabled = !uiState.isLoggingOut
                ) {
                    if (uiState.isLoggingOut) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "ログアウト",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // App version
            Spacer(modifier = Modifier.height(32.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "TUTnext",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "バージョン 1.0.0 (1)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun UserInfoSection(fullName: String, username: String, initials: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color.Red),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = fullName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "@$username",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    detail: String? = null,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (detail != null) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// Simple icon constants using material icons
private val DarkModeIcon = Icons.Default.Lock // placeholder - moon icon not in default set
private val DocIcon = Icons.Default.Lock
private val PrivacyIcon = Icons.Default.Lock
private val FeedbackIcon = Icons.Default.Lock
