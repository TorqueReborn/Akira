package com.ghostreborn.akira

import com.ghostreborn.akira.parser.Gojo
import com.ghostreborn.akira.provider.KitsuProvider
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Constants {

    const val SHARED_PREF = "AKIRA"
    const val PREF_TOKEN = "TOKEN"
    const val PREF_REFRESH_TOKEN = "REFRESH_TOKEN"
    const val PREF_USER_ID = "USER_ID"

    var offset = 0
    var animeID = ""
    var animeThumbnail = ""
    var animeEpisode = ""
    var episodeWatchId = ""
    var animeUrl = ""

    val animeAPI = Gojo()

    val api = Retrofit.Builder()
        .baseUrl("https://kitsu.io/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(KitsuProvider::class.java)
}