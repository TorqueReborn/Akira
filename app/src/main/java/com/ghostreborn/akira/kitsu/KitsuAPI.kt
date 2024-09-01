package com.ghostreborn.akira.kitsu

import com.ghostreborn.akira.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class KitsuAPI {

    suspend fun login(username: String, password: String) = withContext(Dispatchers.IO) {
        return@withContext Constants.api
            .login(username = username, password = password)
            .execute()
            .body()
    }

    suspend fun user(token: String) = withContext(Dispatchers.IO) {
        return@withContext Constants.api
            .user("Bearer $token")
            .execute()
            .body()
    }

}