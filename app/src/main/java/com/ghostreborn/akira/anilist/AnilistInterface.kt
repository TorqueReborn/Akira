package com.ghostreborn.akira.anilist

import com.ghostreborn.akira.model.GraphQLRequest
import com.ghostreborn.akira.model.UserList
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AnilistInterface {
    @POST("graphql")
    fun getData(@Body query: GraphQLRequest): Call<UserList>
}