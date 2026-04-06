package com.meikenn.tama.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meikenn.tama.data.local.entity.RoomChangeEntity

@Dao
interface RoomChangeDao {
    @Query("SELECT * FROM room_change WHERE expiryDate > :now")
    suspend fun getValidChanges(now: Long = System.currentTimeMillis()): List<RoomChangeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RoomChangeEntity)

    @Query("DELETE FROM room_change WHERE expiryDate <= :now")
    suspend fun clearExpired(now: Long = System.currentTimeMillis())

    @Query("DELETE FROM room_change")
    suspend fun clearAll()
}
