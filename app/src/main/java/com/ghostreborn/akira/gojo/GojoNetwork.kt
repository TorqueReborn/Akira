package com.ghostreborn.akira.gojo

import okhttp3.OkHttpClient
import okhttp3.Request

class GojoNetwork {
    private fun connectGojo(query: String): String? {
        val request = Request.Builder()
            .url(query)
            .build()
        return OkHttpClient().newCall(request).execute().body?.string()
    }

    fun recentUpdates(): String? {
        return connectGojo("https://api.gojotv.xyz/recent-eps?type=anime&page=1&perPage=12")
    }

    fun episodeId(id: String): String? {
        return connectGojo("https://api.gojotv.xyz/episodes?id=$id")
    }

    fun server(id:String, episodeId: String): String?{
        return connectGojo("https://api.gojotv.xyz/tiddies?provider=shash&id=$id&subType=sub&watchId=$episodeId")
    }
}