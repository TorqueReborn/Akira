package com.ghostreborn.akirareborn.anilist

import android.content.Context
import androidx.room.Room
import com.ghostreborn.akirareborn.allAnime.AllAnimeParser
import com.ghostreborn.akirareborn.database.Anilist
import com.ghostreborn.akirareborn.database.AnilistDatabase
import org.json.JSONObject

class AnilistParser {
    fun saveAnime(animeId: String, status: String, progress: String, context: Context) {
        val rawJSON = AnilistNetwork().saveAnime(animeId, status, progress)
        val entry = JSONObject(rawJSON)
            .getJSONObject("data")
            .getJSONObject("SaveMediaListEntry")

        val id = entry.getString("id")
        val malId = entry.getJSONObject("media").getString("idMal")
        val title = entry.getJSONObject("media").getJSONObject("title").getString("native")
        val allAnimeId = AllAnimeParser().allAnimeIdWithMalId(title, malId)
        val currentProgress = entry.getString("progress")

        val instance = Room.databaseBuilder(
            context,
            AnilistDatabase::class.java,
            "Akira"
        ).build()
        instance.anilistDao().insertAll(
            Anilist(id, malId, allAnimeId, title, currentProgress)
        )
        instance.close()
    }

    fun deleteAnime(mediaId: String, context: Context){
        AnilistNetwork().deleteAnime(mediaId)
        val instance = Room.databaseBuilder(
            context,
            AnilistDatabase::class.java,
            "Akira"
        ).build()
        instance.anilistDao().delete(
            instance.anilistDao().findByMediaID(mediaId)
        )
        instance.close()
    }
}