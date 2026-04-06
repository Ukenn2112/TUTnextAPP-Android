package com.meikenn.tama.domain.model

import java.util.Calendar
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
        get() = Date().after(dueDate)

    val isUrgent: Boolean
        get() {
            if (isOverdue) return false
            val diffMs = dueDate.time - System.currentTimeMillis()
            val diffHours = TimeUnit.MILLISECONDS.toHours(diffMs)
            return diffHours < 2
        }

    val remainingTimeText: String
        get() {
            val now = Date()
            if (now.after(dueDate)) return "期限切れ"

            val diffMs = dueDate.time - now.time
            val days = TimeUnit.MILLISECONDS.toDays(diffMs)
            val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)

            return when {
                days > 0 -> "${days}日"
                hours > 0 -> "${hours}時間"
                minutes > 0 -> "${minutes}分"
                else -> "まもなく期限"
            }
        }

    val isPending: Boolean
        get() = status == AssignmentStatus.PENDING

    /** Fingerprint for deduplication (id changes every request) */
    val fingerprint: String
        get() = "$courseId-$title-${dueDate.time / 1000}"
}

enum class AssignmentStatus {
    PENDING,
    COMPLETED
}
