package com.meikenn.tama.domain.model

data class DaySchedule(
    val routeType: RouteType,
    val scheduleType: ScheduleType,
    val hourSchedules: List<HourSchedule>
)
