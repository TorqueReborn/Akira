package com.ghostreborn.kitsumodified.models


import com.google.gson.annotations.SerializedName

data class EntryNum(
    @SerializedName("meta")
    val meta: Meta
) {
    data class Meta(
        @SerializedName("count")
        val count: Int
    )
}