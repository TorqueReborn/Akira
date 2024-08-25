package com.ghostreborn.akira

import com.ghostreborn.akira.abstr.AnimeAbstract
import com.ghostreborn.akira.abstr.MangaAbstract
import com.ghostreborn.akira.parsers.AllManga
import com.ghostreborn.akira.parsers.Gojo

object Constants {
    var animeId: String = ""
    var mangaId: String = ""
    var animeEpisode: String = ""
    var mangaChapter: String = ""
    var episodeWatchId: String = ""
    var animeUrl: String = ""
    val api:AnimeAbstract = Gojo()
    val mangaApi:MangaAbstract = AllManga()
}