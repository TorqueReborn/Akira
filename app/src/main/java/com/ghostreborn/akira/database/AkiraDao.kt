package com.ghostreborn.akira.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AkiraDao {

    @Query("SELECT * FROM akira")
    fun getAll(): List<Akira>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(akira: Akira)

}