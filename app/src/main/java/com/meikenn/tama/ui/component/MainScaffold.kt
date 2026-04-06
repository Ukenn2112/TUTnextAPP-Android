package com.meikenn.tama.ui.component

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.meikenn.tama.ui.navigation.Route

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
    content: @Composable (Modifier) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableIntStateOf(1) } // Default to Timetable
    var showMoreMenu by remember { mutableStateOf(false) }

    val tabs = remember {
        listOf(
            BottomTab("バス", Icons.Default.DateRange, Route.BUS),
            BottomTab("時間割", Icons.Default.DateRange, Route.TIMETABLE),
            BottomTab("課題", Icons.Default.Edit, Route.ASSIGNMENT),
            BottomTab("その他", Icons.Default.MoreVert, "more")
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "TUTnext",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    // User avatar
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                            .clickable {
                                navController.navigate(Route.SETTINGS)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userInitials,
                            color = Color.White,
                            fontSize = 12.sp,
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
                                            onClick = {
                                                showMoreMenu = false
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://tamauniv.jp/campuslife/calendar"))
                                                context.startActivity(intent)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("スマホサイト") },
                                            onClick = {
                                                showMoreMenu = false
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://next.tama.ac.jp"))
                                                context.startActivity(intent)
                                            }
                                        )
                                        HorizontalDivider()
                                        DropdownMenuItem(
                                            text = { Text("教師メール") },
                                            onClick = {
                                                showMoreMenu = false
                                                navController.navigate(Route.TEACHER_EMAIL)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("印刷システム") },
                                            onClick = {
                                                showMoreMenu = false
                                                navController.navigate(Route.PRINT_SYSTEM)
                                            }
                                        )
                                        HorizontalDivider()
                                        DropdownMenuItem(
                                            text = { Text("設定") },
                                            onClick = {
                                                showMoreMenu = false
                                                navController.navigate(Route.SETTINGS)
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
        content(Modifier.padding(innerPadding))
    }
}
