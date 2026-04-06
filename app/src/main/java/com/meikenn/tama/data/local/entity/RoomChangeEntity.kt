package com.meikenn.tama.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "room_change")
data class RoomChangeEntity(
    @PrimaryKey
    val courseName: String,
    val newRoom: String,
    val expiryDate: Long
)
