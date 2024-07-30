package com.ghostreborn.akirareborn.allanime

import android.util.Log
import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.model.Anime
import com.ghostreborn.akirareborn.model.Episode
import org.json.JSONObject

class AllAnimeParser {

    fun queryPopular() {
        Constants.animeList = ArrayList()
        val recommendationsArray = JSONObject(AllAnimeNetwork().queryPopular().toString())
            .getJSONObject("data")
            .getJSONObject("queryPopular")
            .getJSONArray("recommendations")
        for (i in 0 until recommendationsArray.length()) {
            val recommendation = recommendationsArray.getJSONObject(i)
                .getJSONObject("anyCard")
            val id = recommendation.getString("_id")
            val name = recommendation.getString("name")
            val englishName = recommendation.getString("englishName")
            val thumbnail = recommendation.getString("thumbnail")
            Constants.animeList.add(Anime(id, name, englishName, thumbnail))
        }
    }

    fun episodes(id: String) {
        val episodeList = ArrayList<String>()
        val episodesArray = JSONObject(AllAnimeNetwork().episodes(id).toString())
            .getJSONObject("data")
            .getJSONObject("show")
            .getJSONObject("availableEpisodesDetail")
            .getJSONArray("sub")
        for (i in episodesArray.length() - 1 downTo 0) {
            episodeList.add(episodesArray.getString(i))
        }
        groupEpisodes(id, episodeList)
    }

    private fun groupEpisodes(id: String, episodeList: ArrayList<String>) {
        Constants.groupedEpisodes = ArrayList()
        var startIndex = 0
        while (startIndex < episodeList.size) {
            val endIndex = (startIndex + 15).coerceAtMost(episodeList.size)
            Constants.groupedEpisodes.add(ArrayList(episodeList.subList(startIndex, endIndex)))
            startIndex = endIndex
        }
        episodeDetails(id, Constants.groupedEpisodes[0])
    }

    private fun episodeDetail(id: String, episode: String): Episode {
        val rawJSON = AllAnimeNetwork().episodeDetails(id, episode).toString()
        Log.e("TAG", rawJSON)
        val episodeDetails = JSONObject(rawJSON)
            .getJSONObject("data")
            .getJSONObject("episode")
        val episodeNumber = episodeDetails.getString("episodeString")
        if(episodeDetails.isNull("episodeInfo")){
            return Episode(episodeNumber, "Episode ${episodeNumber}", Constants.animeThumbnail)
        }
        val episodeName = episodeDetails.getJSONObject("episodeInfo").getString("notes")
        val episodeThumbnail = "https://wp.youtube-anime.com/aln.youtube-anime.com" +
                episodeDetails.getJSONObject("episodeInfo").getJSONArray("thumbnails")[0]
        return Episode(episodeNumber, episodeName, episodeThumbnail)
    }

    fun episodeDetails(id: String, episodes: ArrayList<String>) {
        Constants.parsedEpisodes = ArrayList()
        for (episode in episodes) {
            val episodeDetail = episodeDetail(id, episode)
            Constants.parsedEpisodes.add(episodeDetail)
        }
    }

}