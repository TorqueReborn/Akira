package com.ghostreborn.akira.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostreborn.akira.api.allAnime.LatestAnime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel: ViewModel() {

    init {
        fetchAnime()
    }

    private fun fetchAnime() {
        viewModelScope.launch(Dispatchers.IO) {
            val anime = LatestAnime().latestAnime()
        }
    }
}