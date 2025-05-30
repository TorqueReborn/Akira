package com.ghostreborn.akira.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "akira")
data class Akira(
    @PrimaryKey
    val allAnimeID: String
)
