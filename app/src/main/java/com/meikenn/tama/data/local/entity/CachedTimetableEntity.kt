package com.meikenn.tama.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_timetable")
data class CachedTimetableEntity(
    @PrimaryKey
    val key: String = "timetable",
    val jsonData: String,
    val lastFetchTime: Long
)
