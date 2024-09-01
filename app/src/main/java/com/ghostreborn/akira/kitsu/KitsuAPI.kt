package com.ghostreborn.akira.kitsu

import com.ghostreborn.akira.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class KitsuAPI {

    suspend fun login() = withContext(Dispatchers.IO) {
        Constants.token =  Constants.api
            .login(username = Constants.userName, password = Constants.pass)
            .execute()
            .body()
            ?.access_token.toString()
    }

    suspend fun user() = withContext(Dispatchers.IO) {
        Constants.userId = Constants.api
            .user("Bearer ${Constants.token}")
            .execute()
            .body()?.data?.get(0)?.id.toString()
    }

    suspend fun entryNum() = withContext(Dispatchers.IO) {
        Constants.entryNum = Constants.api
            .entryNumber(
                userId = Constants.userId
            )
            .execute()
            .body()
            ?.meta
            ?.count.toString()
    }

}