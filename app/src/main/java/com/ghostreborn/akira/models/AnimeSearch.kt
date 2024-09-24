package com.ghostreborn.akira.models.kitsu


import com.google.gson.annotations.SerializedName

data class AnimeSearch(
    @SerializedName("data")
    val `data`: List<Data>
) {
    data class Data(
        @SerializedName("attributes")
        val attributes: Attributes,
        @SerializedName("id")
        val id: String,
        @SerializedName("type")
        val type: String
    ) {
        data class Attributes(
            @SerializedName("canonicalTitle")
            val canonicalTitle: String,
            @SerializedName("episodeCount")
            val episodeCount: String?,
            @SerializedName("posterImage")
            val posterImage: PosterImage
        ) {
            data class PosterImage(
                @SerializedName("large")
                val large: String,
                @SerializedName("medium")
                val medium: String
            )
        }
    }
}