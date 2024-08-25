package com.ghostreborn.akira.parsers.allAnime

import android.util.Log
import com.ghostreborn.akira.model.Anime
import com.ghostreborn.akira.model.Episode
import com.ghostreborn.akira.model.Server
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

    fun episodes(id: String): ArrayList<ArrayList<Episode>> {
        val episodesArray = JSONObject(AllAnimeNetwork().episodes(id))
            .getJSONObject("data")
            .getJSONObject("show")
            .getJSONObject("availableEpisodesDetail")
            .getJSONArray("sub")
        val episodeList = ArrayList<Episode>().apply {
            for (i in episodesArray.length() - 1 downTo 0) {
                add(Episode("","Episode ${episodesArray.getString(i)}",episodesArray.getString(i),false))
            }
        }
        return groupEpisodes(episodeList)
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

    fun servers(id: String?, episode: String?): ArrayList<Server> {
        return episodeUrls(id!!, episode!!).flatMap { source ->
            try {
                val rawJSON = getJSON(source)
                if (rawJSON == "error" || rawJSON == "{}") return@flatMap emptyList<Server>()

                val linksArray = JSONObject(rawJSON).getJSONArray("links")
                List(linksArray.length()) {
                    val linkObject = linksArray.getJSONObject(it)
                    Server(
                        quality = "Server ${it + 1}",
                        url = linkObject.getString("link")
                    )
                }
            } catch (e: JSONException) {
                Log.e("TAG", "Error parsing JSON: ", e)
                emptyList()
            }
        } as ArrayList<Server>
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
}