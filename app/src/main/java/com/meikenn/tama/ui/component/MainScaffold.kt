package com.meikenn.tama.ui.component

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.meikenn.tama.ui.navigation.Route
import androidx.compose.foundation.layout.PaddingValues

private data class BottomTab(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavHostController,
    userInitials: String,
    assignmentBadgeCount: Int = 0,
    onSettingsClick: () -> Unit = {},
    onTeacherEmailClick: () -> Unit = {},
    onPrintSystemClick: () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableIntStateOf(1) } // Default to Timetable
    var showMoreMenu by remember { mutableStateOf(false) }

    val tabs = remember {
        listOf(
            BottomTab("バス", Icons.Default.DirectionsBus, Route.BUS),
            BottomTab("時間割", Icons.Default.CalendarMonth, Route.TIMETABLE),
            BottomTab("課題", Icons.Default.EditNote, Route.ASSIGNMENT),
            BottomTab("その他", Icons.Default.MoreHoriz, "more")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = when (selectedTab) {
                        0 -> "バス時刻表"
                        1 -> "時間割"
                        2 -> "課題"
                        else -> "TUTnext"
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable(onClickLabel = "設定を開く") { onSettingsClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userInitials,
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    if (index == 3) {
                        // "More" tab - shows dropdown instead of navigating
                        NavigationBarItem(
                            selected = false,
                            onClick = { showMoreMenu = true },
                            icon = {
                                Box {
                                    Icon(tab.icon, contentDescription = tab.label)
                                    DropdownMenu(
                                        expanded = showMoreMenu,
                                        onDismissRequest = { showMoreMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("年間予定") },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.CalendarMonth,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            onClick = {
                                                showMoreMenu = false
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://tamauniv.jp/campuslife/calendar"))
                                                context.startActivity(intent)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("スマホサイト") },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Smartphone,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            onClick = {
                                                showMoreMenu = false
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://next.tama.ac.jp"))
                                                context.startActivity(intent)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("たまゆに") },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Language,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            onClick = {
                                                showMoreMenu = false
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://tamauniv.jp"))
                                                context.startActivity(intent)
                                            }
                                        )
                                        HorizontalDivider()
                                        DropdownMenuItem(
                                            text = { Text("教師メール") },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Email,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            onClick = {
                                                showMoreMenu = false
                                                onTeacherEmailClick()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("印刷システム") },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Print,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            onClick = {
                                                showMoreMenu = false
                                                onPrintSystemClick()
                                            }
                                        )
                                        HorizontalDivider()
                                        DropdownMenuItem(
                                            text = { Text("設定") },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Settings,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            onClick = {
                                                showMoreMenu = false
                                                onSettingsClick()
                                            }
                                        )
                                    }
                                }
                            },
                            label = { Text(tab.label) }
                        )
                    } else {
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = {
                                if (selectedTab != index) {
                                    selectedTab = index
                                    navController.navigate(tab.route) {
                                        popUpTo(Route.TIMETABLE) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                }
                            },
                            icon = {
                                if (index == 2 && assignmentBadgeCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge { Text(assignmentBadgeCount.toString()) }
                                        }
                                    ) {
                                        Icon(tab.icon, contentDescription = tab.label)
                                    }
                                } else {
                                    Icon(tab.icon, contentDescription = tab.label)
                                }
                            },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}
