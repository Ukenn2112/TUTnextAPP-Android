package com.meikenn.tama.ui.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.meikenn.tama.domain.model.Course
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
import kotlinx.coroutines.launch

val LocalScaffoldPadding = compositionLocalOf { PaddingValues() }

enum class SheetContent {
    NONE, SETTINGS, SETTINGS_DARK_MODE, TEACHER_EMAIL, PRINT_SYSTEM, COURSE_DETAIL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    viewModel: AppNavigationViewModel = hiltViewModel()
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val userInitials by viewModel.userInitials.collectAsStateWithLifecycle()

    if (!isLoggedIn) {
        LoginScreen(onLoginSuccess = { viewModel.refreshLoginState() })
        return
    }

    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    // Sheet state
    var currentSheet by remember { mutableStateOf(SheetContent.NONE) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Course detail params (stored when tapping a course)
    var selectedCourse by remember { mutableStateOf<Course?>(null) }

    // Track if we're showing dark mode within the settings sheet

    fun showSheet(sheet: SheetContent) {
        currentSheet = sheet
    }

    fun hideSheet() {
        scope.launch {
            sheetState.hide()
            currentSheet = SheetContent.NONE
        }
    }

    MainScaffold(
        navController = navController,
        userInitials = userInitials,
        onSettingsClick = { showSheet(SheetContent.SETTINGS) },
        onTeacherEmailClick = { showSheet(SheetContent.TEACHER_EMAIL) },
        onPrintSystemClick = { showSheet(SheetContent.PRINT_SYSTEM) }
    ) { paddingValues ->
        CompositionLocalProvider(LocalScaffoldPadding provides paddingValues) {
            NavHost(
                navController = navController,
                startDestination = Route.TIMETABLE
            ) {
                composable(Route.BUS) {
                    BusScheduleScreen()
                }
                composable(Route.TIMETABLE) {
                    TimetableScreen(
                        onCourseClick = { course ->
                            selectedCourse = course
                            showSheet(SheetContent.COURSE_DETAIL)
                        }
                    )
                }
                composable(Route.ASSIGNMENT) {
                    AssignmentScreen()
                }
            }
        }
    }

    // Bottom sheet for Settings, Teacher, Print, CourseDetail
    if (currentSheet != SheetContent.NONE) {
        ModalBottomSheet(
            onDismissRequest = { currentSheet = SheetContent.NONE },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            dragHandle = null,
            windowInsets = WindowInsets.statusBars,
            modifier = Modifier.fillMaxHeight(0.93f)
        ) {
            when (currentSheet) {
                SheetContent.SETTINGS -> {
                    SettingsScreen(
                        onNavigateBack = { hideSheet() },
                        onNavigateToDarkMode = { currentSheet = SheetContent.SETTINGS_DARK_MODE },
                        onLogout = {
                            hideSheet()
                            viewModel.refreshLoginState()
                        }
                    )
                }
                SheetContent.SETTINGS_DARK_MODE -> {
                    DarkModeSettingsScreen(
                        onNavigateBack = { currentSheet = SheetContent.SETTINGS }
                    )
                }
                SheetContent.TEACHER_EMAIL -> {
                    TeacherEmailListScreen(
                        onNavigateBack = { hideSheet() }
                    )
                }
                SheetContent.PRINT_SYSTEM -> {
                    PrintSystemScreen(
                        onNavigateBack = { hideSheet() }
                    )
                }
                SheetContent.COURSE_DETAIL -> {
                    val course = selectedCourse
                    if (course != null) {
                        val detailViewModel: CourseDetailViewModel = hiltViewModel()
                        LaunchedEffect(course) {
                            detailViewModel.loadCourseDetail(
                                courseName = course.name,
                                jugyoCd = course.jugyoCd ?: "",
                                nendo = course.academicYear ?: 0,
                                kaikoNendo = course.courseYear ?: 0,
                                gakkiNo = course.courseTerm ?: 0,
                                jugyoKbn = course.jugyoKbn ?: "",
                                kaikoYobi = course.weekday ?: 0,
                                jigenNo = course.period ?: 0,
                                teacherName = course.teacher,
                                roomName = course.room
                            )
                        }
                        CourseDetailScreen(
                            onNavigateBack = { hideSheet() },
                            viewModel = detailViewModel
                        )
                    }
                }
                else -> {}
            }
        }
    }

}
