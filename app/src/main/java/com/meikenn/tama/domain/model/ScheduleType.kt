package com.meikenn.tama.domain.model

enum class ScheduleType(val displayName: String, val jsonKey: String) {
    WEEKDAY("平日（水曜日を除く）", "weekday"),
    WEDNESDAY("水曜日", "wednesday"),
    SATURDAY("土曜日", "saturday")
}
