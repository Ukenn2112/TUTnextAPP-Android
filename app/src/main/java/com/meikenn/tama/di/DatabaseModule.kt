package com.meikenn.tama.di

import android.content.Context
import androidx.room.Room
import com.meikenn.tama.data.local.AppDatabase
import com.meikenn.tama.data.local.dao.BusScheduleDao
import com.meikenn.tama.data.local.dao.CourseColorDao
import com.meikenn.tama.data.local.dao.PrintRecordDao
import com.meikenn.tama.data.local.dao.RoomChangeDao
import com.meikenn.tama.data.local.dao.TimetableDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "tutnext_database"
        ).build()
    }

    @Provides
    fun provideTimetableDao(db: AppDatabase): TimetableDao = db.timetableDao()

    @Provides
    fun provideBusScheduleDao(db: AppDatabase): BusScheduleDao = db.busScheduleDao()

    @Provides
    fun provideCourseColorDao(db: AppDatabase): CourseColorDao = db.courseColorDao()

    @Provides
    fun providePrintRecordDao(db: AppDatabase): PrintRecordDao = db.printRecordDao()

    @Provides
    fun provideRoomChangeDao(db: AppDatabase): RoomChangeDao = db.roomChangeDao()
}
