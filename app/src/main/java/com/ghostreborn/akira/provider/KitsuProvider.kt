package com.ghostreborn.akira.provider

import com.ghostreborn.akira.models.AnimeMapping
import com.ghostreborn.akira.models.kitsu.AnimeEntry
import com.ghostreborn.akira.models.kitsu.AnimeSearch
import com.ghostreborn.akira.models.kitsu.Authentication
import com.ghostreborn.akira.models.kitsu.Entry
import com.ghostreborn.akira.models.kitsu.User
import retrofit2.Call
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
    fun user(
        @Header("Authorization") authHeader: String
    ): Call<User>

    @GET("edge/users/{id}/library-entries")
    fun ids(
        @Path("id") userId: String,
        @Query("page[offset]") num: Int,
        @Query("page[limit]") limit: Int = 50,
        @Query("fields[library-entries]") libraryFields: String = "id,status,progress",
        @Query("filter[status]") filter: String = "current",
        @Query("filter[kind]") kind: String = "anime"
    ): Call<Entry>

    @GET("edge/library-entries/{id}/anime")
    fun anime(
        @Path("id") entryID: String,
        @Query("fields[anime]") libraryFields: String = "canonicalTitle,posterImage,episodeCount"
    ): Call<AnimeEntry>

    @GET("edge/anime")
    fun search(
        @Query("filter[text]") anime: String,
        @Query("fields[anime]") fields: String = "id,canonicalTitle,episodeCount,posterImage"
    ): Call<AnimeSearch>

    @GET("edge/anime/{id}/mappings")
    fun mapping(
        @Path("id") entryID: String,
        @Query("filter[externalSite]") filter: String = "anilist/anime",
        @Query("fields[mappings]") fields: String = "externalId"
    ): Call<AnimeMapping>
}