package com.ghostreborn.akira.anilist

import android.app.Activity
import android.content.Context
import androidx.room.Room
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.database.Anilist
import com.ghostreborn.akira.database.AnilistDatabase
import org.json.JSONObject

class AnilistParser {
    fun saveAnime(
        animeId: String,
        status: String,
        progress: String,
        context: Context,
        isManga: Boolean,
        activity: Activity
    ): Boolean {
        val raw = JSONObject(AnilistNetwork().saveAnimeManga(animeId, status, progress, activity))
        if (raw.getJSONObject("data").isNull("SaveMediaListEntry")) {
            return false
        }
        val entry = raw.getJSONObject("data").getJSONObject("SaveMediaListEntry")
        val id = entry.getString("id")
        val malId = entry.getJSONObject("media").getString("idMal")
        val title = entry.getJSONObject("media").getJSONObject("title").getString("native")
        val allAnimeId = if (isManga) Constants.allMangaID else Constants.allAnimeID
        val currentProgress = entry.getString("progress")

        Room.databaseBuilder(context, AnilistDatabase::class.java, "Akira").build().apply {
            anilistDao().insertAll(Anilist(id, malId, allAnimeId, title, currentProgress, false))
            close()
        }
        return true
    }

    fun deleteAnime(mediaId: String, context: Context, activity: Activity) {
        AnilistNetwork().deleteAnimeManga(mediaId, activity)
        Room.databaseBuilder(context, AnilistDatabase::class.java, "Akira").build().apply {
            anilistDao().findByMediaID(mediaId).let { anilist ->
                anilistDao().delete(anilist)
            }
            close()
        }
    }
}