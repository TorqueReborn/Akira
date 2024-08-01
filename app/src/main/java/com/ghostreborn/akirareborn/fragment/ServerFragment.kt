package com.ghostreborn.akirareborn.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akirareborn.R
import com.ghostreborn.akirareborn.adapter.ServerAdapter
import com.ghostreborn.akirareborn.allAnime.AllAnimeParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ServerFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            inflater.inflate(R.layout.fragment_server, container, false)

        val fragmentRecyclerView: RecyclerView =
            view.findViewById(R.id.server_recycler_view)
        fragmentRecyclerView.layoutManager = LinearLayoutManager(context)

        val progressBar: ProgressBar = view.findViewById(R.id.server_progress_bar)

        CoroutineScope(Dispatchers.IO).launch {
            val sources =
                AllAnimeParser().getSourceUrls(AnimeFragment.allAnimeID, AnimeFragment.animeEpisode)
            withContext(Dispatchers.Main) {
                progressBar.visibility = ProgressBar.GONE
                fragmentRecyclerView.adapter = ServerAdapter(sources)
            }
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window!!.setLayout(width, height)
        }
    }

}