package com.ghostreborn.akira.api

import okhttp3.OkHttpClient
import okhttp3.Request

class GojoAPI {
    fun test(): String{
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.gojo.live/episodes?id=163292")
            .build()
        return client.newCall(request).execute().body!!.string()
    }
}