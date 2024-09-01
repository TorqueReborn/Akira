package com.ghostreborn.akira

import com.ghostreborn.akira.provider.KitsuProvider
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Constants {

    const val SHARED_PREF = "AKIRA"
    const val PREF_TOKEN = "TOKEN"
    const val PREF_REFRESH_TOKEN = "REFRESH_TOKEN"
    const val PREF_USER_ID = "USER_ID"
    const val PREF_TOTAL_LIST = "TOTAL_LIST"

    var offset = 0

    val api = Retrofit.Builder()
        .baseUrl("https://kitsu.io/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(KitsuProvider::class.java)
}