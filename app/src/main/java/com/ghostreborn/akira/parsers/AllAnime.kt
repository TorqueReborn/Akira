package com.ghostreborn.akira.parsers

import com.ghostreborn.akira.abstr.AnimeAbstract
import com.ghostreborn.akira.model.Anime
import com.ghostreborn.akira.parsers.allAnime.AllAnimeParser

class AllAnime: AnimeAbstract() {

    override fun recent(): ArrayList<Anime> {
        return AllAnimeParser().searchAnime("")
    }

    override fun search(anime: String): ArrayList<Anime> {
        return AllAnimeParser().searchAnime(anime)
    }

    override fun episodes(id:String): ArrayList<ArrayList<String>> {
        return AllAnimeParser().episodes(id)
    }

    override fun servers(id: String, episode: String): ArrayList<String> {
        return AllAnimeParser().servers(id,episode)
    }
}