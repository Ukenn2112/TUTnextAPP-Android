package com.meikenn.tama.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meikenn.tama.data.local.entity.CachedTimetableEntity

@Dao
interface TimetableDao {
    @Query("SELECT * FROM cached_timetable WHERE `key` = 'timetable'")
    suspend fun getCachedTimetable(): CachedTimetableEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetable(entity: CachedTimetableEntity)

    @Query("DELETE FROM cached_timetable")
    suspend fun clearAll()
}
