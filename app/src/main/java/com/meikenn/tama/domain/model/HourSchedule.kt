package com.meikenn.tama.domain.model

data class HourSchedule(
    val hour: Int,
    val times: List<TimeEntry>
)
