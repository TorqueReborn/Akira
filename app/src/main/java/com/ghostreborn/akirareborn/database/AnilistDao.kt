package com.ghostreborn.akirareborn.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AnilistDao {
    @Query("SELECT * FROM anilist")
    fun getAll(): List<Anilist>

    @Query("SELECT * FROM anilist WHERE allAnimeID=:allAnimeID")
    fun findByAllAnimeID(allAnimeID: String): Anilist

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg anilist: Anilist)

    @Delete
    fun delete(anilist: Anilist)
}