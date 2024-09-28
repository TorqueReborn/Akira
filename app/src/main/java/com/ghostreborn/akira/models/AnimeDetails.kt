package com.ghostreborn.akira.models

data class AnimeDetails(
    val title: String,
    val description: String,
    val studio: String,
    val lastEpisode: String,
    val airingAt: String,
    val timeLeft: String,
    val prequel: String,
    val sequel: String,
    val thumbnail: String
)