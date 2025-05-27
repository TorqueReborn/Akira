package com.ghostreborn.akira.model

data class AnimeDetails(
    val aniListId: String,
    val animeName: String,
    val animeDescription: String,
    val animeBanner: String,
    val animeThumbnail: String,
    val animePrequel: String,
    val animeSequel: String,
    val animeEpisodes: ArrayList<String>
)
