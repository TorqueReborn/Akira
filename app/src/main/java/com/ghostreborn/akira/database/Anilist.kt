package com.ghostreborn.akira.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anilist")
data class Anilist(
    @PrimaryKey
    val id: String,
    val malID: String,
    val allAnimeID: String,
    val title: String,
    val progress: String
)