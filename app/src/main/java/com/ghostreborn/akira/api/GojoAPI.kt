package com.ghostreborn.akira.api

import com.ghostreborn.akira.models.Episode
import com.ghostreborn.akira.models.GojoEpisode
import com.ghostreborn.akira.models.GojoServers
import com.ghostreborn.akira.models.Servers
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request

class GojoAPI {
    fun episode(animeID: String): ArrayList<Episode> {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.gojo.live/episodes?id=$animeID")
            .build()
        val rawJSON = client.newCall(request).execute().body?.string().toString()

        val episodes = ArrayList<Episode>()

        val gson = Gson()
        val episodeArray = gson.fromJson(rawJSON, GojoEpisode::class.java)[0].episodes
        for (episode in episodeArray) {
            episodes.add(
                Episode(
                    episode.id,
                    episode.title,
                    episode.number.toString(),
                    episode.isFiller
                )
            )
        }

        return episodes

    }

    fun server(animeID: String, episodeNum: String, episodeID: String): ArrayList<Servers> {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.gojo.live/tiddies?provider=shash&id=$animeID&num=$episodeNum&subType=sub&watchId=$episodeID")
            .build()
        val rawJSON = client.newCall(request).execute().body?.string().toString()

        val servers = ArrayList<Servers>()

        val gson = Gson()
        val jsonObject = gson.fromJson(rawJSON, GojoServers::class.java).sources
        for (server in jsonObject) {
            servers.add(Servers(server.quality, server.url))
        }

        return servers
    }
}