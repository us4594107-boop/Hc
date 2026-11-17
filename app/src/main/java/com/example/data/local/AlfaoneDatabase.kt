package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        EmployeeEntity::class,
        AttendanceLogEntity::class,
        LeaveRequestEntity::class,
        PayslipEntity::class,
        TrainingModuleEntity::class,
        AnnouncementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AlfaoneDatabase : RoomDatabase() {
    abstract fun alfaoneDao(): AlfaoneDao

    companion object {
        @Volatile
        private var INSTANCE: AlfaoneDatabase? = null

        fun getInstance(context: Context): AlfaoneDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AlfaoneDatabase::class.java,
                    "alfaone_hc_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
