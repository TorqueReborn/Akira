package com.ghostreborn.akira.anilist

import com.ghostreborn.akira.model.GraphQLRequest
import com.ghostreborn.akira.model.Anilist
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AnilistApi {
    @POST("graphql")
    fun getData(@Body query: GraphQLRequest): Call<Anilist>
}