package com.ghostreborn.akira.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_entries")
data class SavedEntry(
    @PrimaryKey
    val kitsuID: String,
    val progress: String,
    val anime: String,
    val thumbnail: String
)