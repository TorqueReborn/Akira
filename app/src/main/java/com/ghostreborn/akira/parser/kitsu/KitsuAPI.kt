package com.ghostreborn.akira.parser.kitsu

import com.ghostreborn.akira.Constants
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

    suspend fun entryNum(userID: String) = withContext(Dispatchers.IO) {
        return@withContext Constants.api
            .entryNumber(
                userId = userID
            )
            .execute().body()?.meta?.count
    }

    suspend fun entry(userID: String, num: Int) = withContext(Dispatchers.IO) {
        return@withContext Constants.api
            .entry(
                userId = userID,
                num = num
            )
            .execute().body()
    }

    suspend fun anilistID(animeID: String) = withContext(Dispatchers.IO) {
        return@withContext Constants.api
            .anilistID(
                animeID = animeID
            )
            .execute().body()
    }

    suspend fun malID(animeID: String) = withContext(Dispatchers.IO) {
        return@withContext Constants.api
            .malID(
                animeID = animeID
            )
            .execute().body()
    }

    suspend fun search(animeName: String) = withContext(Dispatchers.IO) {
        return@withContext Constants.api
            .search(
                animeName = animeName
            )
            .execute().body()
    }
}