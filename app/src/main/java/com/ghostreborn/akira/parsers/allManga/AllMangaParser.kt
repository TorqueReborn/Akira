package com.ghostreborn.akira.parsers.allManga

import com.ghostreborn.akira.model.Manga
import org.json.JSONObject

class AllMangaParser {

    fun searchManga(manga: String): ArrayList<Manga> {
        return ArrayList<Manga>().apply {
            val edgesArray = JSONObject(AllMangaNetwork().search(manga))
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
                    add(Manga(it.getString("_id"), it.getString("name"), thumbnail.toString()))
                }
            }
        }
    }

    fun mangaChapters(mangaId: String): ArrayList<ArrayList<String>> {
        val episodesArray = JSONObject(AllMangaNetwork().chapters(mangaId))
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
        val edges = JSONObject(AllMangaNetwork().pages(mangaId, chapter))
            .getJSONObject("data")
            .getJSONObject("chapterPages")
            .getJSONArray("edges")
        val pictureUrls = edges.getJSONObject(0).getJSONArray("pictureUrls")
        val thumbnails: ArrayList<String> = ArrayList()
        for (i in 0 until pictureUrls.length()) {
            var thumbnail =
                "https://ytimgf.youtube-anime.com/${pictureUrls.getJSONObject(i).getString("url")}"
            if (!pictureUrls.getJSONObject(i).getString("url").contains("images")) {
                thumbnail =
                    "https://ytimgf.youtube-anime.com/images/${
                        pictureUrls.getJSONObject(i).getString("url")
                    }"
            }
            thumbnails.add(thumbnail)
        }
        return thumbnails
    }

}