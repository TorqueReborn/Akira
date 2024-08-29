package com.ghostreborn.akira.model


import com.google.gson.annotations.SerializedName

data class GraphQLRequest(
    val query: String
)

data class UserList(
    @SerializedName("data")
    val `data`: Data
)

data class Data(
    @SerializedName("MediaListCollection")
    val mediaListCollection: MediaListCollection,
    @SerializedName("Page")
    val page: Page
)

data class Page(
    @SerializedName("media")
    val media: List<Media>
)

data class MediaListCollection(
    @SerializedName("lists")
    val lists: List<Lists>
)

data class Lists(
    @SerializedName("entries")
    val entries: List<Entry>
)

data class Entry(
    @SerializedName("media")
    val media: Media,
    @SerializedName("progress")
    val progress: Int
)

data class Media(
    @SerializedName("coverImage")
    val coverImage: CoverImage,
    @SerializedName("title")
    val title: Title,
    @SerializedName("episodes")
    val episodes: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("nextAiringEpisode")
    val nextAiringEpisode: Any?
)

data class Title(
    @SerializedName("userPreferred")
    val userPreferred: String
)

data class CoverImage(
    @SerializedName("large")
    val large: String
)