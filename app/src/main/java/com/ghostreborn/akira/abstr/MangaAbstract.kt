package com.ghostreborn.akira.abstr

import com.ghostreborn.akira.model.Manga

abstract class MangaAbstract {
    abstract fun recent(): ArrayList<Manga>
    abstract fun search(manga: String): ArrayList<Manga>
    abstract fun chapters(id: String): ArrayList<ArrayList<String>>
    abstract fun pages(id: String, chapter: String): ArrayList<String>
}