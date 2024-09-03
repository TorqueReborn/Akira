package com.ghostreborn.akira.provider

import com.ghostreborn.akira.models.retro.AnimeID
import com.ghostreborn.akira.models.retro.Authentication
import com.ghostreborn.akira.models.retro.Entry
import com.ghostreborn.akira.models.retro.EntryMinimized
import com.ghostreborn.akira.models.retro.Search
import com.ghostreborn.akira.models.retro.EntryRequest
import com.ghostreborn.akira.models.retro.User
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
        @Query("page[offset]") num: Int,
        @Query("page[limit]") limit: Int = 10,
        @Query("fields[library-entries]") libraryFields: String = "id,progress,status",
        @Query("include") include: String = "anime",
        @Query("fields[anime]") animeFields: String = "canonicalTitle,posterImage,coverImage",
    ): Call<Entry>

    @GET("edge/anime/{id}/mappings")
    fun anilistID(
        @Path("id") animeID: String,
        @Query("filter[externalSite]") externalSite: String = "anilist/anime",
        @Query("fields[mappings]") mappingFields: String = "externalId"
    ): Call<AnimeID>

    @GET("edge/anime")
    fun search(
        @Query("filter[text]") animeName: String,
        @Query("fields[anime]") animeFields: String = "canonicalTitle,posterImage,coverImage"
    ): Call<Search>

    @GET("edge/users/{id}/library-entries")
    fun test(
        @Path("id") userId: String = "1402888",
        @Query("page[offset]") num: Int,
        @Query("page[limit]") limit: Int = 50,
        @Query("fields[library-entries]") libraryFields: String = "id,progress,status",
        @Query("include") include: String = "anime",
        @Query("fields[anime]") animeFields: String = "canonicalTitle,posterImage,coverImage",
    ): Call<EntryMinimized>

}