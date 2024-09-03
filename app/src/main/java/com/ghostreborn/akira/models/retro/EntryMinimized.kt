package com.ghostreborn.akira.models.retro


import com.google.gson.annotations.SerializedName

data class EntryMinimized(
    @SerializedName("data")
    val `data`: List<Data>,
    @SerializedName("meta")
    val meta: Meta
) {
    data class Data(
        @SerializedName("id")
        val id: String
    )

    data class Meta(
        @SerializedName("count")
        val count: Int
    )
}