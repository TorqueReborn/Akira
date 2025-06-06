package com.ghostreborn.akira.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AkiraDao {

    @Query("SELECT allAnimeID FROM akira WHERE allAnimeID = :id")
    fun get(id: String): Akira?

    @Query("SELECT allAnimeID FROM akira")
    fun getAll(): List<String>

    @Query("DELETE FROM akira WHERE allAnimeID = :id")
    fun delete(id: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(akira: Akira)

}