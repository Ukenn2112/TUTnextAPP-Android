package com.meikenn.tama.domain.model

data class BusSchedule(
    val weekdaySchedules: List<DaySchedule>,
    val saturdaySchedules: List<DaySchedule>,
    val wednesdaySchedules: List<DaySchedule>,
    val specialNotes: List<SpecialNote>,
    val temporaryMessages: List<TemporaryMessage> = emptyList(),
    val pin: PinMessage? = null
)
