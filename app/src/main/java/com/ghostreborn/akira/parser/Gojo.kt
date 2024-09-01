package com.ghostreborn.akira.parser

import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.models.Episode
import com.ghostreborn.akira.models.Server
import com.ghostreborn.akira.parser.gojo.GojoParser
import com.ghostreborn.akira.provider.AnimeProvider

class Gojo: AnimeProvider() {
    override fun episodes(id: String): ArrayList<ArrayList<Episode>> {
        return GojoParser().episodes(Constants.animeID)
    }

    override fun servers(id: String, episode: String): ArrayList<Server> {
        return GojoParser().servers(
            Constants.animeID,
            Constants.animeEpisode,
            Constants.episodeWatchId
        )
    }
}