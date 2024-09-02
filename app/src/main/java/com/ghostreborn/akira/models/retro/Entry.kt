package com.ghostreborn.akira.models.retro


import com.google.gson.annotations.SerializedName

data class Entry(
    @SerializedName("data")
    val `data`: List<Data>,
    @SerializedName("included")
    val included: List<Included>,
    @SerializedName("meta")
    val meta: Meta
) {
    data class Data(
        @SerializedName("attributes")
        val attributes: Attributes,
        @SerializedName("id")
        val id: String
    ) {
        data class Attributes(
            @SerializedName("progress")
            val progress: String,
            @SerializedName("status")
            val status: String
        )
    }

    data class Included(
        @SerializedName("attributes")
        val attributes: Attributes,
        @SerializedName("id")
        val id: String
    ) {
        data class Attributes(
            @SerializedName("canonicalTitle")
            val canonicalTitle: String,
            @SerializedName("coverImage")
            val coverImage: Any?,
            @SerializedName("posterImage")
            val posterImage: PosterImage
        ) {
            data class PosterImage(
                @SerializedName("large")
                val large: String,
                @SerializedName("medium")
                val medium: String,
                @SerializedName("original")
                val original: String
            )
        }
    }

    data class Meta(
        @SerializedName("count")
        val count: Int
    )
}