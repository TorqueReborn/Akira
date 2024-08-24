package com.ghostreborn.akira.abstr

import com.ghostreborn.akira.model.Anime

abstract class AnimeAbstract {
    abstract fun recent(): ArrayList<Anime>
    abstract fun search(anime: String): ArrayList<Anime>
    abstract fun episodes(id: String): ArrayList<ArrayList<String>>
    abstract fun servers(id: String,episode:String): ArrayList<String>
}