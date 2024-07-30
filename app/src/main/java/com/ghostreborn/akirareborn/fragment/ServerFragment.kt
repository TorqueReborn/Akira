package com.ghostreborn.akirareborn.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.adapter.ServerAdapter
import com.ghostreborn.akirareborn.allanime.AllAnimeParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ServerFragment(val episodeNumber: String) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            inflater.inflate(com.ghostreborn.akirareborn.R.layout.fragment_server, container, false)

        val fragmentRecyclerView: RecyclerView =
            view.findViewById(com.ghostreborn.akirareborn.R.id.server_recycler_view)
        fragmentRecyclerView.layoutManager = LinearLayoutManager(context)

        CoroutineScope(Dispatchers.IO).launch {
            AllAnimeParser().getSourceUrls(Constants.animeID, episodeNumber)
            withContext(Dispatchers.Main) {
                fragmentRecyclerView.adapter = ServerAdapter(requireContext())
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