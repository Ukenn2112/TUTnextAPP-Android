package com.meikenn.tama.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_bus_schedule")
data class CachedBusScheduleEntity(
    @PrimaryKey
    val key: String = "busSchedule",
    val jsonData: String,
    val lastFetchTime: Long
)
