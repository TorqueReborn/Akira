package com.ghostreborn.akirareborn.allAnime

import android.util.Log
import androidx.core.text.HtmlCompat
import com.ghostreborn.akirareborn.fragment.AnimeFragment
import com.ghostreborn.akirareborn.model.Anime
import com.ghostreborn.akirareborn.model.AnimeDetails
import com.ghostreborn.akirareborn.model.Episode
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

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
        val tempThumbnail = AnimeFragment.animeThumbnail
        if (episodeDetails.isNull("episodeInfo")) {
            return Episode(episodeNumber, "Episode ${episodeNumber}", tempThumbnail)
        }
        var episodeName = episodeDetails.getJSONObject("episodeInfo").getString("notes")
        if (episodeName == "null") {
            episodeName = "Episode ${episodeNumber}"
        }
        var episodeThumbnail = tempThumbnail
        if (!episodeDetails.getJSONObject("episodeInfo").isNull("thumbnails")) {
            episodeThumbnail = "https://wp.youtube-anime.com/aln.youtube-anime.com" +
                    episodeDetails.getJSONObject("episodeInfo").getJSONArray("thumbnails")[0]
        }
        return Episode(episodeNumber, episodeName, episodeThumbnail)
    }

    fun getDetailsByIds(ids: String): ArrayList<Anime> {
        val rawJSON = AllAnimeNetwork().getDetailsByIds(ids)
        val animes: ArrayList<Anime> = ArrayList()
        val shows = JSONObject(rawJSON.toString())
            .getJSONObject("data")
            .getJSONArray("showsWithIds")
        for (i in 0 until shows.length()) {
            val show = shows.getJSONObject(i)
            val id = show.getString("_id")
            val name = show.getString("name")
            val thumbnail = show.getString("thumbnail")
            animes.add(Anime(id, name, thumbnail))
        }
        return animes
    }

    fun episodeDetails(id: String, episodes: ArrayList<String>): ArrayList<Episode> {
        val parsed: ArrayList<Episode> = ArrayList()
        for (episode in episodes) {
            val episodeDetail = episodeDetail(id, episode)
            parsed.add(episodeDetail)
        }
        return parsed
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

    fun getSourceUrls(id: String?, episode: String?): ArrayList<String> {
        val sources: ArrayList<String> = episodeUrls(id!!, episode!!)
        val episodeUrls: ArrayList<String> = ArrayList()

        for (source in sources) {
            try {
                val rawJSON: String = getJSON(source)
                if (rawJSON == "error") {
                    continue
                }
                val linksArray = JSONObject(rawJSON).getJSONArray("links")
                for (j in 0 until linksArray.length()) {
                    episodeUrls.add(linksArray.getJSONObject(j).getString("link"))
                }
            } catch (e: JSONException) {
                Log.e("TAG", "Error parsing JSON: ", e)
            }
        }
        return episodeUrls
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

    fun anilistWithAllAnimeID(allAnimeId: String): String {
        val rawJSON = AllAnimeNetwork().anilistIdWithAllAnimeID(allAnimeId).toString()
        val show = JSONObject(rawJSON)
            .getJSONObject("data")
            .getJSONObject("show")
        val malId = show.getString("aniListId")
        return malId
    }

    fun allAnimeIdWithMalId(anime: String, malId: String): String {
        val rawJSON = AllAnimeNetwork().allAnimeIdWithMalId(anime).toString()
        val edgesArray = JSONObject(rawJSON)
            .getJSONObject("data")
            .getJSONObject("shows")
            .getJSONArray("edges")
        for (i in 0 until edgesArray.length()) {
            val edge = edgesArray.getJSONObject(i)
            if (edge.getString("malId") == malId) {
                return edge.getString("_id")
            }
        }
        return ""
    }
}