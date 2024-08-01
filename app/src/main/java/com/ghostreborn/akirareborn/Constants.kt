package com.ghostreborn.akirareborn

import android.content.SharedPreferences
import com.ghostreborn.akirareborn.database.AnilistUser
import com.ghostreborn.akirareborn.model.Anime
import com.ghostreborn.akirareborn.model.AnimeDetails
import com.ghostreborn.akirareborn.model.Episode

object Constants {
    const val DATABASE_NAME = "akira"
    lateinit var allAnimeID: String
    lateinit var anilist: AnilistUser
}