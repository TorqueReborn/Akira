package com.ghostreborn.akira.anilist

import com.ghostreborn.akira.model.GraphQLRequest
import com.ghostreborn.akira.model.UserList
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UserListInterface {
    @POST("graphql")
    fun getUserList(@Body query: GraphQLRequest): Call<UserList>
}