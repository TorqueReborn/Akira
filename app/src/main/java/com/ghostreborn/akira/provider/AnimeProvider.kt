package com.ghostreborn.akira.provider

import com.ghostreborn.akira.models.Episode
import com.ghostreborn.akira.models.Server

abstract class AnimeProvider {
    abstract fun episodes(id: String): ArrayList<ArrayList<Episode>>
    abstract fun servers(id: String,episode:String): ArrayList<Server>
}