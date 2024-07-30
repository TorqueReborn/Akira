package com.ghostreborn.akirareborn.allanime

import android.util.Log
import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.model.Anime
import com.ghostreborn.akirareborn.model.Episode
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


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
        if (episodeDetails.isNull("episodeInfo")) {
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