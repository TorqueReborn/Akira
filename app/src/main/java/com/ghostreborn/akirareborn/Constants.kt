package com.ghostreborn.akirareborn

import android.content.SharedPreferences

object Constants {
    // Shared Preferences
    lateinit var preferences: SharedPreferences
    const val PREF_NAME = "AKIRA_REBORN"

    // Anilist
    const val AUTH_URL =
        "https://anilist.co/api/v2/oauth/authorize?client_id=20149&response_type=code&redirect_uri=wanpisu://ghostreborn.in"
}