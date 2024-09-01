package com.ghostreborn.akira

import com.ghostreborn.akira.provider.KitsuProvider
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Constants {

    var userName = ""
    var pass = ""
    var token = "hKnGjxHngD-pdORMAv98deAlmb7foqmpw8XQxYdl99U"
    var userId = "1402888"
    var entryNum = ""

    val api = Retrofit.Builder()
        .baseUrl("https://kitsu.io/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(KitsuProvider::class.java)
}