package com.ghostreborn.akira.kitsu

import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.models.Anime
import com.ghostreborn.kitsumodified.models.Entry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class KitsuAPI {

    suspend fun login(userName: String, pass: String) = withContext(Dispatchers.IO) {
        return@withContext Constants.api
            .login(username = userName, password = pass)
            .execute().body()
    }

    suspend fun user(token: String) = withContext(Dispatchers.IO) {
        return@withContext Constants.api
            .user("Bearer $token")
            .execute().body()?.data?.get(0)?.id
    }

    private suspend fun entryNum(userID: String) = withContext(Dispatchers.IO) {
        return@withContext Constants.api
            .entryNumber(
                userId = userID
            )
            .execute().body()?.meta?.count
    }

    private suspend fun entry(userID: String, num: String) = withContext(Dispatchers.IO) {
        return@withContext Constants.api
            .entry(
                userId = userID,
                num = num
            )
            .execute().body()?.included
    }

    suspend fun getAll(userId: String): ArrayList<Anime> {
        val num = entryNum(userId)!!
        val entries: ArrayList<Entry.Included> = ArrayList()
        for (offset in 0 until num step 100) {
            entries.addAll(entry(userId, offset.toString()) as ArrayList<Entry.Included>)
        }
        val anime = ArrayList<Anime>()
        for (entry in entries) {
            anime.add(Anime(
                entry.attributes.canonicalTitle,
                entry.attributes.posterImage.medium
            ))
        }
        entries.clear()
        return anime
    }

}