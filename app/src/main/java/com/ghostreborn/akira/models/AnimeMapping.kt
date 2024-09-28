package com.ghostreborn.akira.models


import com.google.gson.annotations.SerializedName

data class AnimeMapping(
    @SerializedName("data")
    val `data`: List<Data>
) {
    data class Data(
        @SerializedName("attributes")
        val attributes: Attributes
    ) {
        data class Attributes(
            @SerializedName("externalId")
            val externalId: String
        )
    }
}