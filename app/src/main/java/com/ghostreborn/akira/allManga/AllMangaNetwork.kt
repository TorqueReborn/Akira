package com.ghostreborn.akira.allManga

import java.net.HttpURLConnection
import java.net.URL

class AllMangaNetwork {
    private fun connectAllAnime(
        variables: String,
        queryTypes: String,
        query: String
    ): String {
        val url =
            URL("https://api.allanime.day/api?variables={$variables}&query=query($queryTypes){$query}")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "Mozilla/5.0")
        connection.setRequestProperty("Referer", "https://allanime.to")
        connection.setRequestProperty("Cipher", "AES256-SHA256")
        connection.setRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/121.0"
        )
        return connection.inputStream.bufferedReader().use { it.readText() }
    }

    private fun getSubDub(isDubEnabled: Boolean): String {
        return if (isDubEnabled) "dub" else "sub"
    }

    fun searchManga(anime: String): String {
        val allowAdult = false
        val allowUnknown = false
        val subDub = "sub"
        val variables =
            "\"search\":{\"allowAdult\":$allowAdult,\"allowUnknown\":$allowUnknown,\"query\":\"$anime\"},\"limit\":39,\"page\":1,\"translationType\":\"$subDub\",\"countryOrigin\":\"JP\""
        val queryTypes =
            "\$search:SearchInput,\$limit:Int,\$page:Int,\$translationType:VaildTranslationTypeMangaEnumType,\$countryOrigin:VaildCountryOriginEnumType"
        val query =
            "mangas(search:\$search,limit:\$limit,page:\$page,translationType:\$translationType,countryOrigin:\$countryOrigin){edges{_id,name,thumbnail,aniListId}}"
        return connectAllAnime(variables, queryTypes, query)
    }

    fun mangaDetails(mangaId: String): String {
        val variables = "\"id\":\"$mangaId\""
        val queryTypes = "\$id:String!"
        val query = "manga(_id:\$id){name,thumbnail,description,banner,relatedMangas}"
        return connectAllAnime(variables, queryTypes, query)
    }

    fun mangaChapters(mangaId: String): String {
        val variables = "\"id\":\"$mangaId\""
        val queryTypes = "\$id:String!"
        val query = "manga(_id:\$id){availableChaptersDetail}"
        return connectAllAnime(variables, queryTypes, query)
    }

    fun chapterPages(mangaId: String, chapter: String): String {
        val variables =
            "\"mangaId\":\"$mangaId\",\"chapterString\":\"$chapter\",\"translationType\":\"sub\""
        val queryTypes =
            "\$mangaId:String!,\$chapterString:String!,\$translationType:VaildTranslationTypeMangaEnumType!"
        val query =
            "chapterPages(mangaId:\$mangaId,chapterString:\$chapterString,translationType:\$translationType){edges{pictureUrls}}"
        return connectAllAnime(variables, queryTypes, query)
    }

    fun anilistIdWithAllMangaID(id: String): String {
        val variables = "\"showId\":\"$id\""
        val queryTypes = "\$showId:String!"
        val query = "manga(_id:\$showId){aniListId}"
        return connectAllAnime(variables, queryTypes, query)
    }

    fun allAnimeIdWithMalId(anime: String): String {
        val allowAdult = false
        val allowUnknown = false
        val subDub = getSubDub(false)
        val variables =
            "\"search\":{\"allowAdult\":$allowAdult,\"allowUnknown\":$allowUnknown,\"query\":\"$anime\"},\"limit\":39,\"page\":1,\"translationType\":\"$subDub\",\"countryOrigin\":\"JP\""
        val queryTypes =
            "\$search:SearchInput,\$limit:Int,\$page:Int,\$translationType:VaildTranslationTypeMangaEnumType,\$countryOrigin:VaildCountryOriginEnumType"
        val query =
            "mangas(search:\$search,limit:\$limit,page:\$page,translationType:\$translationType,countryOrigin:\$countryOrigin){edges{_id,malId}}"
        return connectAllAnime(variables, queryTypes, query)
    }

    fun getDetailsByIds(ids: String): String {
        val variables = "\"ids\":[$ids]"
        val queryTypes = "\$ids:[String!]!"
        val query = "mangasWithIds(ids:\$ids){_id,name,thumbnail}"
        return connectAllAnime(variables, queryTypes, query)
    }
}