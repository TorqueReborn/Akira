package com.ghostreborn.akirareborn.allanime

import okhttp3.OkHttpClient
import okhttp3.Request

class AllAnimeNetwork {

    private fun connectAllAnime(
        variables: String,
        queryTypes: String,
        query: String
    ): String? {
        val client = OkHttpClient()
        val url =
            "https://api.allanime.day/api?variables={" + variables + "}&query=query(" + queryTypes + "){" + query + "}"
        val request = Request.Builder()
            .url(url)
            .header("Referer", "https://allanime.to")
            .header("Cipher", "AES256-SHA256")
            .header(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/121.0"
            )
            .build()
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
        return responseBody
    }

    fun queryPopular():String{
        val variables =
            "\"size\":30,\"type\":\"anime\",\"dateRange\":1,\"page\":1,\"allowAdult\":true,\"allowUnknown\":true"
        val queryTypes =
            "\$size:Int!,\$type:VaildPopularTypeEnumType!,\$dateRange:Int!,\$page:Int!,\$allowAdult:Boolean!,\$allowUnknown:Boolean!"
        val query =
            "queryPopular(type:\$type,size:\$size,dateRange:\$dateRange,page:\$page,allowAdult:\$allowAdult,allowUnknown:\$allowUnknown){total,recommendations{anyCard{_id,name,englishName,thumbnail}}}"
        return connectAllAnime(variables, queryTypes, query)!!
    }

    fun episodeDetails(id: String, episode: String): String? {
        val variables = "\"showId\":\"$id\",\"episode\":\"$episode\",\"translationType\":\"sub\""
        val queryTypes =
            "\$showId:String!,\$episode:String!,\$translationType:VaildTranslationTypeEnumType!"
        val query =
            "episode(showId:\$showId,episodeString:\$episode,translationType:\$translationType){" +
                    "episodeString," +
                    "episodeInfo{notes,thumbnails}" +
                    "}"
        return connectAllAnime(variables, queryTypes, query)
    }

}