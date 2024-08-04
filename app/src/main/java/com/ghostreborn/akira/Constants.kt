package com.ghostreborn.akira

import android.content.SharedPreferences

object Constants {
    // Shared Preferences
    lateinit var preferences: SharedPreferences
    const val PREF_NAME = "AKIRA_REBORN"
    const val PREF_USER_ID = "USER_ID"
    const val PREF_TOKEN = "USER_TOKEN"
    const val PREF_LOGGED_IN = "LOGGED_IN"

    const val PREF_ALLOW_ADULT = "ALLOW_ADULT"
    const val PREF_ALLOW_UNKNOWN = "ALLOW_UNKNOWN"
    const val PREF_DUB_ENABLED = "DUB_ENABLED"

    // Anilist
    const val AUTH_URL =
        "https://anilist.co/api/v2/oauth/authorize?client_id=20149&response_type=code&redirect_uri=wanpisu://ghostreborn.in"

    // Anime
    var allAnimeID: String = ""
    var animeThumbnail: String = ""
    var animeEpisode: String = ""
    var animeUrl: String = ""
}