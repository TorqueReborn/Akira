package com.ghostreborn.akira.abstr

import com.ghostreborn.akira.model.Anime
import com.ghostreborn.akira.model.Episode
import com.ghostreborn.akira.model.Server

abstract class AnimeAbstract {
    abstract fun recent(): ArrayList<Anime>
    abstract fun search(anime: String): ArrayList<Anime>
    abstract fun episodes(id: String): ArrayList<ArrayList<Episode>>
    abstract fun servers(id: String,episode:String): ArrayList<Server>
}