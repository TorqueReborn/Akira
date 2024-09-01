package com.ghostreborn.akira.provider

import com.ghostreborn.akira.models.Authentication
import com.ghostreborn.kitsumodified.models.Entry
import com.ghostreborn.kitsumodified.models.EntryNum
import com.ghostreborn.kitsumodified.models.EntryRequest
import com.ghostreborn.kitsumodified.models.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface KitsuProvider {
    @POST("oauth/token")
    @FormUrlEncoded
    fun login(
        @Field("grant_type") grantType: String = "password",
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<Authentication>

    @GET("edge/users?filter[self]=true")
    fun user(@Header("Authorization") authHeader: String
    ): Call<User>

    @POST("edge/library-entries")
    fun add(
        @Header("Authorization") authHeader: String,
        @Header("Accept") acceptHeader: String,
        @Header("Content-Type") contentTypeHeader: String,
        @Body body: EntryRequest
    ): Call<Void>

    @DELETE("edge/library-entries/{id}")
    fun delete(
        @Header("Authorization") authHeader: String,
        @Header("Accept") acceptHeader: String,
        @Header("Content-Type") contentTypeHeader: String,
        @Path("id") id: String
    ): Call<Void>

    @GET("edge/users/{id}/library-entries")
    fun entry(
        @Path("id") userId: String,
        @Query("page[offset]") num: String,
        @Query("page[limit]") limit: Int = 1,
        @Query("fields[library-entries]") libraryFields: String = "id,progress,status",
        @Query("include") include: String = "anime",
        @Query("fields[anime]") animeFields: String = "canonicalTitle,posterImage,coverImage",
    ): Call<Entry>

    @GET("edge/users/{id}/library-entries")
    fun entryNumber(
        @Path("id") userId: String,
        @Query("page[offset]") num: Int = 1,
        @Query("page[limit]") limit: Int = 1,
        @Query("fields[library-entries]") libraryFields: String = "id",
    ): Call<EntryNum>
}