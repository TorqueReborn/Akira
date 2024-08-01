package com.ghostreborn.akirareborn

import android.content.SharedPreferences
import com.ghostreborn.akirareborn.database.AnilistUser
import com.ghostreborn.akirareborn.model.Anilist
import com.ghostreborn.akirareborn.model.Anime
import com.ghostreborn.akirareborn.model.AnimeDetails
import com.ghostreborn.akirareborn.model.Episode

object Constants {
    lateinit var anime: Anime
    lateinit var animeDetails: AnimeDetails
    lateinit var animeThumbnail: String
    lateinit var episodeUrl: String
    lateinit var animeList: ArrayList<Anime>
    lateinit var parsedEpisodes: ArrayList<Episode>
    lateinit var groupedEpisodes: ArrayList<ArrayList<String>>
    lateinit var episodeUrls: ArrayList<String>

    // Anilist
    lateinit var preferences: SharedPreferences
    val SHARED_PREFERENCE = "AKIRA"
    val AKIRA_TOKEN = "AKIRA_TOKEN"
    val AKIRA_CODE = "AKIRA_CODE"
    val AKIRA_USER_NAME = "AKIRA_USER_NAME"
    val AKIRA_USER_ID = "AKIRA_USER_ID"
    val AKIRA_LOGGED_IN = "AKIRA_LOGGED_IN"
    val AUTH_URL = "https://anilist.co/api/v2/oauth/authorize" +
            "?client_id=20149" +
            "&response_type=code" +
            "&redirect_uri=wanpisu://ghostreborn.in"

    lateinit var anilistToken: String
    lateinit var anilistUserID: String

    lateinit var anilistAnimeList: ArrayList<Anilist>
    var isAnilist: Boolean = false
    var getAllAnimeID: Boolean = false
    // Database
    const val DATABASE_NAME = "anilist"

    // Updated variables
    lateinit var allAnimeID: String

    // For one time use to store anilist to database
    lateinit var anilistAnimes: ArrayList<Anilist>
    lateinit var anilist: AnilistUser

}