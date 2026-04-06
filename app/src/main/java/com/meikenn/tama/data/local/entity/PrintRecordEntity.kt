package com.meikenn.tama.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "print_record")
data class PrintRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val prCode: String,
    val fileName: String,
    val expiryDate: Long,
    val pageCount: Int,
    val duplex: String,
    val fileSize: String,
    val nUp: String
)
