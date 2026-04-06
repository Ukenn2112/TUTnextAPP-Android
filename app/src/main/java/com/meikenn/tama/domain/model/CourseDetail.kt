package com.meikenn.tama.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CourseDetail(
    val announcements: List<Announcement> = emptyList(),
    val attendance: Attendance = Attendance(),
    val memo: String = "",
    val syllabusPubFlg: Boolean = false,
    val syuKetuKanriFlg: Boolean = false
)

data class Announcement(
    val id: Int,
    val title: String,
    val date: Long, // milliseconds timestamp
    val torkDate: String? = null // read date
) {
    val formattedDate: String
        get() {
            return try {
                // SimpleDateFormat created per-call (safe for Compose recomposition)
                val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN)
                sdf.format(Date(date))
            } catch (e: Exception) {
                ""
            }
        }

    val isRead: Boolean get() = torkDate != null
}

data class Attendance(
    val present: Int = 0,
    val absent: Int = 0,
    val late: Int = 0,
    val early: Int = 0,
    val sick: Int = 0,
    val unregistered: Int = 0
) {
    val total: Int get() = present + absent + late + early + sick + unregistered

    val presentRate: Double get() = if (total > 0) present.toDouble() / total * 100 else 0.0
    val absentRate: Double get() = if (total > 0) absent.toDouble() / total * 100 else 0.0
    val lateRate: Double get() = if (total > 0) late.toDouble() / total * 100 else 0.0
    val earlyRate: Double get() = if (total > 0) early.toDouble() / total * 100 else 0.0
    val sickRate: Double get() = if (total > 0) sick.toDouble() / total * 100 else 0.0
}
