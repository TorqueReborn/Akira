package com.ghostreborn.akira

import com.ghostreborn.akira.provider.KitsuProvider
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Constants {
    val api = Retrofit.Builder()
        .baseUrl("https://kitsu.io/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(KitsuProvider::class.java)
}