package com.ghostreborn.akira.allManga

import android.util.Log
import androidx.core.text.HtmlCompat
import com.ghostreborn.akira.model.Anime
import com.ghostreborn.akira.model.AnimeDetails
import org.json.JSONObject

class AllMangaParser {
    fun searchManga(manga: String): ArrayList<Anime> {
        return ArrayList<Anime>().apply {
            val edgesArray = JSONObject(AllMangaNetwork().searchManga(manga).toString())
                .getJSONObject("data")
                .getJSONObject("mangas")
                .getJSONArray("edges")
            for (i in 0 until edgesArray.length()) {
                edgesArray.getJSONObject(i).let {
                    var thumbnail = it.getString("thumbnail")
                    if (!thumbnail.startsWith("http")) {
                        thumbnail =
                            "https://wp.youtube-anime.com/aln.youtube-anime.com/${it.getString("thumbnail")}"
                    }
                    add(Anime(it.getString("_id"), it.getString("name"), thumbnail.toString()))
                }
            }
        }
    }

    fun mangaDetails(mangaId: String): AnimeDetails {
        val manga = JSONObject(AllMangaNetwork().mangaDetails(mangaId).toString())
            .getJSONObject("data")
            .getJSONObject("manga")

        val description =
            HtmlCompat.fromHtml(manga.getString("description"), HtmlCompat.FROM_HTML_MODE_COMPACT)
                .toString()
        val relatedShows = manga.getJSONArray("relatedMangas")

        var prequel = ""
        var sequel = ""

        var thumbnail = manga.getString("thumbnail")
        if (!thumbnail.startsWith("http")) {
            thumbnail =
                "https://wp.youtube-anime.com/aln.youtube-anime.com/${manga.getString("thumbnail")}"
        }

        for (i in 0 until relatedShows.length()) {
            relatedShows.getJSONObject(i).apply {
                when (getString("relation")) {
                    "prequel" -> prequel = getString("mangaId")
                    "sequel" -> sequel = getString("mangaId")
                }
            }
        }

        return AnimeDetails(
            name = manga.getString("name"),
            thumbnail = thumbnail,
            description = description,
            banner = manga.getString("banner"),
            prequel = prequel,
            sequel = sequel
        )
    }

    fun mangaChapters(mangaId: String): ArrayList<ArrayList<String>> {
        val episodesArray = JSONObject(AllMangaNetwork().mangaChapters(mangaId).toString())
            .getJSONObject("data")
            .getJSONObject("manga")
            .getJSONObject("availableChaptersDetail")
            .getJSONArray("sub")

        val episodeList = ArrayList<String>().apply {
            for (i in episodesArray.length() - 1 downTo 0) {
                add(episodesArray.getString(i))
            }
        }

        return groupChapters(episodeList)
    }

    private fun groupChapters(chaptersList: ArrayList<String>): ArrayList<ArrayList<String>> {
        val group = ArrayList<ArrayList<String>>()
        var startIndex = 0
        while (startIndex < chaptersList.size) {
            val endIndex = (startIndex + 15).coerceAtMost(chaptersList.size)
            group.add(ArrayList(chaptersList.subList(startIndex, endIndex)))
            startIndex = endIndex
        }
        return group
    }

    fun chapterPages(mangaId: String, chapter: String): ArrayList<String> {
        val edges = JSONObject(AllMangaNetwork().chapterPages(mangaId, chapter).toString())
            .getJSONObject("data")
            .getJSONObject("chapterPages")
            .getJSONArray("edges")
        val pictureUrls = edges.getJSONObject(0).getJSONArray("pictureUrls")
        val thumbnails: ArrayList<String> = ArrayList()
        for (i in 0 until pictureUrls.length()) {
            val thumbnail =
                "https://ytimgf.youtube-anime.com/${pictureUrls.getJSONObject(i).getString("url")}"
            thumbnails.add(thumbnail)
        }
        return thumbnails
    }

    fun anilistIdWithAllMangaID(id: String): String {
        val raw = JSONObject(AllMangaNetwork().anilistIdWithAllMangaID(id))
            .getJSONObject("data")

        Log.e("TAG", raw.toString())
        Log.e("TAG", "id: $id")

        if (raw.isNull("manga")) {
            return "null"
        }
        return raw.getJSONObject("manga").getString("aniListId")
    }

    fun allAnimeIdWithMalId(anime: String, malId: String): String {
        val rawJSON = AllMangaNetwork().allAnimeIdWithMalId(anime).toString()
        val edgesArray = JSONObject(rawJSON)
            .getJSONObject("data")
            .getJSONObject("mangas")
            .getJSONArray("edges")

        return (0 until edgesArray.length()).firstNotNullOfOrNull { index ->
            val edge = edgesArray.getJSONObject(index)
            if (edge.getString("malId") == malId) edge.getString("_id") else null
        } ?: ""
    }

    fun getDetailsByIds(ids: String): ArrayList<Anime> {
        val show = JSONObject(AllMangaNetwork().getDetailsByIds(ids).toString())
            .getJSONObject("data")

        if (show.isNull("mangasWithIds")) {
            return ArrayList()
        }

        val shows = show.getJSONArray("mangasWithIds")

        return ArrayList<Anime>().apply {
            for (i in 0 until shows.length()) {
                shows.getJSONObject(i).apply {
                    add(Anime(getString("_id"), getString("name"), getString("thumbnail")))
                }
            }
        }
    }
}