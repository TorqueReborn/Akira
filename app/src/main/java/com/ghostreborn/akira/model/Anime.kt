package com.ghostreborn.akira.model

data class AnimeItem(
    val id: String,
    val thumbnail: String
)

data class Anime(
    val animeSeason: String,
    val animeList: List<AnimeItem>
)