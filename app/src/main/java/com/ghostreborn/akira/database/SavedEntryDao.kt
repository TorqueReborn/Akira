package com.ghostreborn.akira.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SavedEntryDao {
    @Query("SELECT * FROM saved_entries")
    fun getAll(): List<SavedEntry>

    @Query("SELECT * FROM saved_entries WHERE id = :id")
    fun getById(id: String): SavedEntry?

    @Query("DELETE FROM saved_entries WHERE id = :id")
    fun deleteById(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entry: SavedEntry)

}