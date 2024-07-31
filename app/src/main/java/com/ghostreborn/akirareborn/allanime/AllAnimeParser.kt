package com.ghostreborn.akirareborn.allanime

import android.util.Log
import androidx.core.text.HtmlCompat
import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.model.Anime
import com.ghostreborn.akirareborn.model.AnimeDetails
import com.ghostreborn.akirareborn.model.Episode
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


class AllAnimeParser {

    fun searchAnime(anime: String) {
        Constants.animeList = ArrayList()
        val edgesArray = JSONObject(AllAnimeNetwork().searchAnime(anime).toString())
            .getJSONObject("data")
            .getJSONObject("shows")
            .getJSONArray("edges")
        for (i in 0 until edgesArray.length()) {
            val edge = edgesArray.getJSONObject(i)
            val id = edge.getString("_id")
            val name = edge.getString("name")
            val thumbnail = edge.getString("thumbnail")
            Constants.animeList.add(Anime(id, name, thumbnail))
        }
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

    fun animeDetails(animeId: String) {
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
        Constants.animeDetails = AnimeDetails(name, thumbnail, description, banner, prequel, sequel)
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
        val episodeDetails = JSONObject(rawJSON)
            .getJSONObject("data")
            .getJSONObject("episode")
        val episodeNumber = episodeDetails.getString("episodeString")
        if (episodeDetails.isNull("episodeInfo")) {
            return Episode(episodeNumber, "Episode ${episodeNumber}", Constants.anime.thumbnail)
        }
        var episodeName = episodeDetails.getJSONObject("episodeInfo").getString("notes")
        if (episodeName=="null") {
            episodeName = "Episode ${episodeNumber}"
        }
        var episodeThumbnail = Constants.animeThumbnail
        if(!episodeDetails.getJSONObject("episodeInfo").isNull("thumbnails")){
            episodeThumbnail = "https://wp.youtube-anime.com/aln.youtube-anime.com" +
                    episodeDetails.getJSONObject("episodeInfo").getJSONArray("thumbnails")[0]
        }
        return Episode(episodeNumber, episodeName, episodeThumbnail)
    }

    fun episodeDetails(id: String, episodes: ArrayList<String>) {
        Constants.parsedEpisodes = ArrayList()
        for (episode in episodes) {
            val episodeDetail = episodeDetail(id, episode)
            Constants.parsedEpisodes.add(episodeDetail)
        }
    }

    private fun episodeUrls(id: String, episode: String): ArrayList<String> {
        val rawJSON = AllAnimeNetwork().episodeUrls(id, episode).toString()
        val sources = ArrayList<String>()
        val sourceUrls = JSONObject(rawJSON)
            .getJSONObject("data")
            .getJSONObject("episode")
            .getJSONArray("sourceUrls")
        for (i in 0 until sourceUrls.length()) {
            val sourceUrl = sourceUrls.getJSONObject(i).getString("sourceUrl")
            if (sourceUrl.contains("--")) {
                val decrypted: String =
                    decryptAllAnimeServer(sourceUrl.substring(2)).replace("clock", "clock.json")
                if (!decrypted.contains("fast4speed")) {
                    sources.add("https://allanime.day$decrypted")
                }
            }
        }
        return sources
    }

    fun getSourceUrls(id: String?, episode: String?) {
        val sources: ArrayList<String> = episodeUrls(id!!, episode!!)
        Constants.episodeUrls = ArrayList()

        for (source in sources) {
            try {
                val rawJSON: String = getJSON(source)
                if (rawJSON == "error") {
                    continue
                }
                val linksArray = JSONObject(rawJSON).getJSONArray("links")
                for (j in 0 until linksArray.length()) {
                    Constants.episodeUrls.add(linksArray.getJSONObject(j).getString("link"))
                }
            } catch (e: JSONException) {
                Log.e("TAG", "Error parsing JSON: ", e)
            }
        }
    }

    private fun getJSON(url: String?): String {
        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(url!!)
            .header("Referer", "https://allanime.to")
            .header("Cipher", "AES256-SHA256")
            .header(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/121.0"
            )
            .build()

        try {
            client.newCall(request).execute().use { response ->
                return if (response.body != null) response.body!!
                    .string() else "NULL"
            }
        } catch (e: IOException) {
            Log.e("TAG", "Error fetching JSON: ", e)
        }
        return "{}"
    }

    private fun decryptAllAnimeServer(decrypt: String): String {
        val decryptedString = StringBuilder()
        var i = 0
        while (i < decrypt.length) {
            val dec = decrypt.substring(i, i + 2).toInt(16)
            decryptedString.append((dec xor 56).toChar())
            i += 2
        }
        return decryptedString.toString()
    }

}