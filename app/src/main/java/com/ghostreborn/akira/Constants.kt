package com.ghostreborn.akira

import com.ghostreborn.akira.abstr.AnimeAbstract
import com.ghostreborn.akira.parsers.AllAnime

object Constants {
    var animeId: String = ""
    var animeEpisode: String = ""
    var episodeWatchId: String = ""
    var animeUrl: String = ""
    val api:AnimeAbstract = AllAnime()
}