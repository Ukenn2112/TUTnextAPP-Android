package com.meikenn.tama.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

// App Primary (iOS: AppPrimary / AccentColor) - #F4868E
val AppPrimary = Color(0xFFF4868E)

// Light theme palette
val AppPrimaryLight = Color(0xFFF4868E)
val AppOnPrimary = Color.White
val AppPrimaryContainer = Color(0xFFFFDAD9)
val AppOnPrimaryContainer = Color(0xFF410006)

// Dark theme palette
val AppPrimaryDarkTheme = Color(0xFFFFB3B4)
val AppOnPrimaryDark = Color(0xFF68000F)
val AppPrimaryContainerDark = Color(0xFF8F0019)
val AppOnPrimaryContainerDark = Color(0xFFFFDAD9)

// Card effects
val CardGlowLight = Color(0x00000000)
val CardGlowDark = Color(0x0DFFFFFF)
val CardShadowLight = Color(0x1A000000)
val CardShadowDark = Color(0x12FFFFFF)

// Notification badge background
val NotificationBadgeBgLight = Color(0x26FF2D55)
val NotificationBadgeBgDark = Color(0x40FF2D55)

// Current hour highlight
val CurrentHourBgLight = Color(0x1A34C759)
val CurrentHourBgDark = Color(0x3334C759)

// Teacher gradient top
val TeacherGradientTopLight = Color(0x4DF2F2F7)
val TeacherGradientTopDark = Color(0xFF1C1C1E)

// ============================================================
// Semantic Colors (app-specific tokens for light/dark themes)
// ============================================================

@Immutable
data class AppSemanticColors(
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,
    val urgent: Color,
    val onUrgent: Color,
    val countdown: Color,
    val earlyLeave: Color,
    val excusedAbsence: Color,
    val avatarGradientStart: Color,
    val avatarGradientEnd: Color,
    val selectedFilter: Color,
    val onSelectedFilter: Color,
)

val LightSemanticColors = AppSemanticColors(
    success = Color(0xFF4CAF50),
    onSuccess = Color.White,
    warning = Color(0xFFFF9800),
    onWarning = Color.White,
    urgent = Color(0xFFFF3B30),
    onUrgent = Color.White,
    countdown = Color(0xFF34C759),
    earlyLeave = Color(0xFF9C27B0),
    excusedAbsence = Color(0xFF2196F3),
    avatarGradientStart = Color(0xFFEF5350),
    avatarGradientEnd = Color(0xFFC62828),
    selectedFilter = Color(0xFF007AFF),
    onSelectedFilter = Color.White,
)

val DarkSemanticColors = AppSemanticColors(
    success = Color(0xFF81C784),
    onSuccess = Color.Black,
    warning = Color(0xFFFFB74D),
    onWarning = Color.Black,
    urgent = Color(0xFFFF6B6B),
    onUrgent = Color.Black,
    countdown = Color(0xFF66D17A),
    earlyLeave = Color(0xFFCE93D8),
    excusedAbsence = Color(0xFF64B5F6),
    avatarGradientStart = Color(0xFFEF5350),
    avatarGradientEnd = Color(0xFFC62828),
    selectedFilter = Color(0xFF64B5F6),
    onSelectedFilter = Color.Black,
)

val LocalSemanticColors = staticCompositionLocalOf { LightSemanticColors }
val LocalDarkTheme = staticCompositionLocalOf { false }

// Convenience accessor
object AppColors {
    val semantic: AppSemanticColors
        @Composable
        get() = LocalSemanticColors.current
}

// Settings icon gradient colors
object SettingsIconColors {
    val busGradient = listOf(Color(0xFF42A5F5), Color(0xFF1E88E5))
    val timetableGradient = listOf(Color(0xFFEF5350), Color(0xFFC62828))
    val assignmentGradient = listOf(Color(0xFFAB47BC), Color(0xFF7B1FA2))
    val teacherGradient = listOf(Color(0xFF66BB6A), Color(0xFF388E3C))
    val printGradient = listOf(Color(0xFFFF7043), Color(0xFFE64A19))
    val settingsGradient = listOf(Color(0xFF78909C), Color(0xFF455A64))
    val darkModeGradient = listOf(Color(0xFF5C6BC0), Color(0xFF283593))
    val docGradient = listOf(Color(0xFF26C6DA), Color(0xFF00838F))
    val privacyGradient = listOf(Color(0xFF8D6E63), Color(0xFF4E342E))
    val feedbackGradient = listOf(Color(0xFFFFA726), Color(0xFFF57C00))
    val logoutGradient = listOf(Color(0xFFEF5350), Color(0xFFC62828))
}

// Dark mode settings screen colors
object DarkModeColors {
    val lightPreviewBg = Color(0xFFFFF3E0)
    val lightPreviewIcon = Color(0xFFFF9800)
    val darkPreviewBg = Color(0xFFE3F2FD)
    val darkPreviewIcon = Color(0xFF1565C0)
    val systemOptionGradient = listOf(Color(0xFF7E57C2), Color(0xFF4527A0))
    val lightOptionGradient = listOf(Color(0xFFFF8A65), Color(0xFFE64A19))
    val darkOptionGradient = listOf(Color(0xFF5C6BC0), Color(0xFF283593))
}
