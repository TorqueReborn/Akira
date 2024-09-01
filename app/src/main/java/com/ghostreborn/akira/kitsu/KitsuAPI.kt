package com.ghostreborn.akira.kitsu

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

}