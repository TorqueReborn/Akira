package com.ghostreborn.akira.models.retro


import com.google.gson.annotations.SerializedName

data class EntryRequest(
    @SerializedName("data")
    val `data`: Data
) {
    data class Data(
        @SerializedName("attributes")
        val attributes: Attributes,
        @SerializedName("relationships")
        val relationships: Relationships,
        @SerializedName("type")
        val type: String
    ) {
        data class Attributes(
            @SerializedName("progress")
            val progress: Int,
            @SerializedName("status")
            val status: String
        )

        data class Relationships(
            @SerializedName("anime")
            val anime: Anime,
            @SerializedName("user")
            val user: User
        ) {
            data class Anime(
                @SerializedName("data")
                val `data`: Data
            ) {
                data class Data(
                    @SerializedName("id")
                    val id: String,
                    @SerializedName("type")
                    val type: String
                )
            }

            data class User(
                @SerializedName("data")
                val `data`: Data
            ) {
                data class Data(
                    @SerializedName("id")
                    val id: String,
                    @SerializedName("type")
                    val type: String
                )
            }
        }
    }
}