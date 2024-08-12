package com.ghostreborn.akira.fragment

import android.os.Bundle
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import com.ghostreborn.akira.allAnime.AllAnimeParser
import com.ghostreborn.akira.presenter.AnimePresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : BrowseSupportFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        headersState = HEADERS_DISABLED
        adapter = createRowsAdapter()
    }

    private fun createRowsAdapter(): ArrayObjectAdapter {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

        CoroutineScope(Dispatchers.IO).launch {
            val animeList = AllAnimeParser().searchAnime("One Piece")
            withContext(Dispatchers.Main) {
                animeList.forEach { animeList ->
                    val listRowAdapter = ArrayObjectAdapter(AnimePresenter()).apply {
                        addAll(0, animeList.take(4))
                    }
                    rowsAdapter.add(ListRow(listRowAdapter))
                }
            }
        }
        return rowsAdapter
    }
}