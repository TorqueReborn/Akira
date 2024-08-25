package com.ghostreborn.akira.parsers.allManga

import java.net.HttpURLConnection
import java.net.URL

class AllMangaNetwork {

    private fun connectAllAnime(
        variables: String,
        queryTypes: String,
        query: String
    ): String {
        val url = URL("https://api.allanime.day/api?variables={$variables}&query=query($queryTypes){$query}")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Referer", "https://allanime.to")
        return connection.inputStream.bufferedReader().use { it.readText() }
    }

    fun search(manga: String): String {
        val variables = "\"search\":{\"allowAdult\":true,\"allowUnknown\":false,\"query\":\"$manga\"},\"limit\":39,\"page\":1,\"translationType\":\"sub\",\"countryOrigin\":\"JP\""
        val queryTypes = "\$search:SearchInput,\$limit:Int,\$page:Int,\$translationType:VaildTranslationTypeMangaEnumType,\$countryOrigin:VaildCountryOriginEnumType"
        val query = "mangas(search:\$search,limit:\$limit,page:\$page,translationType:\$translationType,countryOrigin:\$countryOrigin){edges{_id,name,thumbnail,aniListId}}"
        return connectAllAnime(variables, queryTypes, query)
    }

    fun chapters(id: String): String {
        val variables = "\"id\":\"$id\""
        val queryTypes = "\$id:String!"
        val query = "manga(_id:\$id){availableChaptersDetail}"
        return connectAllAnime(variables, queryTypes, query)
    }

    fun pages(id: String, chapter: String): String {
        val variables = "\"mangaId\":\"$id\",\"chapterString\":\"$chapter\",\"translationType\":\"sub\""
        val queryTypes = "\$mangaId:String!,\$chapterString:String!,\$translationType:VaildTranslationTypeMangaEnumType!"
        val query = "chapterPages(mangaId:\$mangaId,chapterString:\$chapterString,translationType:\$translationType){edges{pictureUrls}}"
        return connectAllAnime(variables, queryTypes, query)
    }

}