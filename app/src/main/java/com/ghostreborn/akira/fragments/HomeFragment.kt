package com.ghostreborn.akira.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.R
import com.ghostreborn.akira.adapter.AnimeAdapter
import com.ghostreborn.akira.kitsu.KitsuAPI
import com.ghostreborn.akira.models.Anime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var userID: String
    private lateinit var animes: ArrayList<Anime>
    private lateinit var recycler: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = LayoutInflater.from(context).inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userID = requireContext()
            .getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
            .getString(Constants.PREF_USER_ID, "")!!

        recycler = view.findViewById<RecyclerView>(R.id.home_recycler).apply {
            layoutManager = LinearLayoutManager(requireContext())
        }

        CoroutineScope(Dispatchers.IO).launch {
            animes = getEntry(Constants.offset)
            withContext(Dispatchers.Main) {
                recycler.adapter = AnimeAdapter(animes)
            }
        }

        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItemCount = recyclerView.layoutManager?.itemCount ?: 0
                val lastVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                if (totalItemCount <= (lastVisibleItemPosition + 5)) {
                    loadMoreItems()
                }
            }
        })
    }

    fun loadMoreItems(){
        Constants.offset += 10
        if (Constants.offset <= requireContext().getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE).getInt(Constants.PREF_TOTAL_LIST, 0)){
            CoroutineScope(Dispatchers.IO).launch {
                val moreAnimes = getEntry(Constants.offset)
                animes.addAll(moreAnimes)
                withContext(Dispatchers.Main){
                    recycler.adapter = AnimeAdapter(animes)
                }
            }
        }
    }

    private suspend fun getEntry(offset: Int): ArrayList<Anime> {
        val entry = KitsuAPI().entry(userID, offset)!!
        val animes = ArrayList<Anime>()
        val data = entry.data
        val included = entry.included
        for (i in 0 until included.size){
            animes.add(Anime(
                kitsuID = included[i].id,
                title = included[i].attributes.canonicalTitle,
                progress = data[i].attributes.progress.toString(),
                thumbnail = included[i].attributes.posterImage.large
            ))
        }
        return animes
    }
}