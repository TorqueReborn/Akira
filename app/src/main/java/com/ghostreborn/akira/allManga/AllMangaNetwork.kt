package com.ghostreborn.akira.allManga

import com.ghostreborn.akira.Constants
import okhttp3.OkHttpClient
import okhttp3.Request

class AllMangaNetwork {
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

    private fun getSubDub(isDubEnabled: Boolean): String{
        return if (isDubEnabled) "dub" else "sub"
    }

    fun searchManga(anime: String): String? {
        val allowAdult = false
        val allowUnknown = false
        val subDub = "sub"
        val variables = "\"search\":{\"allowAdult\":$allowAdult,\"allowUnknown\":$allowUnknown,\"query\":\"$anime\"},\"limit\":39,\"page\":1,\"translationType\":\"$subDub\",\"countryOrigin\":\"JP\""
        val queryTypes = "\$search:SearchInput,\$limit:Int,\$page:Int,\$translationType:VaildTranslationTypeMangaEnumType,\$countryOrigin:VaildCountryOriginEnumType"
        val query = "mangas(search:\$search,limit:\$limit,page:\$page,translationType:\$translationType,countryOrigin:\$countryOrigin){edges{_id,name,thumbnail,aniListId}}"
        return connectAllAnime(variables, queryTypes, query)
    }

    fun mangaDetails(mangaId: String): String?{
        val variables = "\"id\":\"$mangaId\""
        val queryTypes = "\$id:String!"
        val query = "manga(_id:\$id){name,thumbnail,description,banner,relatedMangas}"
        return connectAllAnime(variables, queryTypes, query)
    }

    fun mangaChapters(mangaId: String): String?{
        val variables = "\"id\":\"$mangaId\""
        val queryTypes = "\$id:String!"
        val query = "manga(_id:\$id){availableChaptersDetail}"
        return connectAllAnime(variables, queryTypes, query)
    }

    fun chapterPages(mangaId: String, chapter: String): String?{
        val variables = "\"mangaId\":\"$mangaId\",\"chapterString\":\"$chapter\",\"translationType\":\"sub\""
        val queryTypes = "\$mangaId:String!,\$chapterString:String!,\$translationType:VaildTranslationTypeMangaEnumType!"
        val query = "chapterPages(mangaId:\$mangaId,chapterString:\$chapterString,translationType:\$translationType){edges{pictureUrls}}"
        return connectAllAnime(variables, queryTypes, query)
    }

    fun anilistIdWithAllMangaID(id: String): String {
        val variables = "\"showId\":\"$id\""
        val queryTypes = "\$showId:String!"
        val query = "manga(_id:\$showId){aniListId}"
        return connectAllAnime(variables, queryTypes, query).toString()
    }

    fun allAnimeIdWithMalId(anime: String): String? {
        val allowAdult = Constants.preferences.getBoolean(Constants.PREF_ALLOW_ADULT, false)
        val allowUnknown = Constants.preferences.getBoolean(Constants.PREF_ALLOW_UNKNOWN, false)
        val subDub = getSubDub(Constants.preferences.getBoolean(Constants.PREF_DUB_ENABLED, false))
        val variables = "\"search\":{\"allowAdult\":$allowAdult,\"allowUnknown\":$allowUnknown,\"query\":\"$anime\"},\"limit\":39,\"page\":1,\"translationType\":\"$subDub\",\"countryOrigin\":\"JP\""
        val queryTypes = "\$search:SearchInput,\$limit:Int,\$page:Int,\$translationType:VaildTranslationTypeMangaEnumType,\$countryOrigin:VaildCountryOriginEnumType"
        val query = "mangas(search:\$search,limit:\$limit,page:\$page,translationType:\$translationType,countryOrigin:\$countryOrigin){edges{_id,malId}}"
        return connectAllAnime(variables, queryTypes, query)
    }

    fun getDetailsByIds(ids: String): String? {
        val variables = "\"ids\":[$ids]"
        val queryTypes = "\$ids:[String!]!"
        val query = "mangasWithIds(ids:\$ids){_id,name,thumbnail}"
        return connectAllAnime(variables, queryTypes, query)
    }
}