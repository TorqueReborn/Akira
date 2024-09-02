package com.ghostreborn.akira.models.retro


import com.google.gson.annotations.SerializedName

data class Search(
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
            @SerializedName("coverImage")
            val coverImage: CoverImage,
            @SerializedName("posterImage")
            val posterImage: PosterImage
        ) {
            data class CoverImage(
                @SerializedName("large")
                val large: String,
                @SerializedName("original")
                val original: String,
                @SerializedName("small")
                val small: String,
                @SerializedName("tiny")
                val tiny: String
            )

            data class PosterImage(
                @SerializedName("large")
                val large: String,
                @SerializedName("medium")
                val medium: String,
                @SerializedName("original")
                val original: String,
                @SerializedName("small")
                val small: String,
                @SerializedName("tiny")
                val tiny: String
            )
        }
    }
}