package com.ghostreborn.akira

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TestFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_test, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        CoroutineScope(Dispatchers.IO).launch {
            val test = AllAnimeTest().getSomething()
            withContext(Dispatchers.Main) {
                view.findViewById<TextView>(R.id.test_text).text = test.toString()
            }
        }

        CoroutineScope(Dispatchers.IO).launch {

            val animeList = AllAnimeTest().getSomething()
            val animeItem = listOf(
                AnimeItem("Spring 2025", animeList),
                AnimeItem("Spring 2025", animeList),
                AnimeItem("Spring 2025", animeList),
                AnimeItem("Spring 2025", animeList),
                AnimeItem("Spring 2025", animeList),
            )

            val adapter = AnimeItemAdapter(animeItem)

            withContext(Dispatchers.Main) {
                val recycler = view.findViewById<RecyclerView>(R.id.test_recycler)
                recycler.adapter = adapter
                recycler.layoutManager = LinearLayoutManager(context)
            }
        }

    }

}