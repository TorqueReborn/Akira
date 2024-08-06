package com.ghostreborn.akira.gojo

import com.ghostreborn.akira.model.Anime
import org.json.JSONArray
import org.json.JSONObject

class GojoParser {
    fun recentUpdates(): ArrayList<Anime>{
        val anime = ArrayList<Anime>()
        val recentUpdates = JSONArray(GojoNetwork().recentUpdates())
        for (i in 0 until recentUpdates.length()){
            recentUpdates.getJSONObject(i).apply {
                anime.add(Anime(getString("id"), getJSONObject("title").getString("english"), getString("coverImage")))
            }
        }
        return anime
    }

    fun episodeId(id: String, episode: String): String{
        val episodesArray = JSONArray(GojoNetwork().episodeId(id))
            .getJSONObject(0)
            .getJSONArray("episodes")
        for (i in 0 until episodesArray.length()){
            val episodeObject = episodesArray.getJSONObject(i)
            val number = episodeObject.getString("number")
            if (number == episode){
                return episodeObject.getString("id")
            }
        }
        return ""
    }

    fun server(id:String, episodeId: String): ArrayList<String>{
        val sourcesArray = JSONObject(GojoNetwork().server(id, episodeId).toString())
            .getJSONArray("sources")
        val servers = ArrayList<String>()
        for (i in 0 until sourcesArray.length()){
            val sourceObject = sourcesArray.getJSONObject(i)
            val url = sourceObject.getString("url")
            servers.add(url)
        }
        return servers
    }
}