package com.ghostreborn.akira.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Anilist::class], version = 1, exportSchema = false)
abstract class AnilistDatabase:RoomDatabase() {
    abstract fun anilistDao(): AnilistDao
}