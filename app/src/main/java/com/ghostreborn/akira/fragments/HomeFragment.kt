package com.ghostreborn.akira.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.R
import com.ghostreborn.akira.adapter.AnimeAdapter
import com.ghostreborn.akira.models.Anime
import com.ghostreborn.akira.utils.AkiraUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var animes: ArrayList<Anime>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recycler = view.findViewById<RecyclerView>(R.id.search_recycler).apply {
            layoutManager = LinearLayoutManager(requireContext())
        }
        animes = ArrayList()
        CoroutineScope(Dispatchers.IO).launch {
            animes.addAll(AkiraUtils().entry(requireContext()))
            withContext(Dispatchers.Main){
                recycler.adapter = AnimeAdapter(animes)
            }
        }
        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItemCount = recyclerView.layoutManager?.itemCount ?: 0
                val lastVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                if (totalItemCount <= (lastVisibleItemPosition + 5)) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val anime = AkiraUtils().entry(requireContext())
                        if (anime.isNotEmpty()){
                            animes.addAll(anime)
                            withContext(Dispatchers.Main){
                                recycler.adapter = AnimeAdapter(animes)
                            }
                        }
                    }
                }
            }
        })
    }
}