package com.meikenn.tama.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.meikenn.tama.data.local.dao.BusScheduleDao
import com.meikenn.tama.data.local.dao.CourseColorDao
import com.meikenn.tama.data.local.dao.PrintRecordDao
import com.meikenn.tama.data.local.dao.RoomChangeDao
import com.meikenn.tama.data.local.dao.TimetableDao
import com.meikenn.tama.data.local.entity.CachedBusScheduleEntity
import com.meikenn.tama.data.local.entity.CachedTimetableEntity
import com.meikenn.tama.data.local.entity.CourseColorEntity
import com.meikenn.tama.data.local.entity.PrintRecordEntity
import com.meikenn.tama.data.local.entity.RoomChangeEntity

@Database(
    entities = [
        CachedTimetableEntity::class,
        CachedBusScheduleEntity::class,
        CourseColorEntity::class,
        PrintRecordEntity::class,
        RoomChangeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timetableDao(): TimetableDao
    abstract fun busScheduleDao(): BusScheduleDao
    abstract fun courseColorDao(): CourseColorDao
    abstract fun printRecordDao(): PrintRecordDao
    abstract fun roomChangeDao(): RoomChangeDao
}
