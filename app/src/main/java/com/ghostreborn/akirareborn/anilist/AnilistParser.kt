package com.ghostreborn.akirareborn.anilist

import android.content.Context
import androidx.room.Room
import com.ghostreborn.akirareborn.allAnime.AllAnimeParser
import com.ghostreborn.akirareborn.database.Anilist
import com.ghostreborn.akirareborn.database.AnilistDatabase
import org.json.JSONObject

class AnilistParser {
    fun saveAnime(animeId: String, status: String, progress: String, context: Context) {
        val entry = JSONObject(AnilistNetwork().saveAnime(animeId, status, progress)).getJSONObject("data").getJSONObject("SaveMediaListEntry")

        val id = entry.getString("id")
        val malId = entry.getJSONObject("media").getString("idMal")
        val title = entry.getJSONObject("media").getJSONObject("title").getString("native")
        val allAnimeId = AllAnimeParser().allAnimeIdWithMalId(title, malId)
        val currentProgress = entry.getString("progress")

        Room.databaseBuilder(context, AnilistDatabase::class.java, "Akira").build().apply {
            anilistDao().insertAll(Anilist(id, malId, allAnimeId, title, currentProgress))
            close()
        }
    }

    fun deleteAnime(mediaId: String, context: Context) {
        AnilistNetwork().deleteAnime(mediaId)
        Room.databaseBuilder(context, AnilistDatabase::class.java, "Akira").build().apply {
            anilistDao().findByMediaID(mediaId).let { anilist ->
                anilistDao().delete(anilist)
            }
            close()
        }
    }
}