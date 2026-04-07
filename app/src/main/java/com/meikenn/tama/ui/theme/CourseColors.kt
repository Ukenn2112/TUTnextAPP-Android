package com.meikenn.tama.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object CourseColors {
    // iOS exact colors - Light mode
    private val presetsLight = listOf(
        Color.White,                // 0 - white
        Color(0xFFFADBD3),         // 1 - CoursePink
        Color(0xFFFAEBDB),         // 2 - CourseOrange
        Color(0xFFFAFADB),         // 3 - CourseYellow
        Color(0xFFEBFADB),         // 4 - CourseLightGreen
        Color(0xFFDBFADB),         // 5 - CourseGreen
        Color(0xFFDBFAFA),         // 6 - CourseCyan
        Color(0xFFFAEBEB),         // 7 - CoursePinkPurple
        Color(0xFFEBDBFA),         // 8 - CoursePurple
        Color(0xFFDBEBFA),         // 9 - CourseBlue
        Color(0xFFFADBFA)          // 10 - CourseMagenta
    )

    // iOS exact colors - Dark mode
    private val presetsDark = listOf(
        Color(0xFF2C2C2E),         // 0 - dark gray (instead of white)
        Color(0xFF602020),         // 1 - CoursePink dark
        Color(0xFF604020),         // 2 - CourseOrange dark
        Color(0xFF606020),         // 3 - CourseYellow dark
        Color(0xFF406020),         // 4 - CourseLightGreen dark
        Color(0xFF206020),         // 5 - CourseGreen dark
        Color(0xFF206060),         // 6 - CourseCyan dark
        Color(0xFF602040),         // 7 - CoursePinkPurple dark
        Color(0xFF402060),         // 8 - CoursePurple dark
        Color(0xFF204060),         // 9 - CourseBlue dark
        Color(0xFF602060)          // 10 - CourseMagenta dark
    )

    @Composable
    fun getColor(index: Int): Color {
        val presets = if (LocalDarkTheme.current) presetsDark else presetsLight
        return presets.getOrElse(index) { presets[0] }
    }

    // Non-composable version for use outside composition (defaults to light)
    fun getColorLight(index: Int): Color {
        return presetsLight.getOrElse(index) { presetsLight[0] }
    }

    val presets: List<Color> get() = presetsLight
}
