package com.ghostreborn.akira.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.R
import com.ghostreborn.akira.adapter.AnimeAdapter
import com.ghostreborn.akira.adapter.SearchAdapter
import com.ghostreborn.akira.parser.kitsu.KitsuAPI
import com.ghostreborn.akira.models.Anime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var userID: String
    private lateinit var animes: ArrayList<Anime>
    private lateinit var recycler: RecyclerView
    private lateinit var search: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = layoutInflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        CoroutineScope(Dispatchers.IO).launch {
            animes = getEntry(Constants.offset)
            withContext(Dispatchers.Main) {
                recycler.adapter = AnimeAdapter(animes)
            }
        }

        search = view.findViewById(R.id.search_edit_text)
        recycler= view.findViewById<RecyclerView?>(R.id.search_recycler).apply {
            layoutManager = LinearLayoutManager(context)
        }
        userID = requireContext()
            .getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
            .getString(Constants.PREF_USER_ID, "")!!
    }

    override fun onResume() {
        super.onResume()
        search()

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
        if (Constants.offset <= Constants.total){
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
        Constants.total = entry.meta.count
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

    private fun search(){
        search.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                CoroutineScope(Dispatchers.IO).launch {
                    val animes = KitsuAPI().search(search.text.toString())!!
                    withContext(Dispatchers.Main) {
                        val adapter = SearchAdapter(animes)
                        recycler.adapter = adapter
                    }
                }
                true
            } else {
                false
            }
        }
    }
}