package com.ghostreborn.akira.allAnime

import android.util.Log
import androidx.core.text.HtmlCompat
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.model.Anime
import com.ghostreborn.akira.model.AnimeDetails
import com.ghostreborn.akira.model.Episode
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class AllAnimeParser {
    fun searchAnime(anime: String): ArrayList<Anime> {
        return ArrayList<Anime>().apply {
            val edgesArray = JSONObject(AllAnimeNetwork().searchAnime(anime))
                .getJSONObject("data")
                .getJSONObject("shows")
                .getJSONArray("edges")
            for (i in 0 until edgesArray.length()) {
                edgesArray.getJSONObject(i).let {
                    add(Anime(it.getString("_id"), it.getString("name"), it.getString("thumbnail")))
                }
            }
        }
    }

    fun animeDetails(animeId: String): AnimeDetails {
        val show = JSONObject(AllAnimeNetwork().animeDetails(animeId))
            .getJSONObject("data")
            .getJSONObject("show")
        val description =
            HtmlCompat.fromHtml(show.getString("description"), HtmlCompat.FROM_HTML_MODE_COMPACT)
                .toString()
        val relatedShows = show.getJSONArray("relatedShows")
        var prequel = ""
        var sequel = ""
        for (i in 0 until relatedShows.length()) {
            relatedShows.getJSONObject(i).apply {
                when (getString("relation")) {
                    "prequel" -> prequel = getString("showId")
                    "sequel" -> sequel = getString("showId")
                }
            }
        }
        return AnimeDetails(
            name = show.getString("name"),
            thumbnail = show.getString("thumbnail"),
            description = description,
            banner = show.getString("banner"),
            prequel = prequel,
            sequel = sequel
        )
    }

    fun episodes(id: String): ArrayList<ArrayList<String>> {
        val episodesArray = JSONObject(AllAnimeNetwork().episodes(id))
            .getJSONObject("data")
            .getJSONObject("show")
            .getJSONObject("availableEpisodesDetail")
            .getJSONArray("sub")
        val episodeList = ArrayList<String>().apply {
            for (i in episodesArray.length() - 1 downTo 0) {
                add(episodesArray.getString(i))
            }
        }
        return groupEpisodes(episodeList)
    }

    private fun groupEpisodes(episodeList: ArrayList<String>): ArrayList<ArrayList<String>> {
        val group = ArrayList<ArrayList<String>>()
        var startIndex = 0
        while (startIndex < episodeList.size) {
            val endIndex = (startIndex + 15).coerceAtMost(episodeList.size)
            group.add(ArrayList(episodeList.subList(startIndex, endIndex)))
            startIndex = endIndex
        }
        return group
    }

    private fun episodeDetail(id: String, episode: String): Episode {
        val episodeDetails = JSONObject(AllAnimeNetwork().episodeDetails(id, episode))
            .getJSONObject("data")
            .getJSONObject("episode")
        val episodeNumber = episodeDetails.getString("episodeString")
        val tempThumbnail = Constants.animeThumbnail
        if (episodeDetails.isNull("episodeInfo")) {
            return Episode(episodeNumber, "Episode $episodeNumber", tempThumbnail)
        }
        val episodeInfo = episodeDetails.getJSONObject("episodeInfo")
        var episodeName = "Episode $episodeNumber"
        if (!episodeInfo.isNull("notes")) {
            episodeName = episodeInfo.getString("notes")
        }
        val episodeThumbnail = episodeInfo.optJSONArray("thumbnails")?.getString(0)?.let {
            "https://wp.youtube-anime.com/aln.youtube-anime.com$it"
        } ?: tempThumbnail
        return Episode(episodeNumber, episodeName, episodeThumbnail)
    }

    fun getDetailsByIds(ids: String): ArrayList<Anime> {
        val show = JSONObject(AllAnimeNetwork().getDetailsByIds(ids))
            .getJSONObject("data")
        if (show.isNull("showsWithIds")) {
            return ArrayList()
        }
        val shows = show.getJSONArray("showsWithIds")
        return ArrayList<Anime>().apply {
            for (i in 0 until shows.length()) {
                shows.getJSONObject(i).apply {
                    add(Anime(getString("_id"), getString("name"), getString("thumbnail")))
                }
            }
        }
    }

    fun episodeDetails(id: String, episodes: List<String>): List<Episode> {
        return episodes.map { episode -> episodeDetail(id, episode) }
    }

    private fun episodeUrls(id: String, episode: String): List<String> {
        val sourceUrls = JSONObject(AllAnimeNetwork().episodeUrls(id, episode))
            .getJSONObject("data")
            .getJSONObject("episode")
            .getJSONArray("sourceUrls")
        return (0 until sourceUrls.length())
            .map { sourceUrls.getJSONObject(it).getString("sourceUrl") }
            .filter { it.contains("--") }
            .map {
                val decrypted =
                    decryptAllAnimeServer(it.substring(2)).replace("clock", "clock.json")
                if (!decrypted.contains("fast4speed")) "https://allanime.day$decrypted" else ""
            }
            .filter { it.isNotEmpty() }
    }

    fun getSourceUrls(id: String?, episode: String?): List<String> {
        return episodeUrls(id!!, episode!!).flatMap { source ->
            try {
                val rawJSON = getJSON(source)
                if (rawJSON == "error" || rawJSON == "{}") return@flatMap emptyList<String>()

                val linksArray = JSONObject(rawJSON).getJSONArray("links")
                List(linksArray.length()) { linksArray.getJSONObject(it).getString("link") }
            } catch (e: JSONException) {
                Log.e("TAG", "Error parsing JSON: ", e)
                emptyList()
            }
        }
    }

    private fun getJSON(url: String?): String {
        val ur = URL(url ?: return "{}")
        val connection = ur.openConnection() as HttpURLConnection
        with(connection) {
            requestMethod = "GET"
            setRequestProperty("Referer", "https://allanime.to")
            return try {
                inputStream.bufferedReader().use { it.readText() }
            } catch (e: IOException) {
                "{}"
            } finally {
                disconnect()
            }
        }
    }

    private fun decryptAllAnimeServer(decrypt: String): String {
        return buildString {
            for (i in decrypt.indices step 2) {
                val dec = decrypt.substring(i, i + 2).toInt(16)
                append((dec xor 56).toChar())
            }
        }
    }

    fun anilistWithAllAnimeID(allAnimeId: String): String {
        val rawJSON = AllAnimeNetwork().anilistIdWithAllAnimeID(allAnimeId)
        return JSONObject(rawJSON)
            .getJSONObject("data")
            .getJSONObject("show")
            .getString("aniListId")
    }

    fun allAnimeIdWithMalId(anime: String, malId: String): String {
        val rawJSON = AllAnimeNetwork().allAnimeIdWithMalId(anime)
        val edgesArray = JSONObject(rawJSON)
            .getJSONObject("data")
            .getJSONObject("shows")
            .getJSONArray("edges")
        return (0 until edgesArray.length()).firstNotNullOfOrNull { index ->
            val edge = edgesArray.getJSONObject(index)
            if (edge.getString("malId") == malId) edge.getString("_id") else null
        } ?: ""
    }
}