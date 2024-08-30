package com.ghostreborn.akira.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.R
import com.ghostreborn.akira.adapter.CommonAdapter
import com.ghostreborn.akira.anilist.AnilistParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchFragment(val type: String) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_search, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val search: EditText = view.findViewById(R.id.search_edit_text)
        search.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                CoroutineScope(Dispatchers.IO).launch {
                    val anime = AnilistParser().searchAnime(search.text.toString(), type)
                    withContext(Dispatchers.Main) {
                        val adapter = CommonAdapter(false, anime)
                        val recyclerView: RecyclerView = view.findViewById(R.id.common_recycler)
                        recyclerView.adapter = adapter
                        recyclerView.layoutManager = LinearLayoutManager(context)
                    }
                }
                true
            } else {
                false
            }
        }

    }
}