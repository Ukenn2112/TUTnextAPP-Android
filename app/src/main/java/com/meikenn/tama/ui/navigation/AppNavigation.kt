package com.meikenn.tama.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.meikenn.tama.ui.component.MainScaffold
import com.meikenn.tama.ui.feature.assignment.AssignmentScreen
import com.meikenn.tama.ui.feature.bus.BusScheduleScreen
import com.meikenn.tama.ui.feature.coursedetail.CourseDetailScreen
import com.meikenn.tama.ui.feature.coursedetail.CourseDetailViewModel
import com.meikenn.tama.ui.feature.login.LoginScreen
import com.meikenn.tama.ui.feature.print.PrintSystemScreen
import com.meikenn.tama.ui.feature.settings.DarkModeSettingsScreen
import com.meikenn.tama.ui.feature.settings.SettingsScreen
import com.meikenn.tama.ui.feature.teacher.TeacherEmailListScreen
import com.meikenn.tama.ui.feature.timetable.TimetableScreen

@Composable
fun AppNavigation(
    viewModel: AppNavigationViewModel = hiltViewModel()
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val userInitials by viewModel.userInitials.collectAsStateWithLifecycle()

    val navController = rememberNavController()

    if (!isLoggedIn) {
        LoginScreen(onLoginSuccess = { viewModel.refreshLoginState() })
    } else {
        MainScaffold(
            navController = navController,
            userInitials = userInitials
        ) { modifier ->
            NavHost(
                navController = navController,
                startDestination = Route.TIMETABLE,
                modifier = modifier
            ) {
                composable(Route.BUS) {
                    BusScheduleScreen()
                }
                composable(Route.TIMETABLE) {
                    TimetableScreen(
                        onCourseClick = { course ->
                            val encodedName = Uri.encode(course.name)
                            val encodedTeacher = Uri.encode(course.teacher)
                            val encodedRoom = Uri.encode(course.room)
                            val route = "courseDetail/${course.jugyoCd ?: ""}/${course.academicYear ?: 0}/${course.courseYear ?: 0}/${course.courseTerm ?: 0}/${Uri.encode(course.jugyoKbn ?: "")}/${course.weekday ?: 0}/${course.period ?: 0}?name=$encodedName&teacher=$encodedTeacher&room=$encodedRoom"
                            navController.navigate(route)
                        }
                    )
                }
                composable(
                    route = "courseDetail/{jugyoCd}/{nendo}/{kaikoNendo}/{gakkiNo}/{jugyoKbn}/{kaikoYobi}/{jigenNo}?name={name}&teacher={teacher}&room={room}",
                    arguments = listOf(
                        navArgument("jugyoCd") { type = NavType.StringType },
                        navArgument("nendo") { type = NavType.IntType },
                        navArgument("kaikoNendo") { type = NavType.IntType },
                        navArgument("gakkiNo") { type = NavType.IntType },
                        navArgument("jugyoKbn") { type = NavType.StringType },
                        navArgument("kaikoYobi") { type = NavType.IntType },
                        navArgument("jigenNo") { type = NavType.IntType },
                        navArgument("name") { type = NavType.StringType; defaultValue = "" },
                        navArgument("teacher") { type = NavType.StringType; defaultValue = "" },
                        navArgument("room") { type = NavType.StringType; defaultValue = "" }
                    )
                ) { backStackEntry ->
                    val detailViewModel: CourseDetailViewModel = hiltViewModel()
                    val args = backStackEntry.arguments ?: return@composable
                    LaunchedEffect(Unit) {
                        detailViewModel.loadCourseDetail(
                            courseName = args.getString("name") ?: "",
                            jugyoCd = args.getString("jugyoCd") ?: "",
                            nendo = args.getInt("nendo"),
                            kaikoNendo = args.getInt("kaikoNendo"),
                            gakkiNo = args.getInt("gakkiNo"),
                            jugyoKbn = args.getString("jugyoKbn") ?: "",
                            kaikoYobi = args.getInt("kaikoYobi"),
                            jigenNo = args.getInt("jigenNo"),
                            teacherName = args.getString("teacher") ?: "",
                            roomName = args.getString("room") ?: ""
                        )
                    }
                    CourseDetailScreen(
                        onNavigateBack = { navController.popBackStack() },
                        viewModel = detailViewModel
                    )
                }
                composable(Route.ASSIGNMENT) {
                    AssignmentScreen()
                }
                composable(Route.SETTINGS) {
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToDarkMode = { navController.navigate(Route.DARK_MODE_SETTINGS) },
                        onLogout = { viewModel.refreshLoginState() }
                    )
                }
                composable(Route.DARK_MODE_SETTINGS) {
                    DarkModeSettingsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(Route.TEACHER_EMAIL) {
                    TeacherEmailListScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(Route.PRINT_SYSTEM) {
                    PrintSystemScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
