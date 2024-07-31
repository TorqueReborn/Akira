package com.ghostreborn.akirareborn.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AnilistUser::class], version = 1, exportSchema = false)
abstract class AnilistUserDatabase : RoomDatabase() {
    abstract fun anilistUserDao(): AnilistUserDao
}