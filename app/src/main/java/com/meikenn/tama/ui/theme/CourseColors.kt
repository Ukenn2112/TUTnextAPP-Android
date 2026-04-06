package com.meikenn.tama.ui.theme

import androidx.compose.ui.graphics.Color

object CourseColors {
    val presets = listOf(
        Color.White,           // 0
        Color(0xFFFFCDD2),     // 1 - pink
        Color(0xFFFFE0B2),     // 2 - orange
        Color(0xFFFFF9C4),     // 3 - yellow
        Color(0xFFC8E6C9),     // 4 - light green
        Color(0xFFA5D6A7),     // 5 - green
        Color(0xFFB2EBF2),     // 6 - cyan
        Color(0xFFF8BBD0),     // 7 - pink purple
        Color(0xFFE1BEE7),     // 8 - purple
        Color(0xFFBBDEFB),     // 9 - blue
        Color(0xFFF48FB1)      // 10 - magenta
    )

    fun getColor(index: Int): Color {
        return presets.getOrElse(index) { presets[0] }
    }
}
