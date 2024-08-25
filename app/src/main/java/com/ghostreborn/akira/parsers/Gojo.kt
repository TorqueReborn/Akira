package com.ghostreborn.akira.parsers

import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.abstr.AnimeAbstract
import com.ghostreborn.akira.model.Anime
import com.ghostreborn.akira.model.Episode
import com.ghostreborn.akira.model.Server
import com.ghostreborn.akira.parsers.gojo.GojoParser

class Gojo: AnimeAbstract() {
    override fun recent(): ArrayList<Anime> {
        return GojoParser().recentlyUpdated()
    }

    override fun search(anime: String): ArrayList<Anime> {
        return GojoParser().search(anime)
    }

    override fun episodes(id: String): ArrayList<ArrayList<Episode>> {
        return GojoParser().episodes(Constants.animeId)
    }

    override fun servers(id: String, episode: String): ArrayList<Server> {
        return GojoParser().servers(Constants.animeId, Constants.animeEpisode, Constants.episodeWatchId)
    }
}