package com.ghostreborn.akira.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Akira::class], version = 1, exportSchema = false)
abstract class AkiraDatabase : RoomDatabase() {
    abstract fun akiraDao(): AkiraDao

    companion object {
        @Volatile
        private var INSTANCE: AkiraDatabase? = null

        fun getDatabase(context: Context): AkiraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AkiraDatabase::class.java,
                    "akira"
                ).fallbackToDestructiveMigration(false).build()
                INSTANCE = instance
                instance
            }
        }
    }
}