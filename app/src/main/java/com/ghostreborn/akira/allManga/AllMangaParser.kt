package com.ghostreborn.akira.allManga

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
}