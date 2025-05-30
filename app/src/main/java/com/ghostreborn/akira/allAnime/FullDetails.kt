package com.ghostreborn.akira.allAnime

import android.text.Html
import android.util.Log
import com.ghostreborn.akira.model.AnimeDetails
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL


class FullDetails {

    fun fullDetail(id: String): AnimeDetails {
        val variables = "\"showId\":\"$id\""
        val queryTypes = "\$showId:String!"
        val query = "show(_id:\$showId){aniListId,name,englishName,description,thumbnail,banner,availableEpisodesDetail,relatedShows}"
        val url = "https://api.allanime.day/api?variables={$variables}&query=query($queryTypes){$query}"
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Referer", "https://allmanga.to")
        val rawJSON = connection.inputStream.bufferedReader().readText()

        var aniListId = ""
        var anime = ""
        var description = ""
        var thumbnail = ""
        var banner = ""
        var prequel = ""
        var sequel = ""
        val episodes = ArrayList<String>()

        try {
            val show = JSONObject(rawJSON)
                .getJSONObject("data")
                .getJSONObject("show")

            aniListId = show.getString("aniListId")

            anime = show.getString("englishName")

            if (anime == "null") {
                anime = show.getString("name")
            }

            description =
                Html.fromHtml(show.getString("description"), Html.FROM_HTML_MODE_LEGACY).toString()
            thumbnail = show.getString("thumbnail")
            banner = show.getString("banner")

            val sub = show.getJSONObject("availableEpisodesDetail")
                .getJSONArray("sub")
            for (i in 0..<sub.length()) {
                episodes.add(sub.getString(i))
            }

            val relatedShows = show.getJSONArray("relatedShows")
            for (i in 0..<relatedShows.length()) {
                val related = relatedShows.getJSONObject(i)
                val relation = related.optString("relation")
                if ("prequel" == relation) {
                    prequel = related.optString("showId")
                }
                if ("sequel" == relation) {
                    sequel = related.optString("showId")
                }
            }

            if (!thumbnail.contains("https")) {
                thumbnail = "https://wp.youtube-anime.com/aln.youtube-anime.com/$thumbnail"
            }
        } catch (e: JSONException) {
            Log.e("TAG", e.toString())
        }
        return AnimeDetails(
            aniListId,
            anime,
            description,
            banner,
            thumbnail,
            prequel,
            sequel,
            episodes
        )
    }

}