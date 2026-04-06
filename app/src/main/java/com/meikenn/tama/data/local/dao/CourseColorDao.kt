package com.meikenn.tama.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meikenn.tama.data.local.entity.CourseColorEntity

@Dao
interface CourseColorDao {
    @Query("SELECT * FROM course_color WHERE jugyoCd = :jugyoCd")
    suspend fun getColor(jugyoCd: String): CourseColorEntity?

    @Query("SELECT * FROM course_color")
    suspend fun getAll(): List<CourseColorEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColor(entity: CourseColorEntity)

    @Query("DELETE FROM course_color")
    suspend fun clearAll()
}
