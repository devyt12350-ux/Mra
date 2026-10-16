package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserAccountEntity::class,
        StudentAccessLockEntity::class,
        DiaryEntryEntity::class,
        AnnouncementEntity::class,
        TimetableSlotEntity::class,
        AssignmentEntity::class,
        ChatThreadEntity::class,
        ChatMessageEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class ScholarDatabase : RoomDatabase() {
    abstract fun scholarDao(): ScholarDao

    companion object {
        @Volatile
        private var INSTANCE: ScholarDatabase? = null

        fun getInstance(context: Context): ScholarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScholarDatabase::class.java,
                    "scholar_hub_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
