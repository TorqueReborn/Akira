package com.ghostreborn.akira.models


import com.google.gson.annotations.SerializedName
import com.ghostreborn.akira.models.GojoEpisode.GojoEpisodeItem

class GojoEpisode : ArrayList<GojoEpisodeItem>(){
    data class GojoEpisodeItem(
        @SerializedName("default")
        val default: Boolean,
        @SerializedName("episodes")
        val episodes: List<Episode>,
        @SerializedName("hasDub")
        val hasDub: Boolean,
        @SerializedName("providerId")
        val providerId: String
    ) {
        data class Episode(
            @SerializedName("hasDub")
            val hasDub: Boolean,
            @SerializedName("id")
            val id: String,
            @SerializedName("isFiller")
            val isFiller: Boolean,
            @SerializedName("number")
            val number: Int,
            @SerializedName("title")
            val title: String
        )
    }
}