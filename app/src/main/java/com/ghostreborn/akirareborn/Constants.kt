package com.ghostreborn.akirareborn

import android.content.SharedPreferences
import com.ghostreborn.akirareborn.model.Anime
import com.ghostreborn.akirareborn.model.AnimeDetails
import com.ghostreborn.akirareborn.model.Episode

object Constants {
    lateinit var anime: Anime
    lateinit var animeDetails: AnimeDetails
    lateinit var episodeUrl: String
    lateinit var animeList: ArrayList<Anime>
    lateinit var parsedEpisodes: ArrayList<Episode>
    lateinit var groupedEpisodes: ArrayList<ArrayList<String>>
    lateinit var episodeUrls: ArrayList<String>

    // Anilist
    lateinit var akiraSharedPreferences: SharedPreferences
    val SHARED_PREFERENCE = "AKIRA"
    val AUTH_URL = "https://anilist.co/api/v2/oauth/authorize" +
            "?client_id=20149" +
            "&response_type=code" +
            "&redirect_uri=wanpisu://ghostreborn.in"
}