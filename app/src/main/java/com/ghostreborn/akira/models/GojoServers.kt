package com.ghostreborn.akira.models


import com.google.gson.annotations.SerializedName

data class GojoServers(
    @SerializedName("sources")
    val sources: List<Source>,
    @SerializedName("thumbs")
    val thumbs: String
) {
    data class Source(
        @SerializedName("isM3U8")
        val isM3U8: Boolean,
        @SerializedName("quality")
        val quality: String,
        @SerializedName("url")
        val url: String
    )
}