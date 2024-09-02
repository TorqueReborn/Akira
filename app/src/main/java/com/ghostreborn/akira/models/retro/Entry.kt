package com.ghostreborn.akira.models.retro


import com.google.gson.annotations.SerializedName

data class Entry(
    @SerializedName("data")
    val `data`: List<Data>,
    @SerializedName("included")
    val included: List<Included>,
    @SerializedName("links")
    val links: Links,
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

    data class Links(
        @SerializedName("first")
        val first: String,
        @SerializedName("last")
        val last: String,
        @SerializedName("next")
        val next: String
    )

    data class Meta(
        @SerializedName("count")
        val count: Int
    )
}