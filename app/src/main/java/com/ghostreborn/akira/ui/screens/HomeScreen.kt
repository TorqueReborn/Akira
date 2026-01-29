package com.ghostreborn.akira.ui.screens

import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import com.ghostreborn.akira.utils.Utils
import androidx.compose.runtime.Composable
import com.ghostreborn.akira.ui.components.AnimeCard
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid

@Composable
fun HomeScreen() {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp),
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        val anime = Utils().getSampleAnimeList()
        items(anime.size) { index ->
            AnimeCard(anime = anime[index])
        }
    }
}