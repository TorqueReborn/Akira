package com.ghostreborn.akira

import com.ghostreborn.akira.abstr.AnimeAbstract
import com.ghostreborn.akira.abstr.MangaAbstract
import com.ghostreborn.akira.parsers.AllAnime
import com.ghostreborn.akira.parsers.AllManga

object Constants {
    var animeId: String = ""
    var mangaId: String = ""
    var animeEpisode: String = ""
    var mangaChapter: String = ""
    var episodeWatchId: String = ""
    var animeUrl: String = ""
    val api:AnimeAbstract = AllAnime()
    val mangaApi:MangaAbstract = AllManga()
}