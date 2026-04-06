package com.meikenn.tama.domain.model

data class TimeEntry(
    val hour: Int,
    val minute: Int,
    val isSpecial: Boolean = false,
    val specialNote: String? = null
) {
    val formattedTime: String
        get() = String.format("%02d:%02d", hour, minute)
}
