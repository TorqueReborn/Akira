package com.ghostreborn.akira

import com.ghostreborn.akira.abstr.AnimeAbstract
import com.ghostreborn.akira.abstr.MangaAbstract
import com.ghostreborn.akira.parsers.AllManga
import com.ghostreborn.akira.parsers.Gojo

object Constants {
    var animeId: String = ""
    var mangaId: String = ""
    var animeThumbnail: String = ""
    var mangaThumbnail: String = ""
    var animeEpisode: String = ""
    var mangaChapter: String = ""
    var episodeWatchId: String = ""
    var animeUrl: String = ""
    val api: AnimeAbstract = Gojo()
    val mangaApi: MangaAbstract = AllManga()
    var isManga: Boolean = false

    val PREF_NAME = "AKIRA"
    val PREF_TOKEN = "TOKEN"
    val PREF_USER_ID = "USER_ID"

}