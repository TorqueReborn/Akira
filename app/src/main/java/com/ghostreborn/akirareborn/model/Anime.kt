package com.ghostreborn.akirareborn.model

data class Anime(
    var id: String,
    val name: String,
    val englishName: String,
    val thumbnail: String,
    val episodes: ArrayList<String>
)
