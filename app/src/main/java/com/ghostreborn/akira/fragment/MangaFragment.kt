package com.ghostreborn.akira.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.R
import com.ghostreborn.akira.adapter.MangaAdapter
import com.ghostreborn.akira.allManga.AllMangaParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MangaFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_manga, container, false)
        recyclerView = view.findViewById(R.id.manga_recycler_view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchManga()
    }

    private fun fetchManga() {
        CoroutineScope(Dispatchers.IO).launch {
            val mangas = AllMangaParser().searchManga("")
            withContext(Dispatchers.Main) {
                recyclerView.adapter = MangaAdapter(mangas)
                recyclerView.layoutManager =
                    GridLayoutManager(context, 1, RecyclerView.HORIZONTAL, false)
            }
        }
    }

}