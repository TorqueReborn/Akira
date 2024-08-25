package com.ghostreborn.akira.parsers.gojo

import com.ghostreborn.akira.model.Anime
import com.ghostreborn.akira.model.Episode
import com.ghostreborn.akira.model.Server
import org.json.JSONArray
import org.json.JSONObject

class GojoParser {
    fun search(anime: String): ArrayList<Anime> {
        val search = GojoNetwork().search(anime)
        val mediaArray = JSONObject(search)
            .getJSONObject("data")
            .getJSONObject("Page")
            .getJSONArray("media")
        val animes = ArrayList<Anime>()
        for (i in 0 until mediaArray.length()) {
            val media = mediaArray.getJSONObject(i)
            val id = media.getString("id")
            val title = media.getJSONObject("title")
                .getString("userPreferred")
            val image = media.getJSONObject("coverImage")
                .getString("medium")
            animes.add(Anime(id, title, image))
        }
        return animes
    }

    fun recentlyUpdated(): ArrayList<Anime> {
        val recentlyUpdated = GojoNetwork().recentlyUpdated()
        val mediaArray = JSONObject(recentlyUpdated)
            .getJSONObject("data")
            .getJSONObject("Page")
            .getJSONArray("media")
        val animes = ArrayList<Anime>()
        for (i in 0 until mediaArray.length()) {
            val media = mediaArray.getJSONObject(i)
            val id = media.getString("id")
            val title = media.getJSONObject("title")
                .getString("userPreferred")
            val image = media.getJSONObject("coverImage")
                .getString("medium")
            animes.add(Anime(id, title, image))
        }
        return animes
    }

    fun episodes(id: String): ArrayList<ArrayList<Episode>> {
        val episodesArray = JSONArray(GojoNetwork().episodes(id))
        val episodes = episodesArray.getJSONObject(0)
            .getJSONArray("episodes")
        val ep = ArrayList<Episode>()
        for (i in 0 until episodes.length()) {
            val episode = episodes.getJSONObject(i)
            val epId = episode.getString("id")
            val epTitle = episode.getString("title")
            val epNum = episode.getString("number")
            val isFiller = episode.getBoolean("isFiller")
            ep.add(Episode(epId, epTitle, epNum, isFiller))
        }
        return groupEpisodes(ep)
    }

    private fun groupEpisodes(episodeList: ArrayList<Episode>): ArrayList<ArrayList<Episode>> {
        val group = ArrayList<ArrayList<Episode>>()
        var startIndex = 0
        while (startIndex < episodeList.size) {
            val endIndex = (startIndex + 15).coerceAtMost(episodeList.size)
            group.add(ArrayList(episodeList.subList(startIndex, endIndex)))
            startIndex = endIndex
        }
        return group
    }

    fun servers(id: String, episode: String, watchId: String): ArrayList<Server> {
        val serversArray = JSONObject(GojoNetwork().servers(id, episode, watchId))
            .getJSONArray("sources")
        val servers = ArrayList<Server>()
        for (i in 0 until serversArray.length()) {
            val server = serversArray.getJSONObject(i)
            val quality = server.getString("quality")
            val url = server.getString("url")
            servers.add(Server(quality, url))
        }
        return servers
    }
}