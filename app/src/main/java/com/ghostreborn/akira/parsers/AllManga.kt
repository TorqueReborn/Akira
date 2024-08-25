package com.ghostreborn.akira.parsers

import com.ghostreborn.akira.abstr.MangaAbstract
import com.ghostreborn.akira.model.Manga
import com.ghostreborn.akira.parsers.allManga.AllMangaParser

class AllManga: MangaAbstract() {
    override fun recent(): ArrayList<Manga> {
        return AllMangaParser().searchManga("")
    }

    override fun search(manga: String): ArrayList<Manga> {
        return AllMangaParser().searchManga(manga)
    }

    override fun chapters(id: String): ArrayList<ArrayList<String>> {
        return AllMangaParser().mangaChapters(id)
    }

    override fun pages(id: String, chapter: String): ArrayList<String> {
        return AllMangaParser().chapterPages(id, chapter)
    }
}