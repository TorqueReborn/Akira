package com.ghostreborn.akira.abstr

import com.ghostreborn.akira.model.Anime

abstract class AnimeAbstract {
    abstract fun recent(): ArrayList<Anime>
    abstract fun search(anime:String): ArrayList<Anime>
}