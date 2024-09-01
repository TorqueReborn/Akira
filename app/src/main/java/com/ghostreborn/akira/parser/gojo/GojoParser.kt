package com.ghostreborn.akira.parser.gojo

import com.ghostreborn.akira.models.Episode
import com.ghostreborn.akira.models.Server
import org.json.JSONArray
import org.json.JSONObject

class GojoParser {
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