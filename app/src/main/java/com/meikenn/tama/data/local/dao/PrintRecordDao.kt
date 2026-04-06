package com.meikenn.tama.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.meikenn.tama.data.local.entity.PrintRecordEntity

@Dao
interface PrintRecordDao {
    @Query("SELECT * FROM print_record WHERE expiryDate > :now ORDER BY expiryDate DESC")
    suspend fun getValidRecords(now: Long = System.currentTimeMillis()): List<PrintRecordEntity>

    @Insert
    suspend fun insert(entity: PrintRecordEntity)

    @Query("DELETE FROM print_record WHERE expiryDate <= :now")
    suspend fun clearExpired(now: Long = System.currentTimeMillis())
}
