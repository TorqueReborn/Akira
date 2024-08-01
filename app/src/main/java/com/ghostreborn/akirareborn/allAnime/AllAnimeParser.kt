package com.ghostreborn.akirareborn.allAnime

import androidx.core.text.HtmlCompat
import com.ghostreborn.akirareborn.model.Anime
import com.ghostreborn.akirareborn.model.AnimeDetails
import com.ghostreborn.akirareborn.model.Episode
import org.json.JSONObject

class AllAnimeParser {
    fun searchAnime(anime: String): ArrayList<Anime> {
        val animeList: ArrayList<Anime> = ArrayList()
        val edgesArray = JSONObject(AllAnimeNetwork().searchAnime(anime).toString())
            .getJSONObject("data")
            .getJSONObject("shows")
            .getJSONArray("edges")
        for (i in 0 until edgesArray.length()) {
            val edge = edgesArray.getJSONObject(i)
            val id = edge.getString("_id")
            val name = edge.getString("name")
            val thumbnail = edge.getString("thumbnail")
            animeList.add(Anime(id, name, thumbnail))
        }
        return animeList
    }

    fun animeDetails(animeId: String): AnimeDetails {
        val show: JSONObject = JSONObject(AllAnimeNetwork().animeDetails(animeId).toString())
            .getJSONObject("data")
            .getJSONObject("show")
        val name = show.getString("name")
        val thumbnail = show.getString("thumbnail")
        val description =
            HtmlCompat.fromHtml(show.getString("description"), HtmlCompat.FROM_HTML_MODE_COMPACT)
                .toString()
        val banner = show.getString("banner")
        var prequel = ""
        var sequel = ""
        val relatedShows = show.getJSONArray("relatedShows")
        for (i in 0 until relatedShows.length()) {
            val relatedShow = relatedShows.getJSONObject(i)
            val relation = relatedShow.getString("relation")
            if ("prequel" == relation) prequel = relatedShow.getString("showId")
            if ("sequel" == relation) sequel = relatedShow.getString("showId")
        }
        return AnimeDetails(name, thumbnail, description, banner, prequel, sequel)
    }

    fun episodes(id: String): ArrayList<ArrayList<String>> {
        val episodeList = ArrayList<String>()
        val episodesArray = JSONObject(AllAnimeNetwork().episodes(id).toString())
            .getJSONObject("data")
            .getJSONObject("show")
            .getJSONObject("availableEpisodesDetail")
            .getJSONArray("sub")
        for (i in episodesArray.length() - 1 downTo 0) {
            episodeList.add(episodesArray.getString(i))
        }
        return groupEpisodes(episodeList)
    }

    private fun groupEpisodes(episodeList: ArrayList<String>): ArrayList<ArrayList<String>> {
        val group: ArrayList<ArrayList<String>> = ArrayList()
        var startIndex = 0
        while (startIndex < episodeList.size) {
            val endIndex = (startIndex + 15).coerceAtMost(episodeList.size)
            group.add(ArrayList(episodeList.subList(startIndex, endIndex)))
            startIndex = endIndex
        }
        return group
    }

    private fun episodeDetail(id: String, episode: String): Episode {
        val rawJSON = AllAnimeNetwork().episodeDetails(id, episode).toString()
        val episodeDetails = JSONObject(rawJSON)
            .getJSONObject("data")
            .getJSONObject("episode")
        val episodeNumber = episodeDetails.getString("episodeString")
        val tempThumbnail = "https://wp.youtube-anime.com/cdn.myanimelist.net/images/anime/1810/139965.jpg?w=250"
        if (episodeDetails.isNull("episodeInfo")) {
            return Episode(episodeNumber, "Episode ${episodeNumber}", tempThumbnail)
        }
        var episodeName = episodeDetails.getJSONObject("episodeInfo").getString("notes")
        if (episodeName=="null") {
            episodeName = "Episode ${episodeNumber}"
        }
        var episodeThumbnail = tempThumbnail
        if(!episodeDetails.getJSONObject("episodeInfo").isNull("thumbnails")){
            episodeThumbnail = "https://wp.youtube-anime.com/aln.youtube-anime.com" +
                    episodeDetails.getJSONObject("episodeInfo").getJSONArray("thumbnails")[0]
        }
        return Episode(episodeNumber, episodeName, episodeThumbnail)
    }

    fun episodeDetails(id: String, episodes: ArrayList<String>): ArrayList<Episode> {
        val parsed: ArrayList<Episode> = ArrayList()
        for (episode in episodes) {
            val episodeDetail = episodeDetail(id, episode)
            parsed.add(episodeDetail)
        }
        return parsed
    }

    fun anilistWithAllAnimeID(allAnimeId: String):String {
        val rawJSON = AllAnimeNetwork().anilistIdWithAllAnimeID(allAnimeId).toString()
        val show = JSONObject(rawJSON)
            .getJSONObject("data")
            .getJSONObject("show")
        val malId = show.getString("aniListId")
        return malId
    }

    fun allAnimeIdWithMalId(anime: String, malId: String):String {
        val rawJSON = AllAnimeNetwork().allAnimeIdWithMalId(anime).toString()
        val edgesArray = JSONObject(rawJSON)
            .getJSONObject("data")
            .getJSONObject("shows")
            .getJSONArray("edges")
        for (i in 0 until edgesArray.length()) {
            val edge = edgesArray.getJSONObject(i)
            if (edge.getString("malId") == malId){
                return edge.getString("_id")
            }
        }
        return ""
    }
}