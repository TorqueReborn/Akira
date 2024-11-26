package com.ghostreborn.akira.allAnime

import java.net.HttpURLConnection
import java.net.URL

class AllAnimeNetwork {

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

    fun searchAnime(anime: String): String {
        val variables = "\"search\":{\"allowAdult\":false,\"allowUnknown\":false,\"query\":\"$anime\"},\"limit\":18,\"page\":1,\"translationType\":\"sub\",\"countryOrigin\":\"JP\""
        val queryTypes = "\$search:SearchInput,\$limit:Int,\$page:Int,\$translationType:VaildTranslationTypeEnumType,\$countryOrigin:VaildCountryOriginEnumType"
        val query = "shows(search:\$search,limit:\$limit,page:\$page,translationType:\$translationType,countryOrigin:\$countryOrigin){edges{_id,name,thumbnail}}"
        return connectAllAnime(variables, queryTypes, query)
    }

}