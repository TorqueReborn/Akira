package com.ghostreborn.akira.anilist

import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.model.Anime
import com.ghostreborn.akira.model.GraphQLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AnilistParser {

    suspend fun userAnime(userID:String) = withContext(Dispatchers.IO) {
        Constants.api.getData(GraphQLRequest("query { MediaListCollection(userId:$userID, type: ANIME) { lists { entries { media { title { userPreferred } coverImage { large } } progress } } } }"))
            .execute().body()?.data?.mediaListCollection?.lists?.firstOrNull()?.entries
            ?.map { Anime(it.media.title.userPreferred, "${it.progress}", it.media.coverImage.large) }
            ?.let(::ArrayList) ?: arrayListOf()
    }

    suspend fun userManga(userID: String) = withContext(Dispatchers.IO) {
        Constants.api.getData(GraphQLRequest("query { MediaListCollection(userId:$userID, type: MANGA) { lists { entries { media { title { userPreferred } coverImage { large } } progress } } } }"))
            .execute().body()?.data?.mediaListCollection?.lists?.firstOrNull()?.entries
            ?.map { Anime(it.media.title.userPreferred, "${it.progress}", it.media.coverImage.large) }
            ?.let(::ArrayList) ?: arrayListOf()
    }

    suspend fun searchAnime(anime: String,type:String) = withContext(Dispatchers.IO) {
        Constants.api.getData(GraphQLRequest("{Page{media(search:\"$anime\",type:$type){title{userPreferred},coverImage{large},episodes}}}"))
            .execute().body()?.data?.page?.media
            ?.map { Anime(it.title.userPreferred, "${it.episodes}", it.coverImage.large) }
            ?.let(::ArrayList) ?: arrayListOf()
    }
}