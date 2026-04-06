package com.meikenn.tama.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "course_color")
data class CourseColorEntity(
    @PrimaryKey
    val jugyoCd: String,
    val colorIndex: Int
)
