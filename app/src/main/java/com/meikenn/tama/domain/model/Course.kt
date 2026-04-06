package com.meikenn.tama.domain.model

import java.util.UUID

data class Course(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val room: String,
    val teacher: String,
    val startTime: String,  // HH:MM
    val endTime: String,    // HH:MM
    val colorIndex: Int = 0,
    val weekday: Int? = null,   // 1=Mon, 7=Sun
    val period: Int? = null,    // 1-7
    val jugyoCd: String? = null,
    val jugyoKbn: String? = null,
    val academicYear: Int? = null,
    val courseYear: Int? = null,
    val courseTerm: Int? = null,
    val keijiMidokCnt: Int? = null
) {
    val weekdayString: String
        get() {
            val weekdays = listOf("月", "火", "水", "木", "金", "土", "日")
            return if (weekday != null && weekday in 1..7) weekdays[weekday - 1] else ""
        }

    val periodInfo: String
        get() = if (period != null) {
            "${weekdayString}曜日 ${period}限"
        } else {
            "$weekdayString $startTime - $endTime"
        }
}
