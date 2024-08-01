package com.ghostreborn.akirareborn.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AnilistUserDao {
    @Query("SELECT * FROM Anilist")
    fun getAll(): List<AnilistUser>

    @Query("SELECT * FROM Anilist WHERE allAnimeID=:allAnimeID")
    fun findByAllAnimeID(allAnimeID: String): AnilistUser

    @Insert
    fun insertAll(vararg anilist: AnilistUser)

    @Delete
    fun delete(anilist: AnilistUser)
}