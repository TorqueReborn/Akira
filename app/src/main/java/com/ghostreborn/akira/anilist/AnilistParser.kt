package com.ghostreborn.akira.anilist

import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.model.Anime
import com.ghostreborn.akira.model.GraphQLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AnilistParser {

    suspend fun userAnime() = withContext(Dispatchers.IO) {
        Constants.api.getUserList(GraphQLRequest("query { MediaListCollection(userName:\"GhostReborn\", type: ANIME) { lists { entries { media { title { userPreferred } coverImage { large } } progress } } } }"))
            .execute().body()?.data?.mediaListCollection?.lists?.firstOrNull()?.entries
            ?.map { Anime(it.media.title.userPreferred, "${it.progress}", it.media.coverImage.large) }
            ?.let(::ArrayList) ?: arrayListOf()
    }

    suspend fun userManga() = withContext(Dispatchers.IO) {
        Constants.api.getUserList(GraphQLRequest("query { MediaListCollection(userName:\"GhostReborn\", type: MANGA) { lists { entries { media { title { userPreferred } coverImage { large } } progress } } } }"))
            .execute().body()?.data?.mediaListCollection?.lists?.firstOrNull()?.entries
            ?.map { Anime(it.media.title.userPreferred, "${it.progress}", it.media.coverImage.large) }
            ?.let(::ArrayList) ?: arrayListOf()
    }
}