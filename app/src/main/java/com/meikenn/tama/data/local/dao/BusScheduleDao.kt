package com.meikenn.tama.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meikenn.tama.data.local.entity.CachedBusScheduleEntity

@Dao
interface BusScheduleDao {
    @Query("SELECT * FROM cached_bus_schedule WHERE `key` = 'busSchedule'")
    suspend fun getCachedBusSchedule(): CachedBusScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusSchedule(entity: CachedBusScheduleEntity)

    @Query("DELETE FROM cached_bus_schedule")
    suspend fun clearAll()
}
