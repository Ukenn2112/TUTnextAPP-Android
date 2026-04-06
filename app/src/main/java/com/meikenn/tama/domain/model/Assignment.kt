package com.meikenn.tama.domain.model

import java.util.Date
import java.util.concurrent.TimeUnit

data class Assignment(
    val id: String,
    val title: String,
    val courseId: String,
    val courseName: String,
    val dueDate: Date,
    val description: String,
    val status: AssignmentStatus = AssignmentStatus.PENDING,
    val url: String = ""
) {
    val isOverdue: Boolean
        get() = dueDate.before(Date())

    val isUrgent: Boolean
        get() {
            if (isOverdue) return false
            val diffMs = dueDate.time - System.currentTimeMillis()
            return TimeUnit.MILLISECONDS.toHours(diffMs) < 2
        }

    val remainingTimeText: String
        get() {
            if (isOverdue) return "期限切れ"

            val diffMs = dueDate.time - System.currentTimeMillis()
            val totalDays = TimeUnit.MILLISECONDS.toDays(diffMs)
            val totalHours = TimeUnit.MILLISECONDS.toHours(diffMs)
            val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)

            return when {
                totalDays > 0 -> {
                    val remainingHours = totalHours - totalDays * 24
                    if (remainingHours > 0) "${totalDays}日${remainingHours}時間"
                    else "${totalDays}日"
                }
                totalHours > 0 -> {
                    val remainingMinutes = totalMinutes - totalHours * 60
                    if (remainingMinutes > 0) "${totalHours}時間${remainingMinutes}分"
                    else "${totalHours}時間"
                }
                totalMinutes > 0 -> "${totalMinutes}分"
                else -> "まもなく期限"
            }
        }

    val isPending: Boolean
        get() = status == AssignmentStatus.PENDING

    /** Fingerprint for deduplication (id changes every request) */
    val fingerprint: String
        get() = "$courseId-$title-${dueDate.time}"
}

enum class AssignmentStatus {
    PENDING,
    COMPLETED
}
