package com.ghostreborn.kitsumodified.models


import com.google.gson.annotations.SerializedName

data class Entry(
    @SerializedName("data")
    val `data`: List<Data>,
    @SerializedName("included")
    val included: List<Included>
) {
    data class Data(
        @SerializedName("attributes")
        val attributes: Attributes,
        @SerializedName("id")
        val id: String
    ) {
        data class Attributes(
            @SerializedName("progress")
            val progress: Int,
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
                val original: String
            )

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
}