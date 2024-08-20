package com.ghostreborn.akira.allAnime

import com.ghostreborn.akira.Constants
import okhttp3.OkHttpClient
import okhttp3.Request

class AllAnimeNetwork {
    private fun connectAllAnime(
        variables: String,
        queryTypes: String,
        query: String
    ): String? {
        val client = OkHttpClient()
        val url = "https://api.allanime.day/api?variables={$variables}&query=query($queryTypes){$query}"
        val request = Request.Builder()
            .url(url)
            .header("Referer", "https://allanime.to")
            .header("Cipher", "AES256-SHA256")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/121.0")
            .build()
        return client.newCall(request).execute().body?.string()
    }

    fun getSubDub(isDubEnabled: Boolean): String{
        return if (isDubEnabled) "dub" else "sub"
    }

    fun searchAnime(anime: String): String? {
        val allowAdult = false
        val allowUnknown = false
        val subDub = "sub"
        val variables = "\"search\":{\"allowAdult\":$allowAdult,\"allowUnknown\":$allowUnknown,\"query\":\"$anime\"},\"limit\":39,\"page\":1,\"translationType\":\"$subDub\",\"countryOrigin\":\"ALL\""
        val queryTypes = "\$search:SearchInput,\$limit:Int,\$page:Int,\$translationType:VaildTranslationTypeEnumType,\$countryOrigin:VaildCountryOriginEnumType"
        val query = "shows(search:\$search,limit:\$limit,page:\$page,translationType:\$translationType,countryOrigin:\$countryOrigin){edges{_id,name,thumbnail}}"
        return connectAllAnime(variables, queryTypes, query)
    }

    fun animeDetails(id: String): String? {
        val variables = "\"showId\":\"$id\""
        val queryTypes = "\$showId:String!"
        val query = "show(_id:\$showId){name,thumbnail,description,banner,relatedShows}"
        return connectAllAnime(variables, queryTypes, query)
    }

    fun episodes(id: String): String? {
        val variables = "\"showId\":\"$id\""
        val queryTypes = "\$showId:String!"
        val query = "show(_id:\$showId){availableEpisodesDetail}"
        return connectAllAnime(variables, queryTypes, query)
    }

    fun episodeUrls(id: String, episode: String): String? {
        val subDub = getSubDub(Constants.preferences.getBoolean(Constants.PREF_DUB_ENABLED, false))
        val variables = "\"showId\":\"$id\",\"episode\":\"$episode\",\"translationType\":\"$subDub\""
        val queryTypes = "\$showId:String!,\$episode:String!,\$translationType:VaildTranslationTypeEnumType!"
        val query = "episode(showId:\$showId,episodeString:\$episode,translationType:\$translationType){sourceUrls}"
        return connectAllAnime(variables, queryTypes, query)
    }

    fun episodeDetails(id: String, episode: String): String? {
        val subDub = getSubDub(Constants.preferences.getBoolean(Constants.PREF_DUB_ENABLED, false))
        val variables = "\"showId\":\"$id\",\"episode\":\"$episode\",\"translationType\":\"$subDub\""
        val queryTypes = "\$showId:String!,\$episode:String!,\$translationType:VaildTranslationTypeEnumType!"
        val query = "episode(showId:\$showId,episodeString:\$episode,translationType:\$translationType){episodeString,episodeInfo{notes,thumbnails}}"
        return connectAllAnime(variables, queryTypes, query)
    }

    fun getDetailsByIds(ids: String): String? {
        val variables = "\"ids\":[$ids]"
        val queryTypes = "\$ids:[String!]!"
        val query = "showsWithIds(ids:\$ids){_id,name,thumbnail}"
        return connectAllAnime(variables, queryTypes, query)
    }

    fun allAnimeIdWithMalId(anime: String): String? {
        val allowAdult = Constants.preferences.getBoolean(Constants.PREF_ALLOW_ADULT, false)
        val allowUnknown = Constants.preferences.getBoolean(Constants.PREF_ALLOW_UNKNOWN, false)
        val subDub = getSubDub(Constants.preferences.getBoolean(Constants.PREF_DUB_ENABLED, false))
        val variables = "\"search\":{\"allowAdult\":$allowAdult,\"allowUnknown\":$allowUnknown,\"query\":\"$anime\"},\"limit\":39,\"page\":1,\"translationType\":\"$subDub\",\"countryOrigin\":\"ALL\""
        val queryTypes = "\$search:SearchInput,\$limit:Int,\$page:Int,\$translationType:VaildTranslationTypeEnumType,\$countryOrigin:VaildCountryOriginEnumType"
        val query = "shows(search:\$search,limit:\$limit,page:\$page,translationType:\$translationType,countryOrigin:\$countryOrigin){edges{_id,malId}}"
        return connectAllAnime(variables, queryTypes, query)
    }

    fun anilistIdWithAllAnimeID(id: String): String? {
        val variables = "\"showId\":\"$id\""
        val queryTypes = "\$showId:String!"
        val query = "show(_id:\$showId){aniListId}"
        return connectAllAnime(variables, queryTypes, query)
    }

}