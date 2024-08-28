package com.ghostreborn.akira

import com.ghostreborn.akira.anilist.UserListInterface
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Constants {

    var hasClickedLoginButton = false

    const val SHARED_PREF = "AKIRA_PREF"
    const val PREF_USER_ID = "USER_ID"

    const val AUTH_URL = "https://anilist.co/api/v2/oauth/authorize?client_id=20149&response_type=code&redirect_uri=wanpisu://ghostreborn.in"

    // API
    private const val BASE_URL = "https://graphql.anilist.co/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: UserListInterface by lazy {
        retrofit.create(UserListInterface::class.java)
    }
}