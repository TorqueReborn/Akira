package com.ghostreborn.akira.api

abstract class AnimeAPI {
    abstract fun episode(animeID: String): ArrayList<String>
    abstract fun server(animeID: String, episodeNum: String, episodeID: String): ArrayList<String>
}